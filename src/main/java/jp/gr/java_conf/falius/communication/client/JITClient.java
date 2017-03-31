package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * 送信データのない状態でも接続を維持し、送信データがsendメソッドで与えられることで
 *     送信を行う方式をサポートしたソケットクライアント。
 * 送信データがない間はスレッドをブロックするため、必ずマルチスレッドで動作させてください。
 * startメソッドはnullを返しますので、サーバーからの受信データを受け取る手段はOnReceiveListener#onReceiveメソッド
 *     の引数に渡されるReceiveDataのみとなります。
 * @author "ymiyauchi"
 *
 */
public class JITClient implements Client {
    private static final Logger log = LoggerFactory.getLogger(JITClient.class);
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
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
            public SendData swap(String remoteAddress, ReceiveData receiveData) throws InterruptedException {
                SendData data = mSendDataQueue.take();
                log.debug("send data is null: {}", data == null);
                return data;
            }
        };
    }

    public Client startOnNewThread() throws IOException, TimeoutException {
        mExecutor.submit(this);
        return this;
    }

    public void close() throws IOException {
        log.debug("jit client close");
        mClient.close();
    }

    @Override
    public ReceiveData call() throws Exception {
        return mClient.call();
    }

    /**
     * サポーとされていません。
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
