package jp.gr.java_conf.falius.communication.test.helper;

import java.io.IOException;

import jp.gr.java_conf.falius.communication.core.Server;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingServer;
import jp.gr.java_conf.falius.communication.core.socket.SocketServer;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

public class EchoServer implements ServerHelper {
    private static int mPort = 9001;
    private final SocketServer mServer;

    public EchoServer() {
        mServer = init();
    }

    private SocketServer init() {
        synchronized (EchoServer.class) {
            SocketServer server = new NonBlockingServer(mPort++, new SwapperFactory() {

                @Override
                public Swapper get() {
                    return new OnceSwapper() {

                        @Override
                        public SendData swap(String remoteAddress, ReceiveData receiveData) {
                            SendData data = new BasicSendData();
                            data.put(receiveData.getAll());
                            return data;
                        }

                    };
                }
            });
            return server;
        }
    }

    @Override
    public void beforeClass() throws IOException {

        mServer.addOnDisconnectCallback(new OnDisconnectCallback() {

            @Override
            public void onDissconnect(String remote, Throwable cause) {
                System.out.printf("server disconnect with %s by %s%n", remote, cause == null ? "null" : cause);
            }

        });

        mServer.addOnShutdownCallback(new Server.OnShutdownCallback() {

            @Override
            public void onShutdown() {
                System.out.println("server shutdown");
            }

        });

        mServer.addOnAcceptListener(new Server.OnAcceptListener() {

            @Override
            public void onAccept(String remoteAddress) {
                System.out.printf("server accept from %s%n", remoteAddress);
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

    @Override
    public String getAddress() {
        return mServer.getLocalHostAddress();
    }
}
