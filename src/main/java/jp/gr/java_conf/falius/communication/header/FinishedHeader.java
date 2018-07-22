package jp.gr.java_conf.falius.communication.header;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 受信時に読み取りが完全に完了したヘッダ、あるいは書き込み時のヘッダを表すクラス
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
class FinishedHeader implements Header {
    private static final Logger log = LoggerFactory.getLogger(FinishedHeader.class);
    // ヘッダーのサイズ(自身を含む), 全データのサイズ(ヘッダーを含む), データ１のサイズ, データ２のサイズ...
    private final int mHeaderSize;  // ヘッダー自身のサイズ
    private final int mAllDataSize;  // ヘッダー含む全データサイズ
    private final IntBuffer mItemDataSizes;  // 各アイテムごとのデータサイズ

    /**
     *
     * @param headerSize
     * @param allDataSize
     * @param itemDataSizes
     * @since 1.4.0
     */
    FinishedHeader(int headerSize, int allDataSize, IntBuffer itemDataSizes) {
        mHeaderSize = headerSize;
        mAllDataSize = allDataSize;
        mItemDataSizes = itemDataSizes.asReadOnlyBuffer();
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
        mItemDataSizes.rewind();
        return mItemDataSizes;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public ByteBuffer toByteBuffer() {
        // ヘッダーのサイズ(自身を含む), 全データのサイズ(ヘッダーを含む), データ１のサイズ, データ２のサイズ...
        IntBuffer dataSizes = mItemDataSizes;
        dataSizes.rewind();
        ByteBuffer ret = ByteBuffer.allocate(mHeaderSize);
        ret.putInt(mHeaderSize);
        ret.putInt(mAllDataSize);
        while (dataSizes.hasRemaining()) {
            ret.putInt(dataSizes.get());
        }
        ret.flip();
        return ret;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public Header read(SocketChannel channel) {
        // empty
        return this;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public boolean isReadFinished() {
        return true;
    }
}