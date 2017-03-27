package jp.gr.java_conf.falius.communication.sender;

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

import jp.gr.java_conf.falius.communication.client.Client;
import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.helper.EchoServer;
import jp.gr.java_conf.falius.communication.helper.ServerHelper;
import jp.gr.java_conf.falius.communication.receiver.ReceiveData;

public class FileSendDataTest {
    private static Logger log = LoggerFactory.getLogger(FileSendDataTest.class);
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
        Path path = Paths.get(new File(
                "src\\test\\java\\jp\\gr\\java_conf\\falius\\communication\\sender")
                .getAbsolutePath(), "FileSendDataTest.java");
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new SendQueue());
        sendData.put(path);
        ReceiveData receiveData = client.start(sendData);
        String text = receiveData.getString().replace("\r\n", "").replace("\n", "");
        assertThat(text, is(Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining())));
    }

    @Test
    public void testPutFile() throws IOException, TimeoutException {
        File file = new File(
                "src\\test\\java\\jp\\gr\\java_conf\\falius\\communication"
                + "\\sender\\FileSendDataTest.java");
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new SendQueue());
        sendData.put(file);
        ReceiveData receiveData = client.start(sendData);
        String text = receiveData.getString().replace("\r\n", "").replace("\n", "");
        assertThat(text, is(Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)
                        .collect(Collectors.joining())));
    }

    @Test
    public void testPutFile1() throws IOException, TimeoutException {
        String curDir = "src\\test\\java\\jp\\gr\\java_conf\\falius\\communication\\sender";
        String fileName = "FileSendDataTest.java";
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new SendQueue());
        sendData.putFile(curDir, fileName);
        ReceiveData receiveData = client.start(sendData);
        String text = receiveData.getString().replace("\r\n", "").replace("\n", "");
        assertThat(text, is(Files.lines(Paths.get(curDir, fileName), StandardCharsets.UTF_8)
                        .collect(Collectors.joining())));
    }

}
