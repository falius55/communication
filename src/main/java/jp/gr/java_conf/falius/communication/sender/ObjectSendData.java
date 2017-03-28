package jp.gr.java_conf.falius.communication.sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSendData extends ExtendableSendData {

    public ObjectSendData(SendData sender) {
        super(sender);
    }

    public SendData putObject(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        byte[] bytes = baos.toByteArray();
        return put(bytes);
    }
}
