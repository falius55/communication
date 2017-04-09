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
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.util.range.IntRange;

/**
 * <p>
 * Bluetooth通信を行うクライアントです。{@link NonBlockingClient}と同じインターフェースで利用できます。
 *
 * <p>
 * コンストラクタに渡すRemoteDeviceは{@link DeviceSearcher}を利用すると容易に取得できます。
 *
 * <p>
 * 以下に基本的な利用方法を示します。
 * <pre>
 * {@code
 *
 *        final String UUID = "97d38833e31a4a718d8e4e44d052ce2b";
 *
 *        // 接続先のデバイスを取得する。
 *        RemoteDevice selectedDevice;
 *        try (DeviceSearcher searcher = new DeviceSearcher()) {
 *            // 付近からペアリング済のデバイスを探索する。
 *            // 探索は非同期で行われるので、Futureを受け取る。
 *            Future<Set<RemoteDevice>> future = searcher.searchPairedDevice();
 *            System.out.println("search device");
 *            RemoteDevice[] devices = future.get().toArray(new RemoteDevice[0]);
 *
 *            // デバイスを番号付きで表示し、標準入力で選択する。
 *            for (int i = 0; i < devices.length; i++) {
 *                System.out.printf("%d: %s%n", i, devices[i].getFriendlyName(true));
 *            }
 *            try (Scanner sc = new Scanner(System.in)) {
 *                System.out.println("choose device number: ");
 *                String line = sc.nextLine();
 *                int deviceNum = Integer.parseInt(line);
 *                selectedDevice = devices[deviceNum];
 *            }
 *
 *        }
 *
 *        // クライアントのインスタンスを作成してしまえば後はNonBlockingClientと同じ
 *        SwapClient client = new BluetoothClient(UUID, selectedDevice);
 *
 *        SendData sendData = new BasicSendData();
 *        sendData.put("abcde");
 *
 *        ReceiveData receiveData = client.send(sendData);
 *
 *        String ret = receiveData.getString();
 *        System.out.println("ret: " + ret);
 * }
 * </pre>
 *
 * @see DeviceSearcher
 * @see NonBlockingClient
 *
 * @author "ymiyauchi"
 *
 */
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

    /**
     *
     * @param uuid -(ハイフンを除いた)UUIDの文字列
     * @param device 接続先のサーバーを表すデバイス
     * @param swapper
     * @throws IOException 接続先リモートデバイスのＵＲＬ取得に失敗した場合
     */
    public BluetoothClient(String uuid, RemoteDevice device, Swapper swapper) throws IOException {
        try {
            mRemoteURL = UrlDiscovery.getUrl(device, uuid);
        } catch (BluetoothStateException | InterruptedException e) {
            throw new IOException("could not get remote url", e);
        }
        mRemoteAddress = device.getBluetoothAddress();
        mSwapper = swapper;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOnDisconnectCallback(OnDisconnectCallback callback) {
        mOnDisconnectCallback = callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOnConnectListener(OnConnectListener listener) {
        mOnConnectListener = listener;
    }

    @Override
    public ReceiveData call() throws Exception {
        return start(mSwapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReceiveData send(SendData sendData) throws IOException, TimeoutException {
        return start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) throws Exception {
                return sendData;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReceiveData start(Swapper swapper) throws IOException {
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

            try (Scanner sc = new Scanner(System.in)) {
                System.out.println("choose device number: ");
                String line = sc.nextLine();
                int deviceNum = Integer.parseInt(line);
                selectedDevice = devices[deviceNum];
            }

        }

        SwapClient client = new BluetoothClient(UUID, selectedDevice);

        SendData sendData = new BasicSendData();
        sendData.put("abcde");

        ReceiveData receiveData = client.send(sendData);

        String ret = receiveData.getString();
        System.out.println("ret: " + ret);
    }

}