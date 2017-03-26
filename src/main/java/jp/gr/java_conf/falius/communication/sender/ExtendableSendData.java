package jp.gr.java_conf.falius.communication.sender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * SendDataを拡張する場合、このクラスを継承してください
 * @author "ymiyauchi"
 *
 */
public abstract class ExtendableSendData implements SendData {
    private final SendData mSource;

    public ExtendableSendData(SendData sender) {
        mSource = sender;
    }

    @Override
    public SendData put(ByteBuffer buf) {
        return mSource.put(buf);
    }

    @Override
    public SendData put(ByteBuffer[] bufs) {
        return mSource.put(bufs);
    }

    @Override
    public SendData put(byte[] bytes) {
        return mSource.put(bytes);
    }

    @Override
    public SendData put(int num) {
        return mSource.put(num);
    }

    @Override
    public SendData put(boolean bl) {
        return mSource.put(bl);
    }

    @Override
    public SendData put(String msg) {
        return mSource.put(msg);
    }

    @Override
    public SendData put(InputStream in) throws IOException {
        return mSource.put(in);
    }

    @Override
    public int size() {
        return mSource.size();
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        return mSource.iterator();
    }

    @Override
    public boolean hasRemain() {
        return mSource.hasRemain();
    }
}
