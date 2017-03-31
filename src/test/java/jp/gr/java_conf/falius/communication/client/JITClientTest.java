package jp.gr.java_conf.falius.communication.client;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.server.NonBlockingServer;
import jp.gr.java_conf.falius.communication.server.Server;
import jp.gr.java_conf.falius.communication.server.SocketServer;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;
import jp.gr.java_conf.falius.util.check.CheckList;
import jp.gr.java_conf.falius.util.range.IntRange;

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
        mServer.addOnReceiveListener(new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                log.debug("server on receive");
            }

            @Override
            public String toString() {
                return "server receive listener@JITClientTest";
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
        CheckList<String> list = new CheckList<>(data);
        try (JITClient client = new JITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("ret: {}", ret);
                list.check(ret);
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
    public void testMatchCall() throws InterruptedException, IOException {
        // スレッドの数、submitした数だけ接続が確立し、JITClientにsendされたデータを協力しながら処理される
        String[] data = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r" };
        CheckList<String> list = new CheckList<>(data);
        CountDownLatch signal = new CountDownLatch(data.length);
        try (JITClient client = new JITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("receive by {}", Thread.currentThread().getName());
                log.debug("ret: {}", ret);
                assertThat(list.isChecked(ret), is(false));
                list.check(ret);
                assertThat(ret, isIn(data));
                signal.countDown();
            }

        })) {
            ExecutorService executor = Executors.newFixedThreadPool(3);
            for (int i : new IntRange(10)) {
                executor.submit(client);
            }

            for (String s : data) {
                SendData sendData = new BasicSendData();
                sendData.put(s);
                client.send(sendData);
                Thread.sleep(10);
            }
            signal.await(); // すべて受信する前にcloseさせないため
        }

        assertThat(list.isCheckedAll(), is(true));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStart() throws InterruptedException, IOException {
        String[] data = { "a", "b", "c", "d", "e", "f", "g" };
        try (JITClient client = new JITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("receive by {}", Thread.currentThread().getName());
                log.debug("ret: {}", ret);
                assertThat(ret, isIn(data));
            }

        })) {
            client.start(new RepeatSwapper() {

                @Override
                public SendData swap(String remoteAddress, ReceiveData receiveData) throws Exception {
                    SendData sendData = new BasicSendData();
                    sendData.put("data");
                    return sendData;
                }

            });

            for (String s : data) {
                SendData sendData = new BasicSendData();
                sendData.put(s);
                client.send(sendData);
                Thread.sleep(10);
            }
        }
    }

    @Test
    public void testAddOnSendListener() throws IOException, InterruptedException, TimeoutException {
        CheckList<String> check = new CheckList<>("check");
        String[] data = { "a", "b", "c", "d", "e", "f", "g" };
        CheckList<String> list = new CheckList<>(data);
        CountDownLatch signal = new CountDownLatch(data.length);
        try (JITClient client = new JITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("receive by {}", Thread.currentThread().getName());
                log.debug("ret: {}", ret);
                assertThat(list.isChecked(ret), is(false));
                list.check(ret);
                assertThat(ret, isIn(data));
                signal.countDown();
            }

        })) {
            client.addOnSendListener(new OnSendListener() {

                @Override
                public void onSend(int writeBytes) {
                    check.check(0);
                    assertThat(writeBytes, is(4 + 4 + 4 + 1));
                }

            });
            client.startOnNewThread();

            for (String s : data) {
                SendData sendData = new BasicSendData();
                sendData.put(s);
                client.send(sendData);
                Thread.sleep(10);
            }
            signal.await();
        }

        assertThat(list.isCheckedAll(), is(true));
        assertThat(check.isChecked("check"), is(true));
    }

    @Test
    public void testAddOnReceiveListener() throws IOException, TimeoutException, InterruptedException {
        String[] data = { "a", "b", "c", "d", "e", "f", "g" };
        CountDownLatch signal = new CountDownLatch(data.length);
        CheckList<String> list = new CheckList<>(data);
        CheckList<String> check = new CheckList<>("check");
        try (JITClient client = new JITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                assertThat(true, is(false)); // 一度でも通ったら失敗
                String ret = receiveData.getString();
                log.debug("ret: {}", ret);
                assertThat(list.isChecked(ret), is(false));
                list.check(ret);
                assertThat(ret, isIn(data));
            }

            @Override
            public String toString() {
                return "old receive listener@testAddOnReceiveListener";
            }

        })) {
            client.addOnReceiveListener(new OnReceiveListener() {

                @Override
                public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                    log.debug("receive listener in new");
                    check.check(0); // 一度でも通ったことの確認
                    String ret = receiveData.getString();
                    log.debug("receive by {}", Thread.currentThread().getName());
                    log.debug("ret: {}", ret);
                    assertThat(list.isChecked(ret), is(false));
                    list.check(ret);
                    assertThat(ret, isIn(data));
                    signal.countDown();
                }

                @Override
                public String toString() {
                    return "new receive listener@testAddOnReceiveListener";
                }

            });
            client.startOnNewThread();

            for (String s : data) {
                SendData sendData = new BasicSendData();
                sendData.put(s);
                client.send(sendData);
                Thread.sleep(10);
            }
            signal.await();
        }

        assertThat(check.isChecked("check"), is(true));
        assertThat(list.isCheckedAll(), is(true));
    }

    @Test
    public void testAddOnDisconnectCallback() throws IOException, TimeoutException, InterruptedException {
        String[] data = { "a", "b", "c", "d", "e", "f", "g" };
        CheckList<String> list = new CheckList<>(data);
        CheckList<String> check = new CheckList<>("check");
        try (JITClient client = new JITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, int readByte, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("ret: {}", ret);
                assertThat(list.isChecked(ret), is(false));
                list.check(ret);
                assertThat(ret, isIn(data));
            }

        })) {
            client.addOnDisconnectCallback(new OnDisconnectCallback() {

                @Override
                public void onDissconnect(String remote, Throwable cause) {
                    assertThat(check.isChecked("check"), is(false));
                    log.debug("client disconnect by {}", cause == null ? "null" : cause.getMessage());
                    check.check(0);
                }

            });
            client.startOnNewThread();

            for (String s : data) {
                SendData sendData = new BasicSendData();
                sendData.put(s);
                client.send(sendData);
                Thread.sleep(10);
                client.close();  // 送信途中でcloseすることでdisconnectさせる
            }
        }

        assertThat(check.isChecked("check"), is(true));
    }

}