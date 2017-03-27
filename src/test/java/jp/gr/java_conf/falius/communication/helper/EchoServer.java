package jp.gr.java_conf.falius.communication.helper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.receiver.ReceiveData;
import jp.gr.java_conf.falius.communication.sender.SendData;
import jp.gr.java_conf.falius.communication.sender.SendQueue;
import jp.gr.java_conf.falius.communication.server.NonBlockingServer;
import jp.gr.java_conf.falius.communication.server.Server;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public class EchoServer implements ServerHelper {
    private static final Logger log = LoggerFactory.getLogger(EchoServer.class);
    private static int mPort = 9001;
    private final Server mServer;

    public EchoServer() {
        mServer = init();
    }

    private Server init() {
        return new NonBlockingServer(mPort++, new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiveData) {
                        SendData data = new SendQueue();
                        data.put(receiveData.getAll());
                        return data;
                    }

                };
            }
        });
    }

    @Override
    public void beforeClass() throws IOException {

        mServer.addOnDisconnectCallback(new OnDisconnectCallback() {

            @Override
            public void onDissconnect(String remote, Throwable cause) {
                log.debug("server disconnect with {} by {}", remote, cause == null ? "null" : cause);
            }

        });

        mServer.addOnShutdownCallback(new Server.OnShutdownCallback() {

            @Override
            public void onShutdown() {
                log.info("server shutdown");
            }

        });

        mServer.addOnAcceptListener(new Server.OnAcceptListener() {

            @Override
            public void onAccept(String remoteAddress) {
                log.debug("server accept from {}", remoteAddress);
            }
        });

        mServer.startOnNewThread();

    }

    @Override
    public void afterClass() throws IOException {
        mServer.close();
    }

    @Override
    public int getPort() {
        return mServer.getPort();
    }
}
