package jp.gr.java_conf.falius.communication.client;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.server.NonBlockingServer;
import jp.gr.java_conf.falius.communication.server.Server;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public class NonBlockingClientTest {
    private static Logger log = LoggerFactory.getLogger(NonBlockingClientTest.class);
    private static final String HOST = "localhost";
    private static final int PORT = 9101;
    private static Server mServer;

    @BeforeClass
    public static void setupServer() throws IOException {
        mServer = new NonBlockingServer(PORT, new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public Sender swap(String remoteAddress, Receiver receiver) {
                        Sender sender = new MultiDataSender();
                        sender.put(receiver.getAll());
                        log.debug("server swapper");
                        return sender;
                    }

                };
            }

        });

        mServer.addOnDisconnectCallback(new OnDisconnectCallback() {

            @Override
            public void onDissconnect(String remote, Throwable cause) {
                log.debug("server disconnect with {}", remote);
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

    @AfterClass
    public static void shutdownServer() throws IOException {
        mServer.close();
    }

    @Test
    public void testStart() throws IOException, TimeoutException {
        String sendData = "sendData";
        Client client = new NonBlockingClient(HOST, PORT);

        Receiver receiver = client.start(new OnceSwapper() {

            @Override
            public Sender swap(String remoteAddress, Receiver receiver) {
                assertThat(receiver, is(nullValue()));
                Sender sender = new MultiDataSender();
                sender.put(sendData);
                return sender;
            }

        });

        String ret = receiver.getString();
        assertThat(ret, is(sendData));
    }

    @Test
    public void testAddOnSendListener() throws IOException, TimeoutException {
        String sendData = "send data";
        Client client = new NonBlockingClient(HOST, PORT);
        client.addOnSendListener(new OnSendListener() {

            @Override
            public void onSend(int writeSize) {
                // 送信データ + ヘッダーサイズなので、送信データと同じにはならない。
                assertThat(writeSize, is(greaterThan(sendData.getBytes().length)));
            }

        });

        client.start(new OnceSwapper() {

            @Override
            public Sender swap(String remoteAddress, Receiver receiver) {
                Sender sender = new MultiDataSender();
                sender.put(sendData);
                return sender;
            }

        });
    }

    @Test
    public void testAddOnReceiveListener() throws IOException, TimeoutException {
        String[] sendData = { "data1", "data2", "data3", "data4" };
        Client client = new NonBlockingClient(HOST, PORT);
        client.addOnReceiveListener(new OnReceiveListener() {

            @Override
            public void onReceive(String fromAddress, int readByte, Receiver receiver) {
                String ret1 = receiver.getString();
                log.debug("ret1 : {}", ret1);
                assertThat(ret1, is(sendData[0]));
                String ret2 = receiver.getString();
                log.debug("ret2 : {}", ret2);
                assertThat(ret2, is(sendData[1]));
            }
        });

        client.addOnDisconnectCallback(new OnDisconnectCallback() {

            @Override
            public void onDissconnect(String remote, Throwable cause) {
                if (Objects.nonNull(cause)) {
                    log.info("client disconnect : {}", cause.getMessage());
                }
            }

        });

        Receiver receiver = client.start(new OnceSwapper() {

            @Override
            public Sender swap(String remoteAddress, Receiver receiver) {
                Sender sender = new MultiDataSender();
                for (String data : sendData) {
                    sender.put(data);
                }
                return sender;
            }

        });

        String ret3 = receiver.getString();
        log.debug("ret3 : {}", ret3);
        assertThat(ret3, is(sendData[2]));
        String ret4 = receiver.getString();
        log.debug("ret4 : {}", ret4);
        assertThat(ret4, is(sendData[3]));
    }

    @Test
    public void testCall() {
    }

}
