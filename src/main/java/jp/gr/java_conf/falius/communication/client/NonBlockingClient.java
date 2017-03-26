package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.Remote;
import jp.gr.java_conf.falius.communication.handler.Handler;
import jp.gr.java_conf.falius.communication.handler.WritingHandler;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * ノンブロックな通信を行うクラスです
 * 送信内容はコンストラクタかstartメソッドの引数に渡すSenderオブジェクトに格納し、
 *  受信内容はOnReceiveListenerの引数かstartメソッドの戻り値で渡される
 *   Receiverオブジェクトから取得してください。
 * OnReceiverListenerの引数で渡されるReceiverオブジェクトから消費した受信データは
 *  start()メソッドの戻り値で渡されるReceiverオブジェクトには含まれていませんので注意してください。
 * @author "ymiyauchi"
 *
 */
public class NonBlockingClient implements Client {
    private static final Logger log = LoggerFactory.getLogger(NonBlockingClient.class);
    private static final long POLL_TIMEOUT = 30000L;

    private final String mServerHost;
    private final int mServerPort;

    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;
    private OnDisconnectCallback mOnDisconnectCallback = null;

    private Swapper.SwapperFactory mSwapperFactory;

    private boolean mIsExit = false;
    private Selector mSelector = null;

    public NonBlockingClient(String serverHost, int serverPort) {
        this(serverHost, serverPort, null);
    }

    /**
     * このオブジェクトをCallableとして扱う際のコンストラクター
     * @param serverHost
     * @param serverPort
     * @param swapperFactory
     */
    public NonBlockingClient(String serverHost, int serverPort,
            Swapper.SwapperFactory swapperFactory) {
        mServerHost = serverHost;
        mServerPort = serverPort;
        mSwapperFactory = swapperFactory;
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
     */
    @Override
    public Receiver call() throws IOException, TimeoutException {
        return start(mSwapperFactory.get());
    }

    @Override
    public void disconnect(SocketChannel channel, SelectionKey key, Throwable cause) {
        log.debug("before disconnect synchronized");
        synchronized(this) {
            mIsExit = true;
        }
        String remote = channel.socket().getInetAddress().toString();
        if (mSelector != null) {
            mSelector.wakeup();
        }

        if (mOnDisconnectCallback != null) {
            mOnDisconnectCallback.onDissconnect(remote, cause);
        }
    }

    @Override
    public Receiver start(Sender sender) throws IOException, TimeoutException {
        return start(new OnceSwapper() {

            @Override
            public Sender swap(String remoteAddress, Receiver receiver) {
                return sender;
            }
        });
    }
    /**
     * @throws ConnectException 接続に失敗した場合
     * @throws IOException その他入出力エラーが発生した場合。接続がタイムアウトした場合も含まれます。
     */
    @Override
    public Receiver start(Swapper swapper) throws IOException, TimeoutException {
        log.debug("before start synchronized");
        synchronized(this) {
            mIsExit = false;
        }
        Objects.requireNonNull(swapper, "swapper is null");
        try (Selector selector = Selector.open(); SocketChannel channel = SocketChannel.open()) {
            mSelector = selector;
            Remote remote = connect(channel, swapper); // 接続はブロッキングモード
            channel.configureBlocking(false);
            log.debug("before client channel register");
            channel.register(selector, SelectionKey.OP_WRITE,
                    new WritingHandler(this, remote, true));
            log.debug("client mIsExit: {}", mIsExit);

            while (!mIsExit) {
                log.debug("client in loop");
                if (selector.select(POLL_TIMEOUT) > 0 || selector.selectedKeys().size() > 0) {
                    log.debug("client selectedKeys: {}", selector.selectedKeys().size());

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        Handler handler = (Handler) key.attachment();
                        log.debug("client handle");
                        handler.handle(key);
                        iter.remove();
                    }

                } else {
                    throw new TimeoutException("could not get selected operation during " +
                            ((int)(double)POLL_TIMEOUT / 1000) + " sec.");
                }
            }
            return remote.receiver();
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
        log.debug("before client connect");
        channel.connect(address);
        log.debug("after client connect");

        String remoteAddress = channel.getRemoteAddress().toString();
        Swapper.SwapperFactory swapperFactory = new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return swapper;
            }
        };
        Remote remote = new Remote(remoteAddress, swapperFactory);
        remote.addOnSendListener(mOnSendListener);
        remote.addOnReceiveListener(mOnReceiveListener);
        log.debug("client finish connect");
        return remote;
    }
}
