package jp.gr.java_conf.falius.communication.sender;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.FileSendData;

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
    public void testPutOfFile() throws IOException, TimeoutException {
        File file = new File(
                "src\\test\\java\\jp\\gr\\java_conf\\falius\\communication"
                + "\\sender\\FileSendDataTest.java");
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new BasicSendData());
        sendData.put(file);
        ReceiveData receiveData = client.send(sendData);
        String text = receiveData.getString().replace("\r\n", "").replace("\n", "");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        assertThat(text, is(sb.toString()));
    }

}
