package jp.gr.java_conf.falius.communication.test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.FileSendData;
import jp.gr.java_conf.falius.communication.test.helper.EchoServer;
import jp.gr.java_conf.falius.communication.test.helper.ServerHelper;

public class FileSendDataTest {
    private static Logger log = LoggerFactory.getLogger(FileSendDataTest.class);
    private static final String PACKAGE_PATH
        = "src\\test\\java\\jp\\gr\\java_conf\\falius\\communication\\test";
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
    public void testPutPath() throws IOException, TimeoutException {
        Path path = Paths.get(new File(PACKAGE_PATH)
                .getAbsolutePath(), "FileSendDataTest.java");
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new BasicSendData());
        sendData.put(path);
        ReceiveData receiveData = client.send(sendData);
        String text = receiveData.getString().replace("\r\n", "").replace("\n", "");
        assertThat(text, is(Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining())));
    }

    @Test
    public void testPutOfFile() throws IOException, TimeoutException {
        File file = new File(PACKAGE_PATH + "\\FileSendDataTest.java");
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new BasicSendData());
        sendData.put(file);
        ReceiveData receiveData = client.send(sendData);
        String text = receiveData.getString().replace("\r\n", "").replace("\n", "");
        assertThat(text, is(Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)
                        .collect(Collectors.joining())));
    }

    @Test
    public void testPutFile() throws IOException, TimeoutException {
        String curDir = PACKAGE_PATH;
        String fileName = "FileSendDataTest.java";
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new BasicSendData());
        sendData.putFile(curDir, fileName);
        ReceiveData receiveData = client.send(sendData);
        String text = receiveData.getString().replace("\r\n", "").replace("\n", "");
        assertThat(text, is(Files.lines(Paths.get(curDir, fileName), StandardCharsets.UTF_8)
                        .collect(Collectors.joining())));
    }

}
