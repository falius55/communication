package jp.gr.java_conf.falius.communication.client;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.helper.EchoServer;
import jp.gr.java_conf.falius.communication.helper.ServerHelper;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public class NonBlockingClientTest {
    private static Logger log = LoggerFactory.getLogger(NonBlockingClientTest.class);
    private static final String HOST = "localhost";
    private static final ServerHelper mServer = new EchoServer();

    @BeforeClass
    public static void setupServer() throws IOException {
        mServer.beforeClass();
    }

    @AfterClass
    public static void shutdownServer() throws IOException {
        mServer.afterClass();
    }

    @Test
    public void testStart() throws IOException, TimeoutException {
        String sendData = "sendData";
        Client client = new NonBlockingClient(HOST, mServer.getPort());

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
        Client client = new NonBlockingClient(HOST, mServer.getPort());
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
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        client.addOnReceiveListener(new OnReceiveListener() {

            @Override
            public void onReceive(String fromAddress, int readByte, Receiver receiver) {
                assertThat(receiver.getString(), is(sendData[0]));
                assertThat(receiver.getString(), is(sendData[1]));
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

        assertThat(receiver.getString(), is(sendData[2]));
        assertThat(receiver.getString(), is(sendData[3]));
    }

    @Test
    public void testCall() throws InterruptedException, ExecutionException {
        String[] sendData = {"abc", "def", "ghi", "jkl"};
        Client client = new NonBlockingClient(HOST, mServer.getPort(), new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public Sender swap(String remoteAddress, Receiver receiver) {
                        assertThat(receiver, is(nullValue()));
                        Sender sender = new MultiDataSender();
                        for (String data : sendData) {
                            sender.put(data);
                        }
                        return sender;
                    }

                };
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Receiver> future = executor.submit(client);
        Receiver receiver = future.get();
        for (String data : sendData) {
            assertThat(receiver.getString(), is(data));
        }
    }

}
