package jp.gr.java_conf.falius.communication.rcvdata;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class ExtendableReceiveData implements ReceiveData {
    private final ReceiveData mSource;

    public ExtendableReceiveData(ReceiveData receiver) {
        mSource = receiver;
    }

    @Override
    public final int dataCount() {
        return mSource.dataCount();
    }

    @Override
    public final ByteBuffer get() {
        return mSource.get();
    }

    @Override
    public final ByteBuffer[] getAll() {
        return mSource.getAll();
    }

    @Override
    public final String getString() {
        return mSource.getString();
    }

    @Override
    public final int getInt() {
        return mSource.getInt();
    }

    @Override
    public final long getLong() {
        return mSource.getLong();
    }

    @Override
    public final double getDouble() {
        return mSource.getDouble();
    }

    @Override
    public final float getFloat() {
        return mSource.getFloat();
    }

    @Override
    public final boolean getBoolean() {
        return mSource.getBoolean();
    }

    @Override
    public final void getAndOutput(OutputStream os) throws IOException {
        mSource.getAndOutput(os);
    }

    @Override
    public final void clear() {
        mSource.clear();

    }

}
