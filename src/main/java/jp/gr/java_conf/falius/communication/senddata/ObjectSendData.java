package jp.gr.java_conf.falius.communication.senddata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * シリアライズ可能オブジェクトを送信する際に利用するSendData
 * @author "ymiyauchi"
 *
 */
public class ObjectSendData extends ExtendableSendData {

    public ObjectSendData(SendData sendData) {
        super(sendData);
    }

    public SendData putObject(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        byte[] bytes = baos.toByteArray();
        return put(bytes);
    }
}
