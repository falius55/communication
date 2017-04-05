package jp.gr.java_conf.falius.communication.test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
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

import jp.gr.java_conf.falius.communication.core.JITClient;
import jp.gr.java_conf.falius.communication.core.Server;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingJITClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingServer;
import jp.gr.java_conf.falius.communication.core.socket.SocketServer;
import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;
import jp.gr.java_conf.falius.util.check.CheckList;
import jp.gr.java_conf.falius.util.range.IntRange;

public class NonBlockingJITClientTest {
    private static final Logger log = LoggerFactory.getLogger(NonBlockingJITClientTest.class);
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
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
                log.debug("server on receive");
            }

            @Override
            public String toString() {
                return "server receive listener@NonBlockingJITClientTest";
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
        try (JITClient client = new NonBlockingJITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
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
    public void testMatchCall() throws InterruptedException, IOException, TimeoutException {
        // スレッドの数、submitした数だけ接続が確立し、JITClientにsendされたデータを協力しながら処理される
        String[] data = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r" };
        CheckList<String> list = new CheckList<>(data);
        CountDownLatch signal = new CountDownLatch(data.length);
        try (JITClient client = new NonBlockingJITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
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

    @Test
    public void testAddOnSendListener() throws IOException, InterruptedException, TimeoutException {
        CheckList<String> check = new CheckList<>("check");
        String[] data = { "a", "b", "c", "d", "e", "f", "g" };
        CheckList<String> list = new CheckList<>(data);
        CountDownLatch signal = new CountDownLatch(data.length);
        try (JITClient client = new NonBlockingJITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
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
                public void onSend(String remoteAddress) {
                    check.check("check");
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
        try (JITClient client = new NonBlockingJITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
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
                public void onReceive(String remoteAddress, ReceiveData receiveData) {
                    log.debug("receive listener in new");
                    check.check("check"); // 一度でも通ったことの確認
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
        try (JITClient client = new NonBlockingJITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
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
                    check.check("check");
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

    @Test
    public void testMultiStart() throws IOException, TimeoutException, InterruptedException, ExecutionException {
        // スレッドの数、submitした数だけ接続が確立し、JITClientにsendされたデータを協力しながら処理される
        String[] data = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r" };
        CheckList<String> list = new CheckList<>(data);
        CountDownLatch signal = new CountDownLatch(data.length);
        try (JITClient client = new NonBlockingJITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("receive by {}", Thread.currentThread().getName());
                log.debug("ret: {}", ret);
                assertThat(list.isChecked(ret), is(false));
                list.check(ret);
                assertThat(ret, isIn(data));
                signal.countDown();
            }

        })) {
            for (int i : new IntRange(10)) {
                client.startOnNewThread();
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

    @Test
    public void testCloseNotThrow() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        // closeメソッドによって割り込みが発生しても正常終了することの確認
        String[] data
        = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
        CheckList<String> list = new CheckList<>(data);
        CountDownLatch signal = new CountDownLatch(data.length);
        Set<Future<ReceiveData>> futures = new HashSet<>();
        try (JITClient client = new NonBlockingJITClient(HOST, mServer.getPort(), new OnReceiveListener() {

            @Override
            public void onReceive(String remoteAddress, ReceiveData receiveData) {
                String ret = receiveData.getString();
                log.debug("receive by {}", Thread.currentThread().getName());
                log.debug("ret: {}", ret);
                assertThat(list.isChecked(ret), is(false));
                list.check(ret);
                assertThat(ret, isIn(data));
                signal.countDown();
            }

        })) {
            for (int i : new IntRange(3)) {
                Future<ReceiveData> future = client.startOnNewThread();
                futures.add(future);
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

        for (Future<ReceiveData> future : futures) {
            // 例外がスレッドで発生していればここでExecutionExceptionが投げられる
            ReceiveData receiveData = future.get();
            assertThat(receiveData, is(notNullValue()));
            assertThat(future.isDone(), is(true));
            assertThat(receiveData.get(), is(nullValue()));
        }
    }

}
