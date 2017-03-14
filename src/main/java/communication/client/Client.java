package communication.client;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import communication.Swapper;
import communication.receiver.OnReceiveListener;
import communication.receiver.Receiver;
import communication.sender.OnSendListener;

public interface Client extends Callable<Receiver> {

    Receiver start(Swapper sender) throws IOException, TimeoutException;

    void addOnSendListener(OnSendListener listener);

    void addOnReceiveListener(OnReceiveListener listener);

}
