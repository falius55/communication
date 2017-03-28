package jp.gr.java_conf.falius.communication.receiver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class ObjectReceiveData extends ExtendableReceiveData {

    public ObjectReceiveData(ReceiveData receiver) {
        super(receiver);
    }

    public Object getObject() throws IOException, ClassNotFoundException {
        ByteBuffer buf = get();
        if (buf == null) {
            return null;
        }
        if (buf.hasArray()) {
            ByteArrayInputStream bais = new ByteArrayInputStream(buf.array());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object ret = ois.readObject();
            return ret;
        }
        return null;
    }
}
