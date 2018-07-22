package jp.gr.java_conf.falius.communication.rcvdata;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public abstract class ExtendableReceiveData implements ReceiveData {
    private final ReceiveData mSource;

    /**
     *
     * @param receiveData
     * @since 1.4.0
     */
    public ExtendableReceiveData(ReceiveData receiveData) {
        mSource = receiveData;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final int dataCount() {
        return mSource.dataCount();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final ByteBuffer get() {
        return mSource.get();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final ByteBuffer[] getAll() {
        return mSource.getAll();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final String getString() {
        return mSource.getString();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final int getInt() {
        return mSource.getInt();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final long getLong() {
        return mSource.getLong();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final double getDouble() {
        return mSource.getDouble();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final float getFloat() {
        return mSource.getFloat();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final boolean getBoolean() {
        return mSource.getBoolean();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final void getAndOutput(OutputStream os) throws IOException {
        mSource.getAndOutput(os);
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final void clear() {
        mSource.clear();

    }

}
