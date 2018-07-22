package jp.gr.java_conf.falius.communication.senddata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ファイルを送信する際に利用するSendData
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public class FileSendData extends ExtendableSendData {

    /**
     *
     * @param sendData
     * @since 1.4.0
     */
    public FileSendData(SendData sendData) {
        super(sendData);
    }

    /**
     *
     * @param filePath
     * @return
     * @throws IOException
     * @since 1.4.0
     */
    public SendData put(Path filePath) throws IOException {
        return put(Files.readAllBytes(filePath));
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     * @since 1.4.0
     */
    public SendData put(File file) throws IOException {
        return put(new FileInputStream(file));
    }

    /**
     *
     * @param firstPathName
     * @param morePathName
     * @return
     * @throws IOException
     * @since 1.4.0
     */
    public SendData putFile(String firstPathName, String... morePathName) throws IOException {
        Path path = Paths.get(firstPathName, morePathName);
        return put(path);
    }
}
