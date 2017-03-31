package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.remote.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * <p>
 * 送信データのない状態でも接続を維持し、送信データがsendメソッドで与えられることで
 *     送信を行う方式をサポートしたソケットクライアント。
 * <p>
 * 送信データがない間はスレッドをブロックするため、必ずマルチスレッドで動作させてください。
 * <p>
 * sendメソッドはnullを返しますので、サーバーからの受信データを受け取る手段はOnReceiveListener#onReceiveメソッド
 *     の引数に渡されるReceiveDataのみとなります。
 * <P>
 * 同一インスタンスを複数のスレッドで動作させた場合、sendメソッドによって与えられた送信データを各スレッドで共同して送信する
 *     ことになります。
 * @author "ymiyauchi"
 *
 */
public class JITClient implements Client {
    private static final Logger log = LoggerFactory.getLogger(JITClient.class);
    private final Client mClient;
    private final BlockingQueue<SendData> mSendDataQueue = new LinkedBlockingQueue<>();

    /**
     *
     * @param serverHost
     * @param port
     * @param onReceiveListener
     * @throws IOException
     * @throws TimeoutException
     */
    public JITClient(String serverHost, int port, OnReceiveListener onReceiveListener) {
        mClient = new NonBlockingClient(serverHost, port, createSwapper());
        mClient.addOnReceiveListener(onReceiveListener);
    }

    /**
     * 送信データを与えます。
     * 戻り値からは受信データを得られません。受信データはOnReceiveListener#onReceiveメソッドの引数から取得してください。
     * @return null
     */
    @Override
    public ReceiveData send(SendData data) {
        mSendDataQueue.add(data);
        return null;
    }

    private Swapper createSwapper() {
        return new RepeatSwapper() {
            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                SendData data;
                try {
                    data = mSendDataQueue.take();
                } catch (InterruptedException e) {
                    return null;
                }
                return data;
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
    public void close() throws IOException {
        log.debug("jit client close");
        mClient.close();
    }

    @Override
    public ReceiveData call() throws Exception {
        return mClient.call();
    }

    /**
     * サポートされていません。
     * @throws UnsupportedOperationException 常に投げられる
     */
    @Override
    public ReceiveData start(Swapper swapper) {
        throw new UnsupportedOperationException();
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
}
