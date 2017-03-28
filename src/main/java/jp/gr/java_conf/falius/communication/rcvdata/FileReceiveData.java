package jp.gr.java_conf.falius.communication.rcvdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ファイル受信が可能なReceiveDataです。
 * 指定された場所に保存します。
 * @author "ymiyauchi"
 *
 */
public class FileReceiveData extends ExtendableReceiveData {


    public FileReceiveData(ReceiveData receiveData) {
        super(receiveData);
    }


    public void getAndSave(File file) throws IOException {
        getAndOutput(new FileOutputStream(file));
    }

    /**
     *
     * @param file
     * @param append 追記するかどうか
     * @throws IOException
     */
    public void getAndSave(File file, boolean append) throws IOException {
        getAndOutput(new FileOutputStream(file, append));
    }

}
