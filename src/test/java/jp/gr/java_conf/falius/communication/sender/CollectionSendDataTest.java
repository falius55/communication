package jp.gr.java_conf.falius.communication.sender;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import jp.gr.java_conf.falius.communication.rcvdata.CollectionReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.CollectionSendData;

public class CollectionSendDataTest {
    private static Logger log = LoggerFactory.getLogger(CollectionSendDataTest.class);
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
    public void testPutListOfString() throws IOException, TimeoutException {
        List<String> sendData = new ArrayList<String>() {
            {
                add("data");
                add("abc");
                add("89345");
                add("jldkajfkl");
            }
        };
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        CollectionSendData data = new CollectionSendData(new BasicSendData());
        data.put(sendData);
        ReceiveData receiveData = client.send(data);
        CollectionReceiveData collectionData = new CollectionReceiveData(receiveData);
        List<String> ret = collectionData.getList();
        assertThat(ret, is(contains(sendData.toArray(new String[0]))));

        assertThat(receiveData.get(), is(nullValue()));
    }

    @Test
    public void testPutMapOfStringString() throws IOException, TimeoutException {
        Map<String, String> sendData = new HashMap<>();
        sendData.put("key1", "data1");
        sendData.put("key2", "data2");
        sendData.put("abc", "def");
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        CollectionSendData data = new CollectionSendData(new BasicSendData());
        data.put(sendData);
        ReceiveData receiveData = client.send(data);
        Map<String, String> ret = new CollectionReceiveData(receiveData).getMap();
        assertThat(ret.entrySet(), hasSize(sendData.size()));
        for (Map.Entry<String, String> entry : sendData.entrySet()) {
            assertThat(ret, hasEntry(entry.getKey(), entry.getValue()));
        }

        assertThat(receiveData.get(), is(nullValue()));
    }

}
