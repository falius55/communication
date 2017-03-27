package jp.gr.java_conf.falius.communication.receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReceiveData extends ExtendableReceiveData {


    public FileReceiveData(ReceiveData receiveData) {
        super(receiveData);
    }

    /**
     * @throws FileAlreadyExistsException openOptionにCREATE_NEWを渡し、かつファイルがすでに存在した場合
     */
    public void getAndSave(Path savePath, OpenOption... openOptions) throws IOException {
        getAndOutput(Files.newOutputStream(savePath, openOptions));
    }

    public void getAndSave(File file) throws IOException {
        getAndOutput(new FileOutputStream(file));
    }

    public void getAndSave(File file, boolean append) throws IOException {
        getAndOutput(new FileOutputStream(file, append));
    }

    public void getAndSave(String firstPathName, String... morePathName) throws IOException {
        Path path = Paths.get(firstPathName, morePathName);
        getAndSave(path);
    }
}
