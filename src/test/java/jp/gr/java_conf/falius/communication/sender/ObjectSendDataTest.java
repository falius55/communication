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

import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.client.SwapClient;
import jp.gr.java_conf.falius.communication.helper.EchoServer;
import jp.gr.java_conf.falius.communication.helper.SerializableTest;
import jp.gr.java_conf.falius.communication.helper.ServerHelper;
import jp.gr.java_conf.falius.communication.rcvdata.ObjectReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.ObjectSendData;

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
    public void testPutAndGetObject() throws IOException, TimeoutException, ClassNotFoundException {
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SerializableTest data = new SerializableTest("test sample", 25, SerializableTest.Sex.MALE);
        ObjectSendData sendData = new ObjectSendData(new BasicSendData());
        sendData.putObject(data);
        ReceiveData rcv = client.send(sendData);
        ObjectReceiveData receiveData = new ObjectReceiveData(rcv);
        Object obj = receiveData.getObject();

        assertThat(obj, is(instanceOf(SerializableTest.class)));
        assertThat(obj.toString(), is(data.toString()));
        assertThat(obj, is(not(sameInstance(data))));
    }

}
