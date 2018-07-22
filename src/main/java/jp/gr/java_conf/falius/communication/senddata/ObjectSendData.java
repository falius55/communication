package jp.gr.java_conf.falius.communication.senddata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * シリアライズ可能オブジェクトを送信する際に利用するSendData
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public class ObjectSendData extends ExtendableSendData {

    /**
     *
     * @param sendData
     * @since 1.4.0
     */
    public ObjectSendData(SendData sendData) {
        super(sendData);
    }

    /**
     *
     * @param obj
     * @return
     * @throws IOException
     * @since 1.4.0
     */
    public SendData putObject(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        byte[] bytes = baos.toByteArray();
        return put(bytes);
    }
}
