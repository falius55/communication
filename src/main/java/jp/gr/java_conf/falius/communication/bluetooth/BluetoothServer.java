package jp.gr.java_conf.falius.communication.bluetooth;

import java.io.IOException;
import java.util.Scanner;
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

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

public class BluetoothServer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(BluetoothServer.class);

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    private final SwapperFactory mSwapperFactory;

    private boolean mIsShutdowned = false;

    /**
     * UUIDは独自プロトコルのサービスの場合は固有に生成する。
     * - 各種ツールで生成する。（ほぼ乱数）
     * - 注：このまま使わないように。
     */
    static final String serverUUID = "11111111111111111111111111111123";

    private StreamConnectionNotifier mConnection = null;

    public BluetoothServer(SwapperFactory swapperFactory) throws IOException {
        mSwapperFactory = swapperFactory;
        // RFCOMMベースのサーバの開始。
        // - btspp:は PRCOMM 用なのでベースプロトコルによって変わる。
        mConnection = (StreamConnectionNotifier) Connector.open(
                "btspp://localhost:" + serverUUID,
                Connector.READ_WRITE, true);
        // ローカルデバイスにサービスを登録。必須ではない。
        ServiceRecord record = LocalDevice.getLocalDevice().getRecord(mConnection);
        LocalDevice.getLocalDevice().updateRecord(record);

        log.debug("server start");
    }

    public Future<?> startOnNewThread() {
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
                Session session = accept();
                mExecutor.submit(session);
            }
        } catch (IOException e) {

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
        return new Session(channel, mSwapperFactory.get());
    }

    public void shutdown() throws IOException {
        log.debug("shutdown");
        if (mConnection != null) {
            mConnection.close();
        }
        mIsShutdowned = true;
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }


    public static void main(String...strings ) {
        try (BluetoothServer server = new BluetoothServer(new SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiveData) {
                        String rcv = receiveData.getString();
                        SendData sendData = new BasicSendData();
                        sendData.put(rcv.toUpperCase());
                        return sendData;
                    }

                };
            }

        }); Scanner sc = new Scanner(System.in)) {
            server.startOnNewThread();

            while (true) {
                if ((sc.nextLine()).equals("stop")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
