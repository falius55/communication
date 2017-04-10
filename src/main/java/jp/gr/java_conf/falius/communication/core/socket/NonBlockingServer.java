package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.Server;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

/**
 * {@inheritDoc}
 *
 *<p>
 * ノンブロッキング通信を行うサーバー
 *
 * <p>
 * 以下に、基本的な使用例を示します。
 * <p>
 *  {@link Swapper}は送信直前に実行される処理を定義するためのインターフェースで、新たなセッションが確立するごとに
 *      {@link SwapperFactory}によって作成されます。<br>
 *      その引数から受信データを取得できますので、送信データを返すようにしてください。<br>
 *      {@link Server#startOnNewThread}を実行することでサーバーが起動します。<br>
 * <pre>
 * {@code
 * int PORT = 10000;
 * try (Server server = new NonBlockingServer(PORT, new SwapperFactory() {
 *
 *      public void Swapper get() {
 *          return new OnceSwapper() {
 *
 *              public SendData swap(String remoteAddress, ReceiveData receiveData) {
 *                  int retInt = receiveData.getInt();
 *                  String retStr = receiveData.getString();
 *                  CollectionReceiveData crd = new CollectionReceiveData(receiveData);
 *                  {@literal List<String>} retList = crd.getList();
 *
 *                  SendData sendData = new BasicSendData();
 *                  sendData.put("data");
 *                  sendData.put(retInt * 1);
 *                  ArraySendData asd = new ArraySendData(sendData);
 *                  asd.put(retList.toArray(new String[0]));
 *                  return asd;
 *               }
 *      };
 * })) {
 *        server.addOnSendListener(new OnSendListener() {
 *             public void onSend(String remoteAddress) {
 *                System.out.println("send from " + remoteAddress);
 *               }
 *
 *           Future<?> future = server.startOnNewThread();
 *           future.get();
 *           });
 *       }
 * }
 * }
 * </pre>
 *
 * <p>
 * Timeoutの設定はなく、{@link shutdown}メソッドあるいは{@link close}メソッドが実行されるまで起動を
 * 続けます。
 *
 * <p>
 * 同一のインスタンスで複数回実行するようには設計されていません。<br>
 * 一度実行したインスタンスを使用して再度実行しようとした場合、
 * 二度目以降に実行したタスクは{@link IllegalStateException}によって実行を停止します
 * (startOnNewThreadメソッドの戻り値であるFutureのgetメソッドによって実際に投げられるのはExecutionException)
 *
 */
public class NonBlockingServer implements SocketServer, Disconnectable {
    private static final Logger log = LoggerFactory.getLogger(NonBlockingServer.class);

    private final int mServerPort;

    private volatile ServerSocketChannel mServerSocketChannel = null;
    private volatile Selector mSelector = null;

    private ExecutorService mExecutor = null;

    private final AcceptHandler mAcceptHandler;

    private Server.OnShutdownCallback mOnShutdownCallback = null;
    private OnDisconnectCallback mOnDisconnectCallback = null;

    private volatile boolean mIsStarted = false;

    public NonBlockingServer(int serverPort, SwapperFactory swapperFactory) {
        mServerPort = serverPort;
        mAcceptHandler = new AcceptHandler(this, swapperFactory);
    }

