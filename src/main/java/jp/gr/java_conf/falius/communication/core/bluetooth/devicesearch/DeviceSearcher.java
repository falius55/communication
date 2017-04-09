package jp.gr.java_conf.falius.communication.core.bluetooth.devicesearch;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;

/**
 * 付近からBluetooth機器を探索するクラスです。
 * @author "ymiyauchi"
 *
 */
public class DeviceSearcher implements AutoCloseable {
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final CountDownLatch mDeviceLatch = new CountDownLatch(1);
    private final DiscoveryAgent mAgent;
    private final DeviceDiscovery mDiscovery;
    private boolean mIsExecuted = false;

    public DeviceSearcher() throws BluetoothStateException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        mAgent = localDevice.getDiscoveryAgent();
        mDiscovery = new DeviceDiscovery(mDeviceLatch);
    }

    /**
     * ペアリング済のデバイスを付近から検索し、結果の集合をもつFutureを返します。
     * @return
     * @throws BluetoothStateException
     */
    public Future<Set<RemoteDevice>> searchPairedDevice() throws BluetoothStateException {
        if (mIsExecuted) {
            throw new IllegalStateException("searchDevice() can not be executed again");
        }
        mIsExecuted = true;

        Callable<Set<RemoteDevice>> callable = new Callable<Set<RemoteDevice>>() {

            @Override
            public Set<RemoteDevice> call() throws Exception {
            mAgent.startInquiry(DiscoveryAgent.GIAC, mDiscovery);
            mDeviceLatch.await();
            return Collections.unmodifiableSet(mDiscovery.mDevices);
            }

        };
        return mExecutor.submit(callable);
    }

    @Override
    public void close() {
        mExecutor.shutdown();
    }
}
