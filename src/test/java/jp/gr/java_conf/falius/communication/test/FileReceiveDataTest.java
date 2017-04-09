package jp.gr.java_conf.falius.communication.test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.core.socket.NonBlockingClient;
import jp.gr.java_conf.falius.communication.rcvdata.FileReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.BasicSendData;
import jp.gr.java_conf.falius.communication.senddata.FileSendData;
import jp.gr.java_conf.falius.communication.test.helper.EchoServer;
import jp.gr.java_conf.falius.communication.test.helper.ServerHelper;

public class FileReceiveDataTest {
    private static final String FILE_CONTENT = "test test test test";
    private static final String HOST = "localhost";
    private static final ServerHelper mServer = new EchoServer();
    private static Path mOriginTmpDir;
    private static Path mTargetTmpDir;

    @BeforeClass
    public static void setup() throws IOException {
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
                try (BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    bw.write(FILE_CONTENT);
                }
            }
        }
    }

    @AfterClass
    public static void shutdown() throws IOException {
        mServer.afterClass();
    }

    @Test
    public void testGetAndSaveFile() throws IOException, TimeoutException {
        String fileName = "test_testGetAndSaveFile";
        Path originFile = Paths.get(mOriginTmpDir.toString(), fileName);
        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new BasicSendData());
        sendData.put(originFile.toFile());
        ReceiveData ret = client.send(sendData);
        FileReceiveData fileRcvData = new FileReceiveData(ret);

        File targetFile = new File(mTargetTmpDir.toString() + "\\" + fileName);
        fileRcvData.getAndSave(targetFile);
        assertThat(targetFile.exists(), is(true));

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(targetFile.toPath(), StandardCharsets.UTF_8)) {
            sb.append(br.readLine());
        }
        assertThat(sb.toString(), is(FILE_CONTENT));
    }

    @Test
    public void testGetAndSaveFileBoolean() throws IOException, TimeoutException {
        String fileName = "test_testGetAndSaveFileBoolean";
        Path originFile = Paths.get(mOriginTmpDir.toString(), fileName);
        Path target = Paths.get(mTargetTmpDir.toString(), fileName);
        Files.copy(originFile, target);

        SwapClient client = new NonBlockingClient(HOST, mServer.getPort());
        FileSendData sendData = new FileSendData(new BasicSendData());
        sendData.put(originFile.toFile());
        ReceiveData ret = client.send(sendData);
        FileReceiveData fileRcvData = new FileReceiveData(ret);

        File targetFile = new File(mTargetTmpDir.toString() + "\\" + fileName);
        fileRcvData.getAndSave(targetFile, true);
        assertThat(targetFile.exists(), is(true));

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(targetFile.toPath(), StandardCharsets.UTF_8)) {
            sb.append(br.readLine());
        }
        assertThat(sb.toString(), is(FILE_CONTENT + FILE_CONTENT));
    }

}
