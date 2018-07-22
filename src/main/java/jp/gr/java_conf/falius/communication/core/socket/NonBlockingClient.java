package jp.gr.java_conf.falius.communication.core.socket;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.Client;
import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

/**
 * <p>
 * ノンブロックな通信を行うクラスです。
 *
 * <p>
 * 送信内容はコンストラクタか{@link start}メソッドの引数に渡す{@link SendData}オブジェクトに格納し、
 *     受信内容は{@link OnReceiveListener}の引数かstartメソッドの戻り値で渡される
 *     {@link ReceiveData}オブジェクトから取得してください。
 * <p>
 * OnReceiverListenerの引数で渡されるReceiveDataオブジェクトから消費した受信データは
 *     start()メソッドの戻り値で渡されるReceiveDataオブジェクトには含まれていませんので注意してください。
 *
 * <p>
 *  closeメソッドを実行するまでの間、sendメソッド及びstartメソッドは複数回実行することができます。<br>
 *      sendメソッド、startメソッドを実行する度に新しい接続を確立して通信します。
 *
 *  <p>
 *  以下に、基本的な使用例を示します。
 *  <pre>
 *  {@code
 *  String HOST = "localhost";
 *  int PORT = 10000;
 *  SwapClient client = new NonBlockingClient(HOST, PORT);
 *
 *  SendData sendData = new BasicSendData();
 *  sendData.put(10);
 *  sendData.put("send from client");
 *  List<String> list = new ArrayList<>();
 *  list.add("abc");
 *  list.add("def");
 *  list.add("ghi");
 *  CollectionSendData csd = new CollectionSendData(sendData);
 *  csd.put(list);
 *
 *  ReceiveData ret = client.send(csd);
 *  String retStr = ret.getString();
 *  int retInt = ret.getInt();
 *  CollectionReceiveData crd = new CollectionReceiveData(ret);
 *  List<String> retList = crd.getList();
 *  }
 *  </pre>
 *  <p>
 *  上記の例は送受信がそれぞれ一回のみで通信を終える場合のコードです。
 *  startメソッドに{@link Swapper}インターフェース実装オブジェクトを渡すことで複数回に渡るやりとりを行うことも可能です。
 *  <p>
 *  また、sendメソッドでは受信が完了するまで処理は戻ってきません。<br>
 *  非同期に通信を行いたい場合はコンストラクタからSwapperを渡し、受信データはOnReceiveListenerから取得するという形で、
 *  {@link startOnNewThread}メソッドなどで別スレッドにて動作させるという方法もあります。<br>
 *  この場合、送受信がそれぞれ一度のみの場合はstartOnNewThreadメソッドの戻り値である{@link Future}オブジェクトから
 *  受信データを取得することもできます。
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
public class NonBlockingClient implements SwapClient, Disconnectable {
    private static final Logger log = LoggerFactory.getLogger(NonBlockingClient.class);

    private final String mServerHost;
    private final int mServerPort;
    private final long mPollTimeout;
    private final Swapper mSwapper;
    private final Set<SelectionKey> mKeys = Collections.synchronizedSet(new HashSet<>());

    private ExecutorService mExecutor = null;

    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;
    private OnDisconnectCallback mOnDisconnectCallback = null;
    private Client.OnConnectListener mOnConnectListener = null;

    /**
     *
     * @param serverHost
     * @param serverPort
     * @since 1.0
     */
    public NonBlockingClient(String serverHost, int serverPort) {
        this(serverHost, serverPort, 0L);
    }

    public NonBlockingClient(String serverHost, int serverPort, long timeout) {
        this(serverHost, serverPort, timeout, null);
    }

    /**
     *
     * @param serverHost
     * @param serverPort
     * @param swapper
     * @since 1.0
     */
    public NonBlockingClient(String serverHost, int serverPort, Swapper swapper) {
        this(serverHost, serverPort, 0L, swapper);
    }

    /**
     * このオブジェクトをCallableとして扱う際のコンストラクター
     * @param serverHost
     * @param serverPort
     * @param swapper
     */
    public NonBlockingClient(String serverHost, int serverPort, long timeout,
            Swapper swapper) {
        mServerHost = serverHost;
        mServerPort = serverPort;
        mPollTimeout = timeout;
        mSwapper = swapper;
    }

    /**
     * {@inheritDoc}
     * @since 1.0
     */
    @Override
    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    /**
     * {@inheritDoc}
     * @since 1.0
     */
    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    /**
     * {@inheritDoc}
     * @since 1.4.0
     */
    @Override
    public void addOnDisconnectCallback(OnDisconnectCallback callback) {
        mOnDisconnectCallback = callback;
    }

    /**
     * {@inheritDoc}
     * @since 1.4.2
     */
    @Override
    public void addOnConnectListener(Client.OnConnectListener listener) {
        mOnConnectListener = listener;
    }

    /**
     * {@inheritDoc}
     * @throws IOException
     * @throws NullPointerException コンストラクタにSwapperが渡されていない場合
     * @since 1.0
     */
    @Override
    public ReceiveData call() throws IOException, TimeoutException {
        Objects.requireNonNull(mSwapper, "could not call() without swapper");
        return start(mSwapper);
    }

    /**
     * {@inheritDoc}
     * @since 1.4.3
     *
     * @throws NullPointerException 内部に保持されているSwapperがnull(コンストラクタにSwapperが渡されていない)の場合
     */
    @Override
    public Future<ReceiveData> startOnNewThread() {
        Objects.requireNonNull(mSwapper);

        if (mExecutor == null) {
            synchronized (this) {
                if (mExecutor == null) {
                    mExecutor = Executors.newCachedThreadPool();
                }
            }
        }
        return mExecutor.submit(this);
    }

    /**
     * {@inheritDoc}
     * @since 1.0
     */
    @Override
    public void disconnect(SocketChannel channel, SelectionKey key, Throwable cause) throws IOException {
        String remote = channel.socket().getInetAddress().toString();
        channel.close();
        key.selector().wakeup();

        if (mOnDisconnectCallback != null) {
            mOnDisconnectCallback.onDissconnect(remote, cause);
        }

        log.info("client disconnect");
    }

    /**
     * このクライアントによる接続をすべて切断します。
     * 現状では複数回のstartメソッドの呼び出しが同一スレッドにより行われた場合には
     * 先の呼び出しによるチャネルが切断されてから次の呼び出しが行われますので、
     * このメソッドを呼び出す必要はありません(このあたりは内部仕様を変更する可能性があります)。
     * Swapper#swapメソッド内でfinishメソッドを呼んでいれば、このクラスをCallbleとして複数スレッドで動作した場合も同様です。
     * しかし、startOnNewThreadメソッドによって実行された場合には呼び出される必要があります。
     * また、異なるスレッドで実行している場合に外から処理を止める場合にも利用できます
     * (リスナー内で処理がブロックされている場合などを除き、タスクを終了できます)。
     * @throws IOException
     * @since 1.4.1
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
        if (mExecutor != null) {
            mExecutor.shutdownNow();
        }
        log.info("client close");
    }

    /**
     * 送受信を一度だけ行う場合の、start(Swapper)の簡易メソッドです。
     * @param sendData
     * @return 受信データ。受信に失敗するとnull
     * @throws IOException
     * @throws TimeoutException
     * @throws NullPointerException sendDataがnullの場合
     */
    @Override
    public ReceiveData send(SendData sendData) throws IOException, TimeoutException {
        Objects.requireNonNull(sendData);
        return start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                return sendData;
            }
        });
    }

    /**
     * @return 最終受信データ。受信に失敗するとnull
     * @throws NullPointerException swapperがnullの場合
     * @throws ConnectException 接続に失敗した場合
     * @throws IOException その他入出力エラーが発生した場合
     * @throws TimeoutException 接続がタイムアウトした場合
     * @since 1.0
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
                if (selector.select(mPollTimeout) > 0 || selector.selectedKeys().size() > 0) {

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        mKeys.add(key);
                        SocketHandler handler = (SocketHandler) key.attachment();
                        handler.handle(key);
                        iter.remove();
                    }

                } else {
                    throw new TimeoutException("could not get selected operation during " +
                            ((int) (double) mPollTimeout / 1000) + " sec.");
                }
            }
            log.debug("client finish");
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
     * @since 1.0
     */
    private Remote connect(SocketChannel channel, Swapper swapper) throws IOException {
        InetSocketAddress address = new InetSocketAddress(mServerHost, mServerPort);
        log.info("connect to ...{}", address.getAddress());
        channel.connect(address);
        log.info("success conect");

        String remoteAddress = channel.getRemoteAddress().toString();
        if (mOnConnectListener != null) {
            mOnConnectListener.onConnect(remoteAddress);
        }
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
