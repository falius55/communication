package jp.gr.java_conf.falius.communication.helper;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.client.Client;
import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;

public class OnceClient implements ClientHelper {
    private final String mHost;
    private final int mPort;

    public OnceClient(String host, int port){
        mHost = host;
        mPort = port;
    }

    @SuppressWarnings("unchecked")
    public <T> Receiver send(T... sendData) throws IOException, TimeoutException {
        Client client = new NonBlockingClient(mHost, mPort);
        return client.start(new OnceSwapper() {

            @Override
            public Sender swap(String remoteAddress, Receiver receiver) {
                Sender sender = new MultiDataSender();
                for (T data : sendData) {
                    if (data instanceof String) {
                        sender.put((String) data);
                    } else if (data instanceof Integer) {
                        sender.put(((Integer) data).intValue());
                    }
                }
                return sender;
            }

        });
    }
}
