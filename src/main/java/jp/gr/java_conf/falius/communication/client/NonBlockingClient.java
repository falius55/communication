package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.handler.Handler;
import jp.gr.java_conf.falius.communication.handler.WritingHandler;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.remote.Disconnectable;
import jp.gr.java_conf.falius.communication.remote.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.remote.Remote;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

/**
 * <p>
 * ノンブロックな通信を行うクラスです。
 *
 * <p>
 * 送信内容はコンストラクタかstartメソッドの引数に渡すSendDataオブジェクトに格納し、
 *     受信内容はOnReceiveListenerの引数かstartメソッドの戻り値で渡される
 *     ReceiveDataオブジェクトから取得してください。
 * <p>
 * OnReceiverListenerの引数で渡されるReceiveDataオブジェクトから消費した受信データは
 *     start()メソッドの戻り値で渡されるReceiveDataオブジェクトには含まれていませんので注意してください。
 *
 * <p>
 *  startメソッドを実行する度に新しい接続を確立して通信します。
 * @author "ymiyauchi"
 *
 */
public class NonBlockingClient implements Client, Disconnectable {
    private static final Logger log = LoggerFactory.getLogger(NonBlockingClient.class);
    private static final long POLL_TIMEOUT = 30000L;

    private final String mServerHost;
    private final int mServerPort;
    private final Set<SelectionKey> mKeys = Collections.synchronizedSet(new HashSet<>());

    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;
    private OnDisconnectCallback mOnDisconnectCallback = null;

    private Swapper mSwapper = null;

    public NonBlockingClient(String serverHost, int serverPort) {
        this(serverHost, serverPort, null);
    }

    /**
     * このオブジェクトをCallableとして扱う際のコンストラクター
     * @param serverHost
     * @param serverPort
     * @param swapper
     */
    public NonBlockingClient(String serverHost, int serverPort,
            Swapper swapper) {
        mServerHost = serverHost;
        mServerPort = serverPort;
        mSwapper = swapper;
    }

    @Override
    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    @Override
    public void addOnDisconnectCallback(OnDisconnectCallback callback) {
        mOnDisconnectCallback = callback;
    }

    /**
     * @throws IOException
     * @throws NullPointerException コンストラクタにSwapperが渡されていない場合
     */
    @Override
    public ReceiveData call() throws IOException, TimeoutException {
        Objects.requireNonNull(mSwapper, "could not call() without swapper");
        return start(mSwapper);
    }

    @Override
    public void disconnect(SocketChannel channel, SelectionKey key, Throwable cause) throws IOException {
        String remote = channel.socket().getInetAddress().toString();
        channel.close();
        key.selector().wakeup();

        if (mOnDisconnectCallback != null) {
            mOnDisconnectCallback.onDissconnect(remote, cause);
        }
    }

    /**
     * このクライアントによる接続をすべて切断します。
     * 現状では複数回のstartメソッドの呼び出しが同一スレッドにより行われた場合には
     * 先の呼び出しによるチャネルが切断されてから次の呼び出しが行われますので、
     * このメソッドを呼び出す必要はありません(このあたりは内部仕様を変更する可能性があります)。
     * Swapper#swapメソッド内でfinishメソッドを呼んでいれば、このクラスをCallbleとして複数スレッドで動作した場合も同様です。
     * 異なるスレッドで実行している場合に外から処理を止める場合に利用します。
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        synchronized (mKeys) {
            for (SelectionKey key : mKeys) {
                SelectableChannel channel = key.channel();
                if (key.isValid() && channel instanceof SocketChannel) {
                    disconnect((SocketChannel) channel, key, null);
                }
            }
            mKeys.clear();
        }
    }

    /**
     * @throws NullPointerException sendDataがnullの場合
     */
    @Override
    public ReceiveData send(SendData sendData) throws IOException, TimeoutException {
        Objects.requireNonNull(sendData);
        if (!sendData.hasRemain()) {
            throw new IllegalArgumentException("send data is empty");
        }
        return start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                return sendData;
            }
        });
    }

    /**
     * @throws NullPointerException swapperがnullの場合
     * @throws ConnectException 接続に失敗した場合
     * @throws IOException その他入出力エラーが発生した場合
     * @throws TimeoutException 接続がタイムアウトした場合
     */
    @Override
    public ReceiveData start(Swapper swapper) throws IOException, TimeoutException {
        log.debug("client start");
        Objects.requireNonNull(swapper, "swapper is null");
        try (Selector selector = Selector.open(); SocketChannel channel = SocketChannel.open()) {
            Remote remote = connect(channel, swapper); // 接続はブロッキングモード
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE,
                    new WritingHandler(this, remote, true));

            while (channel.isOpen()) {
                log.debug("client in loop");
                if (selector.select(POLL_TIMEOUT) > 0 || selector.selectedKeys().size() > 0) {
                    log.debug("client selectedKeys: {}", selector.selectedKeys().size());

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        mKeys.add(key);
                        Handler handler = (Handler) key.attachment();
                        log.debug("client handle");
                        handler.handle(key);
                        iter.remove();
                    }

                } else {
                    throw new TimeoutException("could not get selected operation during " +
                            ((int) (double) POLL_TIMEOUT / 1000) + " sec.");
                }
            }
            log.debug("client end");
            return remote.receiver().getData();
        }
    }

    /**
     *
     * @param channel
     * @param swapper
     * @return
     * @throws IOException
     * @throws ConnectException 接続に失敗した場合
     */
    private Remote connect(SocketChannel channel, Swapper swapper) throws IOException {
        InetSocketAddress address = new InetSocketAddress(mServerHost, mServerPort);
        channel.connect(address);

        String remoteAddress = channel.getRemoteAddress().toString();
        SwapperFactory swapperFactory = new SwapperFactory() {

            @Override
            public Swapper get() {
                return swapper;
            }
        };
        Remote remote = new Remote(remoteAddress, swapperFactory);
        remote.addOnSendListener(mOnSendListener);
        remote.addOnReceiveListener(mOnReceiveListener);
        return remote;
    }
}
