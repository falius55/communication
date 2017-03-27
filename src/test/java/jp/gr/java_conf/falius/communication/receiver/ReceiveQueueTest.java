package jp.gr.java_conf.falius.communication.receiver;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.client.Client;
import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.helper.EchoServer;
import jp.gr.java_conf.falius.communication.helper.ServerHelper;
import jp.gr.java_conf.falius.communication.receiver.ReceiveData;
import jp.gr.java_conf.falius.communication.sender.SendData;
import jp.gr.java_conf.falius.communication.sender.SendQueue;
import jp.gr.java_conf.falius.util.range.IntRange;

public class ReceiveQueueTest {
    private static Logger log = LoggerFactory.getLogger(ReceiveQueueTest.class);
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
    public void testGetBoolean() throws IOException, TimeoutException {
        boolean[] data = {true, false, false, true};
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new SendQueue();
        for (boolean b : data) {
            sendData.put(b);
        }
        ReceiveData receiveData = client.start(sendData);
        new IntRange(receiveData.dataCount()).forEach((i) -> {
            boolean ret = receiveData.getBoolean();
            assertThat(ret, is(data[i]));
        });;
    }

    @Test(expected=IllegalStateException.class)
    public void testGetStringException() throws IOException, TimeoutException {
        int[] data = {-15, 1, 2, 3};
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new SendQueue();
        for (int i : data) {
            sendData.put(i);
        }
        ReceiveData receiveData = client.start(sendData);
        new IntRange(receiveData.dataCount()).forEach((i) -> {
            String str = receiveData.getString();
            log.debug("get string: {}", str);
            assertThat(str, is(data[i]));
        });;
    }

    @Test(expected=NoSuchElementException.class)
    public void testGetIntException() throws IOException, TimeoutException {
        int[] data = {1, 2};
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new SendQueue();
        for (int i : data) {
            sendData.put(i);
        }
        ReceiveData receiveData = client.start(sendData);
        new IntRange(receiveData.dataCount()).forEach((i) -> {
            int ret = receiveData.getInt();
            assertThat(ret, is(data[i]));
        });;

        receiveData.getInt();
    }
}
