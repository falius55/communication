package jp.gr.java_conf.falius.communication.test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
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

import jp.gr.java_conf.falius.communication.core.Client;
import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.test.helper.EchoServer;
import jp.gr.java_conf.falius.communication.test.helper.ServerHelper;
import jp.gr.java_conf.falius.util.check.CheckList;
import jp.gr.java_conf.falius.util.range.IntRange;

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
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());

        ReceiveData receiveData = client.start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                assertThat(receiveData, is(nullValue()));
                SendData data = new BasicSendData();
                data.put(sendData);
                return data;
            }

        });

        String ret = receiveData.getString();
        assertThat(ret, is(sendData));
    }

    @Test
    public void testAddOnSendListener() throws IOException, TimeoutException {
        String sendData = "send data";
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        CheckList<String> check = new CheckList<>("check");
        client.addOnSendListener(new OnSendListener() {

            @Override
            public void onSend(String remoteAddress) {
                // 送信データ + ヘッダーサイズなので、送信データと同じにはならない。
                check.check("check");
            }

        });

        client.start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                SendData data = new BasicSendData();
                data.put(sendData);
                return data;
            }

        });
        assertThat(check.isChecked("check"), is(true));
    }

    @Test
    public void testAddOnReceiveListener() throws IOException, TimeoutException {
        String[] sendData = { "data1", "data2", "data3", "data4" };
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        client.addOnReceiveListener(new OnReceiveListener() {

            @Override
            public void onReceive(String fromAddress, ReceiveData receiveData) {
                assertThat(receiveData.getString(), is(sendData[0]));
                assertThat(receiveData.getString(), is(sendData[1]));
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

        ReceiveData receiveData = client.start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                SendData data = new BasicSendData();
                for (String s : sendData) {
                    data.put(s);
                }
                return data;
            }

        });

        assertThat(receiveData.getString(), is(sendData[2]));
        assertThat(receiveData.getString(), is(sendData[3]));
    }

    @Test
    public void testAddOnConnectListener() throws IOException, TimeoutException {
        String[] sendData = { "data1", "data2", "data3", "data4" };
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        CheckList<String> check = new CheckList<>("check");
        client.addOnConnectListener(new Client.OnConnectListener() {

            @Override
            public void onConnect(String remoteAddress) {
                check.check("check");
                //                assertThat(remoteAddress, is(mServer.getAddress()));
            }
        });

        ReceiveData receiveData = client.start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                SendData data = new BasicSendData();
                for (String s : sendData) {
                    data.put(s);
                }
                return data;
            }

        });

        assertThat(check.isChecked("check"), is(true));
    }

    @Test
    public void testCall() throws InterruptedException, ExecutionException {
        String[] sendData = { "abc", "def", "ghi", "jkl" };
        Client client = new NonBlockingClient(HOST, mServer.getPort(), new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                assertThat(receiveData, is(nullValue()));
                SendData data = new BasicSendData();
                for (String s : sendData) {
                    data.put(s);
                }
                return data;
            }

        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReceiveData> future = executor.submit(client);
        ReceiveData receiveData = future.get();
        for (String data : sendData) {
            assertThat(receiveData.getString(), is(data));
        }
    }

    @Test
    public void testMultiCall() throws InterruptedException, ExecutionException {
        final int THREADPOOL_COUNT = 3;
        final int TASK_COUNT = 15;
        String[] sendData = { "a", "de", "ghi", "jklm" };
        Client client = new NonBlockingClient(HOST, mServer.getPort(), new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                assertThat(receiveData, is(nullValue()));
                SendData data = new BasicSendData();
                for (int i : new IntRange(sendData.length)) {
                    data.put(sendData[i]);
                }
                return data;
            }

        });

        List<Future<ReceiveData>> futures = new LinkedList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_COUNT);

        for (int i : new IntRange(TASK_COUNT)) {
            Future<ReceiveData> future = executor.submit(client);
            futures.add(future);
        }

        for (Future<ReceiveData> future : futures) {
            ReceiveData receiveData = future.get();
            assertThat(receiveData.dataCount(), is(sendData.length));
            for (int i : new IntRange(sendData.length)) {
                log.info("data: {} : {}", i, sendData[i]);
                String ret = receiveData.getString();
                log.info("ret : {} : {}", i, ret);
                assertThat(ret, is(sendData[i]));
            }
            assertThat(future.isDone(), is(true));
        }
    }

    @Test
    public void testMuchData() throws IOException, TimeoutException {
        String sendData = "sendData";
        int len = 10000;
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());

        ReceiveData receiveData = client.start(new OnceSwapper() {

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                assertThat(receiveData, is(nullValue()));
                SendData data = new BasicSendData();
                new IntRange(len).simpleForEach(() -> {
                    data.put(sendData);
                });
                return data;
            }

        });

        assertThat(receiveData.dataCount(), is(len));

        receiveData.clear();
        assertThat(receiveData.dataCount(), is(0));
    }

    @Test
    public void testSend() throws IOException, TimeoutException {
        String sendData = "data";
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData data = new BasicSendData();
        data.put(sendData);
        ReceiveData receiveData = client.send(data);
        assertThat(receiveData.getString(), is(sendData));
    }

    @Test
    public void testMultiSend() {
        String sendData = "data";
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        client.addOnDisconnectCallback(new OnDisconnectCallback() {

            @Override
            public void onDissconnect(String remote, Throwable cause) {
                log.debug("start sender disconnect to {} by {}", remote, cause == null ? "null" : cause);
            }

        });
        new IntRange(5).forEach((i) -> {
            ReceiveData receiveData;
            try {
                SendData data = new BasicSendData();
                data.put(sendData + i);
                receiveData = client.send(data);
            } catch (IOException | TimeoutException e) {
                throw new IllegalStateException();
            }
            assertThat(receiveData.getString(), is(sendData + i));
        });
    }

    @Test
    public void testChaengeReceiveListener() throws IOException {
        String[] data = { "data1", "data2", "data3" };
        try (SwapClient client = new NonBlockingClient(HOST, mServer.getPort())) {
            new IntRange(data.length).forEach(i -> {
                client.addOnReceiveListener(new OnReceiveListener() {

                    @Override
                    public void onReceive(String fromAddress, ReceiveData receiveData) {
                        assertThat(receiveData.getString(), is(data[i]));
                    }
                });
                SendData sendData = new BasicSendData();
                sendData.put(data[i]);
                try {
                    client.send(sendData);
                } catch (IOException | TimeoutException e) {
                    assertThat(false, is(true));
                }
            });
        }
    }
}
