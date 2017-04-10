package jp.gr.java_conf.falius.communication.core.bluetooth;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

class UrlDiscovery implements DiscoveryListener {
    private final CountDownLatch mServiceLatch;
    final Set<String> mUrls = new HashSet<>();

    UrlDiscovery(CountDownLatch serviceLatch) {
        mServiceLatch = serviceLatch;
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {
            String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            mUrls.add(url);
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        mServiceLatch.countDown();
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) { /* empty */ }

    @Override
    public void inquiryCompleted(int discType) { /* empty */ }

    static String getUrl(RemoteDevice device, String uuid) throws BluetoothStateException, InterruptedException {
        // デバイス探索と比べるとURLの取得にはそこまで時間がかからないため同期実行
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        CountDownLatch latch = new CountDownLatch(1);
        UrlDiscovery discovery = new UrlDiscovery(latch);

        UUID[] uuidSet = new UUID[] { new UUID(uuid, false) };
        agent.searchServices(null, uuidSet, device, discovery);
        latch.await();
        for (String url : discovery.mUrls) {
            return url;
        }
        throw new BluetoothStateException("Device does not support Simple SPP Service." );
    }
}