    @Override
    public void addOnSendListener(OnSendListener listener) {
        mAcceptHandler.addOnSendListener(listener);
    }

    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mAcceptHandler.addOnReceiveListener(listener);
    }

    @Override
    public void addOnAcceptListener(Server.OnAcceptListener listener) {
        mAcceptHandler.addOnAcceptListener(listener);
    }

    @Override
    public void addOnShutdownCallback(Server.OnShutdownCallback callback) {
        mOnShutdownCallback = callback;
    }

    @Override
    public void addOnDisconnectCallback(OnDisconnectCallback callback) {
        mOnDisconnectCallback = callback;
    }

    /**
     * 独自に作成したスレッドで実行する際に利用します。
     * しかし、同一インスタンスを並列実行することは想定されていませんので注意してください。
     * @return null
     */
    @Override
    public Throwable call() throws IOException {
        exec();
        return null;
    }

    /**
     * @throws IllegalStateException すでに実行済にもかかわらず実行した場合
     */
    @Override
    public Future<?> startOnNewThread() {
        // Handler内で発生したIOExceptionは個別の接続先との問題であると判断し、
        // 内部でcatchした後発生先との接続を切断して続行する。
        // それ以外の箇所で発生したIOExceptionはサーバー全体に問題が発生していると判断し、
        // スレッドを停止して例外を外に伝播させている
        synchronized (this) {
            if (mIsStarted) {
                throw new IllegalStateException("server is already executed.");
            }
        }

        if (mExecutor == null) {
            synchronized (this) {
                if (mExecutor == null) {
                    mExecutor = Executors.newSingleThreadExecutor();
                }

            }
        }
        try {
            return mExecutor.submit(this);
        } catch (RejectedExecutionException e) {
            // shutdownメソッドによってmExecutorもシャットダウンされている
            throw new IllegalStateException("already shutdown");
        }
    }

    /**
     * shutdownメソッドと同義です。
     */
    @Override
    public void close() throws IOException {
        shutdown();
    }

    @Override
    public void shutdown() throws IOException {
        if (mServerSocketChannel == null || mSelector == null) {
            return;
        }

        mSelector.wakeup();
        mServerSocketChannel.close();
        if (mExecutor != null) {
            mExecutor.shutdown();
            log.debug("executor shutdown");
        }

        if (mOnShutdownCallback != null) {
            mOnShutdownCallback.onShutdown();
        }
        log.info("server shutdown");
    }

    private void exec() throws IOException {
        synchronized (this) {
            if (mIsStarted) {
                throw new IllegalStateException("server is already executed.");
            }
            mIsStarted = true;
        }
        try (Selector selector = Selector.open();
                ServerSocketChannel channel = ServerSocketChannel.open()) {
            log.debug("open selecctor and channel");
            mSelector = selector;
            mServerSocketChannel = channel;
            bind(channel);

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_ACCEPT, mAcceptHandler);

            while (channel.isOpen()) {
                // select()メソッドの戻り値は新しく通知(OP_ACCEPT)のあったキーの数
                // selectedKeys(Setオブジェクト)から明示的に削除しない限り、
                // キーはselectedKeysに格納されたままになる
                // 削除しないと、次回も再び同じキーで通知される
                if (selector.select() > 0 || selector.selectedKeys().size() > 0) {

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        SocketHandler handler = (SocketHandler) key.attachment();
                        handler.handle(key);
                        iter.remove();
                    }

                }
            }

        }

        log.info("terminate server operation");
    }

    private void bind(ServerSocketChannel channel) throws IOException {
        InetSocketAddress address = new InetSocketAddress(mServerPort);
        log.info("bind to ... {} : {}", getLocalHostAddress(), address.getPort());
        channel.socket().bind(address); // ポートが競合したら処理が返ってこない？
        log.info("success binding");
    }

    @Override
    public void disconnect(SocketChannel channel, SelectionKey key, Throwable cause) {
        try {
            String remote = channel.socket().getRemoteSocketAddress().toString();

            if (channel != null) {
                channel.close();
            }
            key.cancel();

            if (mOnDisconnectCallback != null) {
                mOnDisconnectCallback.onDissconnect(remote, cause);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ループバックアドレスではないIPv4アドレスを取得します。
     * 取得に失敗するとnull
     */
    @Override
    public String getLocalHostAddress() {
        try {
            for(NetworkInterface n: Collections.list(NetworkInterface.getNetworkInterfaces()) ) {
                for (InetAddress addr : Collections.list(n.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("Could not get local address", e);
        }
        return null;
    }

    @Override
    public int getPort() {
        return mServerPort;
    }
}
