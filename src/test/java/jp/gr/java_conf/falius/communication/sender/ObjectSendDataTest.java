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
import jp.gr.java_conf.falius.communication.helper.SerializableTest;
import jp.gr.java_conf.falius.communication.helper.ServerHelper;
import jp.gr.java_conf.falius.communication.receiver.ObjectReceiveData;
import jp.gr.java_conf.falius.communication.receiver.ReceiveData;

public class ObjectSendDataTest {
    private static Logger log = LoggerFactory.getLogger(ObjectSendDataTest.class);
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
    public void testPutObject() throws IOException, TimeoutException, ClassNotFoundException {
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        SerializableTest data = new SerializableTest("test sample", 25, SerializableTest.Sex.MALE);
        ObjectSendData sendData = new ObjectSendData(new BasicSendData());
        sendData.putObject(data);
        ReceiveData rcv = client.start(sendData);
        ObjectReceiveData receiveData = new ObjectReceiveData(rcv);
        Object obj = receiveData.getObject();

        assertThat(obj, is(instanceOf(SerializableTest.class)));
        assertThat(obj.toString(), is(data.toString()));
        assertThat(obj, is(not(sameInstance(data))));
    }

}
