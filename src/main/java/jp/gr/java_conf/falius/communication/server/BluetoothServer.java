package jp.gr.java_conf.falius.communication.server;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.bluetooth.Session;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.remote.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

public class BluetoothServer implements Server, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(BluetoothServer.class);
    /**
     * UUIDは独自プロトコルのサービスの場合は固有に生成する。
     * - 各種ツールで生成する。（ほぼ乱数）
     * - 注：このまま使わないように。
     */
    private static final String serverUUID = "11111111111111111111111111111123";

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    private final SwapperFactory mSwapperFactory;
    private final String mServerUuid;

    private OnSendListener mOnSendListener = null;
    private Server.OnAcceptListener mOnAcceptListener = null;
    private OnReceiveListener mOnReceiveListener = null;
    private Server.OnShutdownCallback mOnShutdownCallback = null;
    private OnDisconnectCallback mOnDisconnectCallback = null;

    private boolean mIsShutdowned = false;

    private StreamConnectionNotifier mConnection = null;

    public BluetoothServer(String serverUuid, SwapperFactory swapperFactory) throws IOException {
        mServerUuid = serverUuid;
        mSwapperFactory = swapperFactory;
        // RFCOMMベースのサーバの開始。
        // - btspp:は PRCOMM 用なのでベースプロトコルによって変わる。
        mConnection = (StreamConnectionNotifier) Connector.open(
                "btspp://localhost:" + mServerUuid,
                Connector.READ_WRITE, true);
        // ローカルデバイスにサービスを登録。必須ではない。
        // これによって、周囲の機器がこのアプリを探せるようになる(macアドレスの登録をいちいちしなくてよくなる)
        ServiceRecord record = LocalDevice.getLocalDevice().getRecord(mConnection);
        LocalDevice.getLocalDevice().updateRecord(record);

        log.debug("server start");
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
    public void addOnAcceptListener(Server.OnAcceptListener listener) {
        mOnAcceptListener = listener;
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
     * @return null
     */
    @Override
    public Throwable call() throws IOException {
        exec();
        return null;
    }

    @Override
    public Future<?> startOnNewThread() {
        log.debug("start on new thread");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                exec();
            }
        };
        return mExecutor.submit(runnable);
    }

    private void exec() {
        try {
            while (!mIsShutdowned) {
                log.debug("in loop");
                Session session = accept();
                mExecutor.submit(session);
            }
        } catch (IOException e) {
            log.error("I/O error in exec : {}", e.getMessage());
        }
    }

    /**
     * クライアントからの接続待ち。
     * @return 接続されたたセッションを返す。
     */
    private Session accept() throws IOException {
        log.debug("Accept");
        StreamConnection channel = mConnection.acceptAndOpen();
        log.debug("Connect");
        if (mOnAcceptListener != null) {
            mOnAcceptListener.onAccept(channel.toString());
        }
        return new Session(channel, mSwapperFactory.get(),
                mOnSendListener, mOnReceiveListener, mOnDisconnectCallback);
    }

    @Override
    public void shutdown() throws IOException {
        log.debug("shutdown");
        if (mConnection != null) {
            mConnection.close();
        }
        mExecutor.shutdown();
        mIsShutdowned = true;

        if (mOnShutdownCallback != null) {
            mOnShutdownCallback.onShutdown();
        }
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }

    public static void main(String... strings) throws InterruptedException, ExecutionException {
        try (BluetoothServer server = new BluetoothServer(serverUUID, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new RepeatSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiveData) {
                        try {
                            log.debug("swap");
                            String rcv = receiveData.getString();
                            log.debug("receive: {}", rcv);
                            SendData sendData = new BasicSendData();
                            sendData.put(rcv.toUpperCase());
                            return sendData;
                        } catch (Exception e) {
                            log.error("swapper error", e);
                            throw new NullPointerException();
                        }
                    }

                };
            }

        }); Scanner sc = new Scanner(System.in)) {
            Future<?> future = server.startOnNewThread();
            future.get();

            while (true) {
                log.debug("main loop");
                System.out.println("please type stop if you want to stop server");
                if ((sc.nextLine()).equals("stop")) {
                    break;
                }
            }

            log.debug("main end");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
