package jp.gr.java_conf.falius.communication.test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.rcvdata.ObjectReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.ObjectSendData;
import jp.gr.java_conf.falius.communication.test.helper.EchoServer;
import jp.gr.java_conf.falius.communication.test.helper.SerializableTest;
import jp.gr.java_conf.falius.communication.test.helper.ServerHelper;

public class ObjectSendDataTest {
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
        assertThat((SerializableTest) obj, is(not(sameInstance(data))));
    }

}
