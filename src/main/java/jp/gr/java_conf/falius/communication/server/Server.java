package jp.gr.java_conf.falius.communication.server;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;

public interface Server extends Callable<Throwable>, AutoCloseable {

    Future<?> startOnNewThread();

    void shutdown() throws IOException;

    void addOnSendListener(OnSendListener listener);

    void addOnReceiveListener(OnReceiveListener listener);

    void addOnAcceptListener(Server.OnAcceptListener callback);

    void addOnShutdownCallback(Server.OnShutdownCallback callback);

    void close() throws IOException;

    interface OnAcceptListener {

        void onAccept(String remoteAddress);
    }

    interface OnShutdownCallback {
        void onShutdown();
    }

}
