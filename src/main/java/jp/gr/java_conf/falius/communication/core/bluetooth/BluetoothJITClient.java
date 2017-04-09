package jp.gr.java_conf.falius.communication.core.bluetooth;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.RemoteDevice;

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
 * 任意のタイミングで送信データを供給することができるBluetooth通信におけるクライアントです。
 * @author "ymiyauchi"
 *
 */
public class BluetoothJITClient implements JITClient {
    private final Client mClient;
    private final BlockingQueue<SendData> mSendDataQueue = new LinkedBlockingQueue<>();

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

    @Override
    public Future<ReceiveData> startOnNewThread() {
        return mClient.startOnNewThread();
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
    public void addOnConnectListener(OnConnectListener listener) {
        mClient.addOnConnectListener(listener);
    }

    @Override
    public void close() throws IOException {
        mClient.close();
    }

    @Override
    public ReceiveData call() throws Exception {
        return mClient.call();
    }

    @Override
    public void send(SendData sendData) throws IOException, TimeoutException {
        mSendDataQueue.add(sendData);
    }

}
