package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.Disconnectable;
import jp.gr.java_conf.falius.communication.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public interface Client extends Callable<Receiver>, Disconnectable {

    Receiver start(Swapper sender) throws IOException, TimeoutException;

    void addOnSendListener(OnSendListener listener);

    void addOnReceiveListener(OnReceiveListener listener);

    void addOnDisconnectCallback(OnDisconnectCallback callback);

}
