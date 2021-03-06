package jp.gr.java_conf.falius.communication.header;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
class UnFinishedHeader implements Header {
    private static final IntBuffer EMPTY_INT_BUFFER = IntBuffer.allocate(0);

    private final int mHeaderSize;  // ヘッダー自身のサイズ
    private final int mAllDataSize;  // ヘッダー含む全データサイズ
    private final ByteBuffer mHeaderBuf;

    /**
     *
     * @param headerSize
     * @param allDataSize
     * @param headerBuf 必要な容量を確保した、空のバッファ
     * @since 1.4.0
     */
    UnFinishedHeader(int headerSize, int allDataSize, ByteBuffer headerBuf) {
        mHeaderSize = headerSize;
        mAllDataSize = allDataSize;
        mHeaderBuf = headerBuf;
    }

    /**
     * @since 1.4.0
     */
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

    /**
     * @since 1.4.0
     */
    static IntBuffer datasizesFromHeaderBuf(int headerSize, ByteBuffer headerBuf) {
        int dataCount = headerSize / 4 - 2;
        IntBuffer dataSizes = IntBuffer.allocate(dataCount);
        while (headerBuf.hasRemaining()) {
            dataSizes.put(headerBuf.getInt());
        }
        dataSizes.flip();
        return dataSizes;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public int size() {
        return mHeaderSize;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public int allDataSize() {
        return mAllDataSize;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public IntBuffer dataSizeBuffer() {
        return EMPTY_INT_BUFFER;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public ByteBuffer toByteBuffer() {
        throw new IllegalStateException();
    }

    /**
     * @since 1.4.0
     */
    @Override
    public boolean isReadFinished() {
        return false;
    }
}
