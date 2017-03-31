package jp.gr.java_conf.falius.communication.client;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.remote.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.server.NonBlockingServer;
import jp.gr.java_conf.falius.communication.server.Server;
import jp.gr.java_conf.falius.communication.server.SocketServer;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

public class JITClientTest {
    private static final Logger log = LoggerFactory.getLogger(JITClientTest.class);
    private static final String HOST = "localhost";
    private static final int PORT = 10000;
    private static SocketServer mServer;

    @BeforeClass
    public static void setupServer() throws IOException {
        mServer = new NonBlockingServer(PORT, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new RepeatSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiveData) throws Exception {
                        SendData sendData = new BasicSendData();
                        sendData.put(receiveData.getAll());
                        return sendData;
                    }

                };
            }

        });
        mServer.addOnDisconnectCallback(new OnDisconnectCallback() {

            @Override
            public void onDissconnect(String remote, Throwable cause) {
                log.debug("server disconnect {} by {}", remote, cause == null ? "null" : cause.getMessage());
            }

        });
        mServer.addOnShutdownCallback(new Server.OnShutdownCallback() {

            @Override
            public void onShutdown() {
                log.debug("server shutdown");
            }

        });
        mServer.startOnNewThread();
    }

    @AfterClass
    public static void shutdownServer() throws IOException {
        mServer.close();
    }

    @Test
    public void testSend() throws IOException, TimeoutException, Exception {
        String[] data = { "a", "b", "c", "d", "e", "f", "g" };
        try (JITClient client = new JITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("ret: {}", ret);
                assertThat(ret, isIn(data));
            }

        })) {
            client.startOnNewThread();

            for (String s : data) {
                SendData sendData = new BasicSendData();
                sendData.put(s);
                client.send(sendData);
                Thread.sleep(100);
            }
        }
    }

    @Test
    public void testStartOnNewThread() {
    }

    @Test
    public void testCall() {
    }

    @Test
    public void testStart() {
    }

    @Test
    public void testAddOnSendListener() {
    }

    @Test
    public void testAddOnReceiveListener() {
    }

    @Test
    public void testAddOnDisconnectCallback() {
    }

}
