package jp.gr.java_conf.falius.communication.test.helper;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;

public class OnceClient implements ClientHelper {
    private static final Logger log = LoggerFactory.getLogger(OnceClient.class);
    private final String mHost;
    private final int mPort;

    public OnceClient(String host, int port){
        mHost = host;
        mPort = port;
    }

    @SuppressWarnings("unchecked")
    public <T> ReceiveData send(final T... sendData) throws IOException, TimeoutException {
        SwapClient client = new NonBlockingClient(mHost, mPort);
        client.addOnDisconnectCallback(new OnDisconnectCallback() {

            @Override
            public void onDissconnect(String remote, Throwable cause) {
                log.debug("client disconnect by {}", cause == null ? "non" : cause.getMessage());
            }

        });
        return client.start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiver) {
                SendData data = new BasicSendData();
                for (T d : sendData) {
                    if (d instanceof String) {
                        data.put((String) d);
                    } else if (d instanceof Integer) {
                        data.put(((Integer) d).intValue());
                    }
                }
                return data;
            }

        });
    }
}
