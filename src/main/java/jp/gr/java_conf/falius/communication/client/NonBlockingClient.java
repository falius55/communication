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
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
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
    private static final long POLL_TIMEOUT = 5000L;

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
        mIsExit = true;
        String remote = channel.socket().getInetAddress().toString();
        if (mSelector != null) {
            mSelector.wakeup();
        }

        if (mOnDisconnectCallback != null) {
            mOnDisconnectCallback.onDissconnect(remote, cause);
        }
    }

    /**
     * @throws ConnectException 接続に失敗した場合
     * @throws IOException その他入出力エラーが発生した場合。接続がタイムアウトした場合も含まれます。
     */
    @Override
    public Receiver start(Swapper swapper) throws IOException, TimeoutException {
        Objects.requireNonNull(swapper, "sender is null");
        try (Selector selector = Selector.open(); SocketChannel channel = SocketChannel.open()) {
            mSelector = selector;
            Remote remote = connect(channel, swapper); // 接続はブロッキングモード
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE,
                    new WritingHandler(this, remote, true));

            while (!mIsExit) {
                if (selector.select(POLL_TIMEOUT) > 0 || selector.selectedKeys().size() > 0) {

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        Handler handler = (Handler) key.attachment();
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

    private Remote connect(SocketChannel channel, Swapper swapper) throws IOException {
        InetSocketAddress address = new InetSocketAddress(mServerHost, mServerPort);
        channel.connect(address);

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
        return remote;
    }

    public static void main(String... args) {

        Client client = new NonBlockingClient("localhost", 6200);
        client.addOnReceiveListener(
                (address, size, rec) -> log.debug("on receive"));
        try {
            Receiver result = client.start(new RepeatSwapper() {
                private int i = 0;

                @Override
                public Sender swap(String remoteAddress, Receiver receiver) {
                    Sender sender = new MultiDataSender();
                    sender.put("I am Client. Who are you?");
                    sender.put("put2 msg");
                    sender.put("title");
                    sender.put(4096);
                    sender.put("I send it " + i);
                    if (receiver != null) {
                        log.debug("return from server in swap():" + receiver.getString());
                    }
                    i++;
                    if (i > 4)
                        finish();
                    return sender;
                }

            });
            if (result == null) {
                return;
            }
            for (int i = 0, len = result.dataCount(); i < len; i++) {
                log.debug("return from server:" + result.getString());
            }
        } catch (IOException | TimeoutException e) {
            log.error("client error: ", e);
        }
    }

}
