package jp.gr.java_conf.falius.communication.rcvdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ファイル受信が可能なReceiveDataです。
 * 指定された場所に保存します。
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public class FileReceiveData extends ExtendableReceiveData {


    /**
     *
     * @param receiveData
     * @since 1.4.0
     */
    public FileReceiveData(ReceiveData receiveData) {
        super(receiveData);
    }

    /**
     * @throws FileAlreadyExistsException openOptionにCREATE_NEWを渡し、かつファイルがすでに存在した場合
     * @since 1.4.0
     */
    public void getAndSave(Path savePath, OpenOption... openOptions) throws IOException {
        getAndOutput(Files.newOutputStream(savePath, openOptions));
    }

    /**
     *
     * @param file
     * @throws IOException
     * @since 1.4.0
     */
    public void getAndSave(File file) throws IOException {
        getAndOutput(new FileOutputStream(file));
    }

    /**
     *
     * @param file
     * @param append 追記するかどうか
     * @throws IOException
     * @since 1.4.0
     */
    public void getAndSave(File file, boolean append) throws IOException {
        getAndOutput(new FileOutputStream(file, append));
    }

    /**
     *
     * @param firstPathName
     * @param morePathName
     * @throws IOException
     * @since  1.4.0
     */
    public void getAndSave(String firstPathName, String... morePathName) throws IOException {
        Path path = Paths.get(firstPathName, morePathName);
        getAndSave(path);
    }
}
