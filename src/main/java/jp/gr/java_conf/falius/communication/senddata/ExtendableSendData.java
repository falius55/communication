package jp.gr.java_conf.falius.communication.senddata;

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

    public ExtendableSendData(SendData sendData) {
        mSource = sendData;
    }

    @Override
    public final SendData put(ByteBuffer buf) {
        return mSource.put(buf);
    }

    @Override
    public final SendData put(ByteBuffer[] bufs) {
        return mSource.put(bufs);
    }

    @Override
    public final SendData put(byte[] bytes) {
        return mSource.put(bytes);
    }

    @Override
    public final SendData put(int num) {
        return mSource.put(num);
    }

    @Override
    public final SendData put(long num) {
        return mSource.put(num);
    }

    @Override
    public final SendData put(double num) {
        return mSource.put(num);
    }

    @Override
    public final SendData put(float num) {
        return mSource.put(num);
    }

    @Override
    public final SendData put(boolean bl) {
        return mSource.put(bl);
    }

    @Override
    public final SendData put(String msg) {
        return mSource.put(msg);
    }

    @Override
    public final SendData put(InputStream in) throws IOException {
        return mSource.put(in);
    }

    @Override
    public final int size() {
        return mSource.size();
    }

    @Override
    public final Iterator<ByteBuffer> iterator() {
        return mSource.iterator();
    }

    @Override
    public final boolean hasRemain() {
        return mSource.hasRemain();
    }
}
