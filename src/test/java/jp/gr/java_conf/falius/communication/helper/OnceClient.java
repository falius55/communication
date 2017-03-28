package jp.gr.java_conf.falius.communication.helper;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.client.Client;
import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;

public class OnceClient implements ClientHelper {
    private final String mHost;
    private final int mPort;

    public OnceClient(String host, int port){
        mHost = host;
        mPort = port;
    }

    @SuppressWarnings("unchecked")
    public <T> ReceiveData send(T... sendData) throws IOException, TimeoutException {
        Client client = new NonBlockingClient(mHost, mPort);
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
