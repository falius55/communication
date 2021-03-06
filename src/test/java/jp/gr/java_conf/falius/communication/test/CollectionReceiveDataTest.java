package jp.gr.java_conf.falius.communication.test;

import java.io.IOException;
import java.lang.invoke.WrongMethodTypeException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.rcvdata.CollectionReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.test.helper.EchoServer;
import jp.gr.java_conf.falius.communication.test.helper.ServerHelper;

public class CollectionReceiveDataTest {
    private static Logger log = LoggerFactory.getLogger(CollectionReceiveDataTest.class);
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

    @Test(expected=WrongMethodTypeException.class)
    public void testGetListException() throws IOException, TimeoutException {
        int data = 10;

        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        sendData.put(data);
        ReceiveData rcv = client.send(sendData);
        CollectionReceiveData drcv = new CollectionReceiveData(rcv);
        drcv.getList();
    }

    @Test(expected=WrongMethodTypeException.class)
    public void testGetMapException() throws IOException, TimeoutException {
        String data = "data";

        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        SendData sendData = new BasicSendData();
        sendData.put(data);
        ReceiveData rcv = client.send(sendData);
        CollectionReceiveData drcv = new CollectionReceiveData(rcv);
        drcv.getMap();
    }
}
