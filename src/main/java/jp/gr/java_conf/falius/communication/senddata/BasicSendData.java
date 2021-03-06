package jp.gr.java_conf.falius.communication.senddata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;

/**
 * 送信データを格納するクラスです。
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public class BasicSendData implements SendData {
    private final Queue<ByteBuffer> mData = new ArrayDeque<>();

    /**
     * @since 1.4.0
     */
    @Override
    public final BasicSendData put(ByteBuffer buf) {
        if (!buf.hasRemaining()) {
            throw new IllegalArgumentException("data have no remaining. might not flip()");
        }
        mData.add(buf);
        return this;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final BasicSendData put(ByteBuffer[] bufs) {
        mData.addAll(Arrays.asList(bufs));
        return this;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.allocate(bytes.length);
        buf.put(bytes);
        buf.flip();
        return put(buf);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(String str) {
        return put(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(int num) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(num);
        buf.flip();
        return put(buf);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(long num) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(num);
        buf.flip();
        return put(buf);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(double num) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putDouble(num);
        buf.flip();
        return put(buf);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(float num) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putFloat(num);
        buf.flip();
        return put(buf);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(boolean bl) {
        return put(bl ? 1 : 0);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public BasicSendData put(InputStream is) throws IOException {
        final int READ_SIZE = 4096 * 2;
        int size = READ_SIZE;
        ByteBuffer result = ByteBuffer.allocate(size);
        byte[] bytes = new byte[READ_SIZE];
        int len;
        while ((len = is.read(bytes)) != -1) {
            int rest = size - result.position();
            if (rest < len) {
                size += READ_SIZE;
                ByteBuffer newBuf = ByteBuffer.allocate(size);
                result.flip();
                newBuf.put(result);
                result = newBuf;
            }

            result.put(bytes, 0, len);
        }

        result.flip();
        return put(result);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public Iterator<ByteBuffer> iterator() {
        return mData.iterator();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public int size() {
        return mData.size();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public boolean hasRemain() {
        for (ByteBuffer elem : this) {
            if (elem.hasRemaining()) {
                return true;
            }
        }
        return false;
    }
}
