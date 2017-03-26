package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class ExtendableReceiver implements ReceiveData {
    private final ReceiveData mSource;

    public ExtendableReceiver(ReceiveData receiver) {
        mSource = receiver;
    }

    @Override
    public int dataCount() {
        return mSource.dataCount();
    }

    @Override
    public ByteBuffer get() {
        return mSource.get();
    }

    @Override
    public ByteBuffer[] getAll() {
        return mSource.getAll();
    }

    @Override
    public String getString() {
        return mSource.getString();
    }

    @Override
    public int getInt() {
        return mSource.getInt();
    }

    @Override
    public boolean getBoolean() {
        return mSource.getBoolean();
    }

    @Override
    public void getAndOutput(OutputStream os) throws IOException {
        mSource.getAndOutput(os);
    }

    @Override
    public void clear() {
        mSource.clear();

    }

}
