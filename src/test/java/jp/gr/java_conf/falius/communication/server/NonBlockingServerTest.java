package jp.gr.java_conf.falius.communication.server;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.client.Client;
import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.helper.ClientHelper;
import jp.gr.java_conf.falius.communication.helper.OnceClient;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.FixedRepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

public class NonBlockingServerTest {
    private static Logger log = LoggerFactory.getLogger(NonBlockingServerTest.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8999;

    @Test
    public void testReceiveValueInSwapper() throws IOException, TimeoutException {
        final String[] data = { "1", "2", "3" };
        try (Server server = new NonBlockingServer(PORT, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiveData) {
                        log.info("swap from {}", remoteAddress);
                        assertThat(receiveData, is(not(nullValue())));
                        SendData sender = new BasicSendData();
                        for (String d : data) {
                            String rev = receiveData.getString();
                            assertThat(rev, is(d));
                            sender.put(Integer.parseInt(rev));
                        }
                        assertThat(receiveData.getString(), is(nullValue()));
                        return sender;
                    }
                };
            }
        })) {
            server.startOnNewThread();

            ClientHelper client = new OnceClient(HOST, PORT);
            ReceiveData ret = client.send(data);
            assertThat(ret, is(not(nullValue())));
            assertThat(ret.dataCount(), is(data.length));
            for (String d : data) {
                assertThat(ret.getInt(), is(Integer.parseInt(d)));
            }
            assertThat(ret.get(), is(nullValue()));
        }
    }

    @Test
    public void testAddOnReceiveListener() throws IOException, TimeoutException {
        final String[] receiveData = { "data1", "data2", "data3" };
        try (Server server = new NonBlockingServer(PORT, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiver) {
                        log.info("swap from {}", remoteAddress);
                        assertThat(receiver, is(not(nullValue())));
                        assertThat(receiver.get(), is(nullValue()));
                        SendData sender = new BasicSendData();
                        return sender;
                    }
                };
            }
        })) {
            server.addOnReceiveListener(new OnReceiveListener() {

                @Override
                public void onReceive(String fromAddress, int readByte, ReceiveData receiver) {
                    assertThat(receiver, is(not(nullValue())));
                    SendData sender = new BasicSendData();
                    for (String d : receiveData) {
                        String rev = receiver.getString();
                        assertThat(rev, is(d));
                        sender.put(rev);
                    }
                    assertThat(receiver.getString(), is(nullValue()));
                }
            });
            server.startOnNewThread();

            ClientHelper client = new OnceClient(HOST, PORT);
            client.send(receiveData);
        }
    }

    @Test
    public void testCall() throws IOException, TimeoutException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = null;
        final String[] receiveData = { "1", "2", "3" };
        try (Server server = new NonBlockingServer(PORT, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiver) {
                        log.info("swap from {}", remoteAddress);
                        assertThat(receiver, is(not(nullValue())));
                        SendData sender = new BasicSendData();
                        for (String data : receiveData) {
                            String rev = receiver.getString();
                            assertThat(rev, is(data));
                            sender.put(Integer.parseInt(rev));
                        }
                        assertThat(receiver.getString(), is(nullValue()));
                        return sender;
                    }
                };
            }
        })) {
            future = executor.submit(server);

            ClientHelper client = new OnceClient(HOST, PORT);
            client.send(receiveData);
        }
        assertThat(future, is(not(nullValue())));

        executor.shutdown();

        Object ret = future.get();
        assertThat(ret, is(nullValue()));
        assertThat(future.isDone(), is(true));
    }

    @Test
    public void testRepeatSwapper() throws IOException, TimeoutException {
        final int repeatLen = 10;
        int port = 8998;
        try (Server server = new NonBlockingServer(port, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new RepeatSwapper() {
                    private int count = 0;

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiveData) {
                        int rcv = receiveData.getInt();
                        SendData sendData = new BasicSendData();
                        sendData.put(rcv + 1);

                        count++;
                        if (count == 10) {
                            finish();
                            // このデータを送信したら終了
                        }
                        return sendData;
                    }

                };
            }
        })) {
            server.startOnNewThread();

            Client client = new NonBlockingClient(HOST, port);
            ReceiveData receiveData = client.start(new RepeatSwapper() {
                private int count = 0;

                @Override
                public SendData swap(String remoteAddress, ReceiveData receiveData) {
                    SendData sendData = new BasicSendData();
                    if (count == 0) {
                        assertThat(receiveData, is(nullValue()));
                        sendData.put(0);
                    } else {
                        int rcv = receiveData.getInt();
                        sendData.put(rcv + 1);
                    }

                    count++;
                    if (count == repeatLen) {
                        finish();
                        // このデータを送信して、最後に受信して終わり
                        // nullをここで返すと直後に切断されてしまい、最後の送受信が行われない。
                    }
                    return sendData;
                }

            });

            assertThat(receiveData.getInt(), is(repeatLen * 2 - 1));
        }
    }

    @Test
    public void testFixedRepeatSwapper() throws IOException, TimeoutException {
        final int repeatLen = 10;
        int port = 8998;
        try (Server server = new NonBlockingServer(port, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new FixedRepeatSwapper(repeatLen) {

                    @Override
                    public SendData onSwap(String remoteAddress, ReceiveData receiveData) {
                        int rcv = receiveData.getInt();
                        SendData sendData = new BasicSendData();
                        sendData.put(rcv + 1);
                        return sendData;
                    }
                };
            }
        })) {
            server.startOnNewThread();

            Client client = new NonBlockingClient(HOST, port);
            ReceiveData result = client.start(new FixedRepeatSwapper(repeatLen) {

                @Override
                public SendData onSwap(String remoteAddress, ReceiveData receiveData) {
                    SendData sendData = new BasicSendData();
                    if (receiveData == null) {
                        sendData.put(0);
                    } else {
                        int rcv = receiveData.getInt();
                        sendData.put(rcv + 1);
                    }

                    return sendData;
                }

            });

            assertThat(result.getInt(), is(repeatLen * 2 - 1));
        }
    }
}
