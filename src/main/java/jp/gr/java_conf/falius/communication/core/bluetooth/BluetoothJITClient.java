package jp.gr.java_conf.falius.communication.core.bluetooth;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.RemoteDevice;

import jp.gr.java_conf.falius.communication.core.Client;
import jp.gr.java_conf.falius.communication.core.JITClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingJITClient;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * <p>
 * 任意のタイミングで送信データを供給することができるBluetooth通信におけるクライアントです。
 *
 * <p>
 * コンストラクタにUUIDとリモートデバイスを指定すること以外は{@link NonBlockingJITClient}と同様です。
 *
 * @see NonBlockingJITClient
 * @author "ymiyauchi"
 * @since 1.5.0
 *
 */
public class BluetoothJITClient implements JITClient {
    private final Client mClient;
    private final BlockingQueue<SendData> mSendDataQueue = new LinkedBlockingQueue<>();

    /**
     *
     * @param uuid
     * @param device
     * @param onReceiveListener
     * @throws IOException
     * @since 1.5.0
     */
    public BluetoothJITClient(String uuid, RemoteDevice device,
            OnReceiveListener onReceiveListener) throws IOException {
        mClient = new BluetoothClient(uuid, device, createSwapper());
        mClient.addOnReceiveListener(onReceiveListener);
    }

    private Swapper createSwapper() {
        return new RepeatSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) throws Exception {
                try {
                    return mSendDataQueue.take();
                } catch (InterruptedException e) {
                    return null;
                }
            }

        };
    }

    /**
     * {@inheritDoc}
     * @since 1.5.0
     */
    @Override
    public Future<ReceiveData> startOnNewThread() {
        return mClient.startOnNewThread();
    }

    /**
     * {@inheritDoc}
     * @since 1.5.0
     */
    @Override
    public void addOnSendListener(OnSendListener listener) {
        mClient.addOnSendListener(listener);
    }

    /**
     * {@inheritDoc}
     * @since 1.5.0
     */
    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mClient.addOnReceiveListener(listener);
    }

    /**
     * {@inheritDoc}
     * @since 1.5.0
     */
    @Override
    public void addOnDisconnectCallback(OnDisconnectCallback callback) {
        mClient.addOnDisconnectCallback(callback);
    }

    /**
     * {@inheritDoc}
     * @since 1.5.0
     */
    @Override
    public void addOnConnectListener(OnConnectListener listener) {
        mClient.addOnConnectListener(listener);
    }

    /**
     * @since 1.5.0
     */
    @Override
    public void close() throws IOException {
        mClient.close();
    }

    /**
     * @since 1.5.0
     */
    @Override
    public ReceiveData call() throws Exception {
        return mClient.call();
    }

    /**
     * {@inheritDoc}
     * @since 1.5.0
     */
    @Override
    public void send(SendData sendData) throws IOException, TimeoutException {
        Objects.requireNonNull(sendData);
        mSendDataQueue.add(sendData);
    }

}
