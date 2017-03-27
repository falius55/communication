package jp.gr.java_conf.falius.communication.receiver;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
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
import jp.gr.java_conf.falius.communication.sender.FileSendData;
import jp.gr.java_conf.falius.communication.sender.SendQueue;

public class FileReceiveDataTest {
    private static Logger log = LoggerFactory.getLogger(FileReceiveDataTest.class);
    private static final String FILE_CONTENT = "test test test test";
    private static final String HOST = "localhost";
    private static final ServerHelper mServer = new EchoServer();
    private static Path mOriginTmpDir;
    private static Path mTargetTmpDir;

    @BeforeClass
    public static void setupServer() throws IOException {
        mServer.beforeClass();
        // コピー元ファイルを作成する一時ディレクトリ
        mOriginTmpDir = Files.createTempDirectory("cmt_");
        // コピー先の一時ディレクトリ
        mTargetTmpDir = Files.createTempDirectory("cmt_");

        // @Beforeで毎回同一名のファイルを作成すると、各テストが並列実行される際に
        // アクセスが拒否されることがある。そのため、各メソッドの名前でファイルを作成
        for (Method method : FileReceiveDataTest.class.getDeclaredMethods()) {
            Annotation anotation = method.getAnnotation(Test.class);
            if (anotation != null) {
                String methodName = method.getName();
                String fileName = "test_" + methodName;
                Path filePath = Paths.get(mOriginTmpDir.toString(), fileName);
                log.debug("file path : {}", filePath.toString());
                Files.write(filePath, Arrays.asList(FILE_CONTENT));
            }
        }
    }

    @AfterClass
    public static void shutdownServer() throws IOException {
        mServer.afterClass();
    }

    @Test
    public void testGetAndSavePathOpenOptionArray() throws IOException, TimeoutException {
        String fileName = "test_testGetAndSavePathOpenOptionArray";
        Path originFile = Paths.get(mOriginTmpDir.toString(), fileName);
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new SendQueue());
        sendData.put(originFile);
        ReceiveData ret = client.start(sendData);
        FileReceiveData fileRcvData = new FileReceiveData(ret);

        Path targetFile = Paths.get(mTargetTmpDir.toString(), fileName);
        fileRcvData.getAndSave(targetFile, StandardOpenOption.CREATE_NEW);
        assertThat(Files.exists(targetFile), is(true));
        assertThat(Files.lines(targetFile).collect(Collectors.joining()), is(FILE_CONTENT));
    }

    @Test
    public void testGetAndSaveFile() throws IOException, TimeoutException {
        String fileName = "test_testGetAndSaveFile";
        Path originFile = Paths.get(mOriginTmpDir.toString(), fileName);
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new SendQueue());
        sendData.put(originFile);
        ReceiveData ret = client.start(sendData);
        FileReceiveData fileRcvData = new FileReceiveData(ret);

        File targetFile = new File(mTargetTmpDir.toString() + "\\" + fileName);
        fileRcvData.getAndSave(targetFile);
        assertThat(targetFile.exists(), is(true));
        assertThat(Files.lines(Paths.get(mTargetTmpDir.toString(), fileName)).collect(Collectors.joining()),
                is(FILE_CONTENT));
    }

    @Test
    public void testGetAndSaveFileBoolean() throws IOException, TimeoutException {
        String fileName = "test_testGetAndSaveFileBoolean";
        Path originFile = Paths.get(mOriginTmpDir.toString(), fileName);
        Path target = Paths.get(mTargetTmpDir.toString(), fileName);
        Files.copy(originFile, target);

        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new SendQueue());
        sendData.put(originFile);
        ReceiveData ret = client.start(sendData);
        FileReceiveData fileRcvData = new FileReceiveData(ret);

        File targetFile = new File(mTargetTmpDir.toString() + "\\" + fileName);
        fileRcvData.getAndSave(targetFile, true);
        assertThat(targetFile.exists(), is(true));
        assertThat(Files.lines(Paths.get(mTargetTmpDir.toString(), fileName)).collect(Collectors.joining()),
                is(FILE_CONTENT + FILE_CONTENT));
    }

    @Test
    public void testGetAndSaveStringStringArray() throws IOException, TimeoutException {
        String fileName = "test_testGetAndSaveStringStringArray";
        Path originFile = Paths.get(mOriginTmpDir.toString(), fileName);
        Client client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new SendQueue());
        sendData.put(originFile);
        ReceiveData ret = client.start(sendData);
        FileReceiveData fileRcvData = new FileReceiveData(ret);

        fileRcvData.getAndSave(mTargetTmpDir.toString(), fileName);

        Path targetFile = Paths.get(mTargetTmpDir.toString(), fileName);
        assertThat(Files.exists(targetFile), is(true));
        assertThat(Files.lines(targetFile).collect(Collectors.joining()), is(FILE_CONTENT));
    }

}
