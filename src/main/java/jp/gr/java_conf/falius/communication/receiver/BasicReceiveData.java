package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.WrongMethodTypeException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Queue;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

/**
 * {@inheritDoc}
 *
 * @author "ymiyauchi"
 */
public class BasicReceiveData implements ReceiveData {
    // パッケージプライベートにするため、receiverパッケージ
    private final static ByteBuffer[] EMPTY_BUFFER_ARRAY = new ByteBuffer[0];
    private final CharsetDecoder DECODER = StandardCharsets.UTF_8.newDecoder();
    private final Queue<ByteBuffer> mData;

    public BasicReceiveData(Queue<ByteBuffer> data) {
        mData = data;
    }

    /**
     * @return 保持している受信データがあればそのデータ。なければnull
     */
    @Override
    public ByteBuffer get() {
        ByteBuffer data = mData.poll();
        return data;
    }

    @Override
    public ByteBuffer[] getAll() {
        ByteBuffer[] ret = mData.toArray(EMPTY_BUFFER_ARRAY);
        mData.clear();
        return ret;
    }

    @Override
    public int dataCount() {
        return mData.size();
    }

    @Override
    public void clear() {
        mData.clear();
    }

    /**
     * @throws WrongMethodTypeException
     */
    @Override
    public String getString() {
        ByteBuffer buf = get();
        if (buf == null) {
            return null;
        }

        try {
            return DECODER.decode(buf).toString();
        } catch (CharacterCodingException e) {
            throw new WrongMethodTypeException("decode error");
        }
    }

    @Override
    public int getInt() {
        ByteBuffer buf = get();
        if (buf == null) {
            throw new NoSuchElementException("no data");
        }
        try {
            int ret = buf.getInt();
            return ret;
        } catch (BufferUnderflowException e) {
            // データが4バイトより少ない
            throw new WrongMethodTypeException("deta less than 4 bytes: " + buf.remaining());
        }
    }

    @Override
    public long getLong() {
        ByteBuffer buf = get();
        if (buf == null) {
            throw new NoSuchElementException("no data");
        }
        try {
            long ret = buf.getLong();
            return ret;
        } catch (BufferUnderflowException e) {
            // データが８バイトより少ない
            throw new WrongMethodTypeException("deta less than 8 bytes: " + buf.remaining());
        }
    }

    @Override
    public double getDouble() {
        ByteBuffer buf = get();
        if (buf == null) {
            throw new NoSuchElementException("no data");
        }
        try {
            double ret = buf.getDouble();
            return ret;
        } catch (BufferUnderflowException e) {
            // データが８バイトより少ない
            throw new WrongMethodTypeException("deta less than 8 bytes: " + buf.remaining());
        }
    }

    @Override
    public float getFloat() {
        ByteBuffer buf = get();
        if (buf == null) {
            throw new NoSuchElementException("no data");
        }
        try {
            float ret = buf.getFloat();
            return ret;
        } catch (BufferUnderflowException e) {
            // データが4バイトより少ない
            throw new WrongMethodTypeException("deta less than 4 bytes: " + buf.remaining());
        }
    }

    @Override
    public boolean getBoolean() {
        return getInt() == 1;
    }

    @Override
    public void getAndOutput(OutputStream os) throws IOException {
        ByteBuffer buf = get();
        try (OutputStream out = os) {
            if (buf == null) {
                return;
            }

            if (buf.hasArray()) {
                byte[] bytes = buf.array();
                out.write(bytes);
            } else {
                for (int i = buf.position(), len = buf.limit(); i < len; i++) {
                    out.write(buf.get());
                }
            }
        }
    }
}
