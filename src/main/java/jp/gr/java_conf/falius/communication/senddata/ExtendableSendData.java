package jp.gr.java_conf.falius.communication.senddata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * SendDataを拡張する場合、このクラスを継承してください
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public abstract class ExtendableSendData implements SendData {
    private final SendData mSource;

    /**
     *
     * @param sendData
     * @since 1.4.0
     */
    public ExtendableSendData(SendData sendData) {
        mSource = sendData;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(ByteBuffer buf) {
        return mSource.put(buf);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(ByteBuffer[] bufs) {
        return mSource.put(bufs);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(byte[] bytes) {
        return mSource.put(bytes);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(int num) {
        return mSource.put(num);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(long num) {
        return mSource.put(num);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(double num) {
        return mSource.put(num);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(float num) {
        return mSource.put(num);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(boolean bl) {
        return mSource.put(bl);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(String msg) {
        return mSource.put(msg);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData put(InputStream in) throws IOException {
        return mSource.put(in);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final int size() {
        return mSource.size();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final Iterator<ByteBuffer> iterator() {
        return mSource.iterator();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final boolean hasRemain() {
        return mSource.hasRemain();
    }
}
