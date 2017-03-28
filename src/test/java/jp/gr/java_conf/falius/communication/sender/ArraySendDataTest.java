package jp.gr.java_conf.falius.communication.sender;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
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
import jp.gr.java_conf.falius.communication.rcvdata.ArrayReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.ArraySendData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.util.range.IntRange;

public class ArraySendDataTest {
    private static Logger log = LoggerFactory.getLogger(ArraySendDataTest.class);
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
    public void testPutIntArray() throws IOException, TimeoutException {
        int[] data = { 1, 2, 3, 4, 5 };
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        ArraySendData sendData = new ArraySendData(new BasicSendData());
        sendData.put(data);
        ReceiveData rcv = client.start(sendData);
        ArrayReceiveData ret = new ArrayReceiveData(rcv);
        int[] result = ret.getIntArray();

        assertThat(result.length, is(data.length));
        for (int i : new IntRange(data.length)) {
            assertThat(result[i], is(data[i]));
        }

    }

    @Test
    public void testPutStringArray() throws IOException, TimeoutException {
        String[] data = { "data1", "data2", "data3" };
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        ArraySendData sendData = new ArraySendData(new BasicSendData());
        sendData.put(data);
        ReceiveData rcv = client.start(sendData);
        ArrayReceiveData ret = new ArrayReceiveData(rcv);
        String[] result = ret.getStringArray();

        assertThat(result, is(arrayWithSize(data.length)));
        assertThat(result, is(arrayContaining(data)));
    }

    @Test
    public void testPutLongArray() throws IOException, TimeoutException {
        long[] data = { 112L, 22342L, 3796L, 479843279L, 512839L };
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        ArraySendData sendData = new ArraySendData(new BasicSendData());
        sendData.put(data);
        ReceiveData rcv = client.start(sendData);
        ArrayReceiveData ret = new ArrayReceiveData(rcv);
        long[] result = ret.getLongArray();

        assertThat(result.length, is(data.length));
        for (int i : new IntRange(data.length)) {
            assertThat(result[i], is(data[i]));
        }
    }

    @Test
    public void testPutDoubleArray() throws IOException, TimeoutException {
        double[] data = { 11.2, 2.2342, 0.3796, 4798.43279, 0.00512839 };
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        ArraySendData sendData = new ArraySendData(new BasicSendData());
        sendData.put(data);
        ReceiveData rcv = client.start(sendData);
        ArrayReceiveData ret = new ArrayReceiveData(rcv);
        double[] result = ret.getDoubleArray();

        assertThat(result.length, is(data.length));
        for (int i : new IntRange(data.length)) {
            assertThat(result[i], is(data[i]));
        }
    }

    @Test
    public void testPutFloatArray() throws IOException, TimeoutException {
        float[] data = { 11.2f, 2.2342f, 0.3796f, 4798.43279f, 0.00512839f };
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        ArraySendData sendData = new ArraySendData(new BasicSendData());
        sendData.put(data);
        ReceiveData rcv = client.start(sendData);
        ArrayReceiveData ret = new ArrayReceiveData(rcv);
        float[] result = ret.getFloatArray();

        assertThat(result.length, is(data.length));
        for (int i : new IntRange(data.length)) {
            assertThat(result[i], is(data[i]));
        }
    }

}
