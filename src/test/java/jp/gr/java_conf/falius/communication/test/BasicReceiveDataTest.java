package jp.gr.java_conf.falius.communication.test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.lang.invoke.WrongMethodTypeException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.test.helper.EchoServer;
import jp.gr.java_conf.falius.communication.test.helper.ServerHelper;
import jp.gr.java_conf.falius.util.range.IntRange;

public class BasicReceiveDataTest {
    private static Logger log = LoggerFactory.getLogger(BasicReceiveDataTest.class);
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
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        for (boolean b : data) {
            sendData.put(b);
        }
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(data.length));
        for (int i : new IntRange(receiveData.dataCount())) {
            boolean ret = receiveData.getBoolean();
            assertThat(ret, is(data[i]));
        }
    }

    @Test(expected=WrongMethodTypeException.class)
    public void testGetStringException() throws IOException, TimeoutException {
        // getString内で-15がデコードできたりできなかったり...??
        int[] data = {-15, 1, 2, 3};
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        for (int i : data) {
            sendData.put(i);
        }
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(data.length));
        for (int i : new IntRange(receiveData.dataCount())) {
            String str = receiveData.getString();
            log.debug("get string: {}", str);
        }
    }

    @Test(expected=NoSuchElementException.class)
    public void testGetIntNoDataException() throws IOException, TimeoutException {
        int[] data = {1, 2};
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        for (int i : data) {
            sendData.put(i);
        }
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(data.length));
        for (int i : new IntRange(receiveData.dataCount())) {
            int ret = receiveData.getInt();
            assertThat(ret, is(data[i]));
        }

        receiveData.getInt();
    }

    @Test(expected=WrongMethodTypeException.class)
    public void testGetIntWrongMethodException() throws IOException, TimeoutException {
        byte[] data = {2, 3};
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        sendData.put(data);
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(1));
        receiveData.getInt();
    }

    @Test(expected=NoSuchElementException.class)
    public void testGetLongNoDataException() throws IOException, TimeoutException {
        long[] data = {1, 2};
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        for (long i : data) {
            sendData.put(i);
        }
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(data.length));
        for (int i : new IntRange(receiveData.dataCount())) {
            long ret = receiveData.getLong();
            assertThat(ret, is(data[i]));
        }

        receiveData.getLong();
    }

    @Test(expected=WrongMethodTypeException.class)
    public void testGetLongWrongMethodException() throws IOException, TimeoutException {
        int data = 10;
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        sendData.put(data);
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(1));
        receiveData.getLong();
    }

    @Test(expected=NoSuchElementException.class)
    public void testGetDoubleNoDataException() throws IOException, TimeoutException {
        double[] data = {1.4, 2.1};
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        for (double i : data) {
            sendData.put(i);
        }
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(data.length));
        for (int i : new IntRange(receiveData.dataCount())) {
            double ret = receiveData.getDouble();
            assertThat(ret, is(data[i]));
        }

        receiveData.getDouble();
    }

    @Test(expected=WrongMethodTypeException.class)
    public void testGetDoubleWrongMethodException() throws IOException, TimeoutException {
        int data = 10;
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        sendData.put(data);
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(1));
        receiveData.getDouble();
    }

    @Test(expected=NoSuchElementException.class)
    public void testGetFloatNoDataException() throws IOException, TimeoutException {
        float[] data = {1.4f, 2.1f};
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        for (float i : data) {
            sendData.put(i);
        }
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(data.length));
        for (int i : new IntRange(receiveData.dataCount())) {
            float ret = receiveData.getFloat();
            assertThat(ret, is(data[i]));
        }

        receiveData.getFloat();
    }

    @Test(expected=WrongMethodTypeException.class)
    public void testGetFloatWrongMethodException() throws IOException, TimeoutException {
        byte[] data = {4, 9};
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        sendData.put(data);
        ReceiveData receiveData = client.send(sendData);
        assertThat(receiveData.dataCount(), is(1));
        receiveData.getFloat();
    }
}
