package jp.gr.java_conf.falius.communication.header;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

public class UnFinishedHeader implements Header {
    private static final IntBuffer EMPTY_INT_BUFFER = IntBuffer.allocate(0);

    private final int mHeaderSize;  // ヘッダー自身のサイズ
    private final int mAllDataSize;  // ヘッダー含む全データサイズ
    private final ByteBuffer mHeaderBuf;

    UnFinishedHeader(int headerSize, int allDataSize, ByteBuffer headerBuf) {
        mHeaderSize = headerSize;
        mAllDataSize = allDataSize;
        mHeaderBuf = headerBuf;
    }

    @Override
    public Header read(SocketChannel channel) throws IOException {
        int tmp = channel.read(mHeaderBuf);
        if (tmp < 0) {
            throw new IOException();
        }
        if (mHeaderBuf.hasRemaining()) {
            // 最後までヘッダーを読み取れていない
            return this;
        }
        mHeaderBuf.flip();

        IntBuffer dataSizes = datasizesFromHeaderBuf(mHeaderSize, mHeaderBuf);
        return new FinishedHeader(mHeaderSize, mAllDataSize, dataSizes);
    }

    private static IntBuffer datasizesFromHeaderBuf(int headerSize, ByteBuffer headerBuf) {
        int dataCount = headerSize / 4 - 2;
        IntBuffer dataSizes = IntBuffer.allocate(dataCount);
        while (headerBuf.hasRemaining()) {
            dataSizes.put(headerBuf.getInt());
        }
        dataSizes.flip();
        return dataSizes;
    }

    @Override
    public int size() {
        return mHeaderSize;
    }

    @Override
    public int allDataSize() {
        return mAllDataSize;
    }

    @Override
    public int itemDataSize(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntBuffer dataSizeBuffer() {
        return EMPTY_INT_BUFFER;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadFinished() {
        return false;
    }
}
