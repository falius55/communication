package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import jp.gr.java_conf.falius.communication.core.Client;
import jp.gr.java_conf.falius.communication.core.JITClient;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * <p>
 * 送信データのない状態でも接続を維持し、送信データが{@link send}メソッドで与えられることで
 *     送信を行う方式をサポートしたソケットクライアント。<br>
 *     素の{@link NonBlockingClient}が{@link Swapper}ですぐに送信データが作成されるのに対して、
 *     任意のタイミングで送信データを供給することができます。
 * <p>
 * 送信データがない間はスレッドをブロックするため、必ずマルチスレッドで動作させてください。
 * <p>
 * サーバーからの受信データを受け取る手段は主に{@link OnReceiveListener#onReceive}メソッドの引数に
 *     渡される{@link ReceiveData}からです。
 * <P>
 * 同一インスタンスを複数のスレッドで動作させた場合、sendメソッドによって与えられた送信データを各スレッドで分担して送信する
 *     ことになります。
 *
 * <p>
 * 以下に、基本的な使用例を示します。
 * <pre>
 * {@code
 * String HOST = "localhost";
 * int PORT = 10000;
 * try (JITClient client = new NonBlockingJITClient(HOST, PORT, new OnReceiveListener() {
 *      public void onReceive(String remoteAddress, ReceiveData receiveData) {
 *          System.out.println(receiveData.getString());
 *      }
 * })) {
 *      client.startOnNewThread();
 *
 *      for (int i = 0; i < 10; i++) {
 *          SendData sendData = new BasicSendData();
 *          sendData.put(i);
 *          client.send(sendData);
 *      }
 * }
 * }
 * </pre>
 * <p>
 * {@link close}メソッドが呼ばれるまで接続を維持しているので、送信データを供給するタイミングに制限はありません。
 *
 * @author "ymiyauchi"
 *
 */
public class NonBlockingJITClient implements JITClient {
    private final Client mClient;
    private final BlockingQueue<SendData> mSendDataQueue = new LinkedBlockingQueue<>();

    /**
     *
     * @param serverHost
     * @param port
     * @param onReceiveListener
     */
    public NonBlockingJITClient(String serverHost, int port, OnReceiveListener onReceiveListener) {
        mClient = new NonBlockingClient(serverHost, port, createSwapper());
        mClient.addOnReceiveListener(onReceiveListener);
    }

    /**
     * 送信データを与えます。
     * 戻り値からは受信データを得られません。受信データはOnReceiveListener#onReceiveメソッドの引数から取得してください。
     * @throws NullPointerException dataがnullの場合
     */
    @Override
    public void send(SendData data) {
        Objects.requireNonNull(data);
        mSendDataQueue.add(data);
    }

    private Swapper createSwapper() {
        return new RepeatSwapper() {
            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                try {
                    return mSendDataQueue.take();
                } catch (InterruptedException e) {
                    return null;
                }
            }
        };
    }

    /**
     * 新たなスレッドで新規に接続を確立して通信を行います。
     * @return 新規に確立した接続における最終受信データを含むFutureオブジェクト
     */
    @Override
    public Future<ReceiveData> startOnNewThread() {
        return mClient.startOnNewThread();
    }

    /**
     * 確立した接続をすべて切断します。
     */
    @Override
    public void close() throws IOException {
        mClient.close();
    }

    @Override
    public ReceiveData call() throws Exception {
        return mClient.call();
    }

    @Override
    public void addOnSendListener(OnSendListener listener) {
        mClient.addOnSendListener(listener);
    }

    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mClient.addOnReceiveListener(listener);
    }

    @Override
    public void addOnDisconnectCallback(OnDisconnectCallback callback) {
        mClient.addOnDisconnectCallback(callback);
    }

    @Override
    public void addOnConnectListener(Client.OnConnectListener listener) {
        mClient.addOnConnectListener(listener);
    }
}
