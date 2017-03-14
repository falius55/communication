package communication.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import communication.Disconnectable;
import communication.Remote;
import communication.RepeatSwapper;
import communication.Swapper;
import communication.handler.Handler;
import communication.handler.WritingHandler;
import communication.receiver.OnReceiveListener;
import communication.receiver.Receiver;
import communication.sender.MultiDataSender;
import communication.sender.OnSendListener;
import communication.sender.Sender;

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
public class NonBlockingClient implements Client, Disconnectable {
    private static final long POLL_TIMEOUT = 5000L;

    private final String mServerHost;
    private final int mServerPort;

    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    private Swapper.SwapperFactory mSwapperFactory;

    private boolean mIsExit = false;
    private Selector mSelector = null;

    public NonBlockingClient(String serverHost, int serverPort) {
        this(serverHost, serverPort, null);
    }

    /**
     * このオブジェクトをRunnableとして扱う際のコンストラクター
     * @param serverHost
     * @param serverPort
     * @param sender
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
        if (mSelector != null) {
            mSelector.wakeup();
        }
    }

    /**
     * @return
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
                (address, size, rec) -> System.out.println("on receive"));
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
                        System.out.println("return from server in swap():" + receiver.getString());
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
                System.out.println("return from server:" + result.getString());
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

}
