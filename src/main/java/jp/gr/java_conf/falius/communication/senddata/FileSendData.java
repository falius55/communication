package jp.gr.java_conf.falius.communication.senddata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * ファイルを送信する際に利用するSendData
 * @author "ymiyauchi"
 *
 */
public class FileSendData extends ExtendableSendData {

    public FileSendData(SendData sendData) {
        super(sendData);
    }

    public SendData put(File file) throws IOException {
        return put(new FileInputStream(file));
    }
}
