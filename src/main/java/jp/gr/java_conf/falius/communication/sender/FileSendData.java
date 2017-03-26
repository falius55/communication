package jp.gr.java_conf.falius.communication.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSendData extends ExtendableSendData {

    public FileSendData(SendData sender) {
        super(sender);
    }

    public SendData put(Path filePath) throws IOException {
        return put(Files.readAllBytes(filePath));
    }

    public SendData put(File file) throws FileNotFoundException, IOException {
        return put(new FileInputStream(file));
    }

    public SendData putFile(String firstPathName, String... morePathName) throws IOException {
        Path path = Paths.get(firstPathName, morePathName);
        return put(path);
    }
}