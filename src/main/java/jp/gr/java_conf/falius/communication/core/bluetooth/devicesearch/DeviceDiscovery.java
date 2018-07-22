package jp.gr.java_conf.falius.communication.core.bluetooth.devicesearch;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 *
 * @author "ymiyauchi"
 * @since 1.5.0
 *
 */
class DeviceDiscovery implements DiscoveryListener {
    private final CountDownLatch mDeviceLatch;
    final Set<RemoteDevice> mDevices = new HashSet<>();

    /**
     *
     * @param deviceLatch
     * @since 1.5.0
     */
    DeviceDiscovery(CountDownLatch deviceLatch) {
        mDeviceLatch = deviceLatch;
    }

    /**
     * @since 1.5.0
     */
    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        mDevices.add(btDevice);
    }

    /**
     * @since 1.5.0
     */
    @Override
    public void inquiryCompleted(int discType) {
        mDeviceLatch.countDown();
    }

    //implement this method since services are not being discovered
    /**
     * @since 1.5.0
     */
    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) { /* empty */ }

    //implement this method since services are not being discovered
    /**
     * @since 1.5.0
     */
    @Override
    public void serviceSearchCompleted(int transID, int respCode) { /* empty */ }
}
