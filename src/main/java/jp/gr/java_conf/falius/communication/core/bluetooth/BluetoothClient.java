package jp.gr.java_conf.falius.communication.core.bluetooth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import jp.gr.java_conf.falius.communication.core.Client;
import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.bluetooth.devicesearch.DeviceSearcher;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.util.range.IntRange;

public class BluetoothClient implements SwapClient {
    private final String mRemoteURL;
    private final String mRemoteAddress;
    private final Set<Session> mSessions = Collections.synchronizedSet(new HashSet<Session>());

    private ExecutorService mExecutor = null;

    private Client.OnConnectListener mOnConnectListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;
    private OnDisconnectCallback mOnDisconnectCallback = null;

    private final Swapper mSwapper;

    public BluetoothClient(String uuid, RemoteDevice device) throws IOException {
        this(uuid, device, null);
    }

    public BluetoothClient(String uuid, RemoteDevice device, Swapper swapper) throws IOException {
        try {
            mRemoteURL = UrlDiscovery.getUrl(device, uuid);
        } catch (BluetoothStateException | InterruptedException e) {
            throw new IOException("could not get remote url", e);
        }
        mRemoteAddress = device.getBluetoothAddress();
        mSwapper = swapper;
    }

    @Override
    public Future<ReceiveData> startOnNewThread() {
        if (mExecutor == null) {
            synchronized (this) {
                if (mExecutor == null) {
                    mExecutor = Executors.newCachedThreadPool();
                }
            }
        }
        return mExecutor.submit(this);
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
    public void addOnDisconnectCallback(OnDisconnectCallback callback) {
        mOnDisconnectCallback = callback;
    }

    @Override
    public void addOnConnectListener(OnConnectListener listener) {
        mOnConnectListener = listener;
    }

    @Override
    public ReceiveData call() throws Exception {
        return start(mSwapper);
    }

    @Override
    public ReceiveData send(SendData sendData) throws IOException, TimeoutException {
        return start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) throws Exception {
                return sendData;
            }
        });
    }

    @Override
    public ReceiveData start(Swapper swapper) throws IOException, TimeoutException {
        Objects.requireNonNull(swapper);

        StreamConnection channel = (StreamConnection) Connector.open(mRemoteURL);
        if (mOnConnectListener != null) {
            mOnConnectListener.onConnect(mRemoteAddress);
        }

        try (Session session = new Session(channel, swapper,
                mOnSendListener, mOnReceiveListener, mOnDisconnectCallback, true)) {
            mSessions.add(session);
            session.run();
            return session.getData();
        }
    }

    @Override
    public void close() throws IOException {
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
        synchronized (this) {
            for (Session session : mSessions) {
                session.close();
            }
            mSessions.clear();
        }
    }

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, IOException, TimeoutException {
        final String UUID = "97d38833e31a4a718d8e4e44d052ce2b";

        RemoteDevice selectedDevice;
        try (DeviceSearcher searcher = new DeviceSearcher()) {
            Future<Set<RemoteDevice>> future = searcher.searchPairedDevice();
            System.out.println("search device");
            RemoteDevice[] devices = future.get().toArray(new RemoteDevice[0]);
            for (int i : new IntRange(devices.length)) {
                System.out.printf("%d: %s%n", i, devices[i].getFriendlyName(true));
            }
            int deviceNum;
            try (Scanner sc = new Scanner(System.in)) {
                System.out.println("choose device number: ");
                String line = sc.nextLine();
                deviceNum = Integer.parseInt(line);
            }

            selectedDevice = devices[deviceNum];
        }

        SwapClient client = new BluetoothClient(UUID, selectedDevice);
        SendData sendData = new BasicSendData();
        sendData.put("abcde");
        ReceiveData receiveData = client.send(sendData);
        String ret = receiveData.getString();
        System.out.println("ret: " + ret);
        System.out.println("active thread: " + Thread.activeCount());
    }

}