package jp.gr.java_conf.falius.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Header {
    private static final Logger log = LoggerFactory.getLogger(Header.class);
    // ヘッダーのサイズ(自身を含む), 全データのサイズ(ヘッダーを含む), データ１のサイズ, データ２のサイズ...
    private final int mHeaderSize;
    private final int mAllDataSize;  // ヘッダー含む
    private final IntBuffer mItemDataSizes;

    private Header(int headerSize, int allDataSize, IntBuffer itemDataSizes) {
        mHeaderSize = headerSize;
        mAllDataSize = allDataSize;
        mItemDataSizes = itemDataSizes.asReadOnlyBuffer();
    }

    public static Header from(Collection<ByteBuffer> data) {
        IntBuffer buf = IntBuffer.allocate(data.size());
        int headerSize = 4 + 4 + data.size() * 4;
        int dataSize = headerSize;
        for (ByteBuffer elem : data) {
            int size = elem.limit();
            dataSize += size;
            buf.put(size);
            log.debug("data size: {}", size);
        }
        buf.flip();
        return new Header(headerSize, dataSize, buf);
    }

    public static Header from(SocketChannel channel) throws IOException {
        // FIXME: ヘッダーは一度での読み取りが前提になっている
        int read = 0;

        ByteBuffer headerSizeBuf = ByteBuffer.allocate(8);
        int tmp = channel.read(headerSizeBuf);
        headerSizeBuf.flip();
        read += tmp;
        if (tmp < 0) {
            throw new IOException();
        }
        int headerSize = headerSizeBuf.getInt();
        int dataSize = headerSizeBuf.getInt();

        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize - 8);
        tmp = channel.read(headerBuf);
        headerBuf.flip();
        read += tmp;
        if (tmp < 0) {
            throw new IOException();
        }

        assert (read == headerSize);

        int dataCount = headerSize / 4 - 2;
        IntBuffer dataSizes = IntBuffer.allocate(dataCount);
        while (headerBuf.hasRemaining()) {
            dataSizes.put(headerBuf.getInt());
        }
        dataSizes.flip();
        return new Header(headerSize, dataSize, dataSizes);
    }

    public int size() {
        return mHeaderSize;
    }

    public int allDataSize() {
        return mAllDataSize;
    }

    public int itemDataSize(int index) {
        int size = mItemDataSizes.get(index);
        log.debug("data size: {}", size);
        return size;
    }

    public IntBuffer dataSizeBuffer() {
        mItemDataSizes.rewind();
        return mItemDataSizes;
    }

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

}