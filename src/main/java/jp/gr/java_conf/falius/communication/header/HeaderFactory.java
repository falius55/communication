package jp.gr.java_conf.falius.communication.header;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderFactory {
    private static final Logger log = LoggerFactory.getLogger(HeaderFactory.class);

    public static FinishedHeader from(Collection<ByteBuffer> data) {
        IntBuffer buf = IntBuffer.allocate(data.size());
        int headerSize = 4 + 4 + data.size() * 4;
        int dataSize = headerSize;
        for (ByteBuffer elem : data) {
            int size = elem.limit();
            dataSize += size;
            buf.put(size);
        }
        buf.flip();
        return new FinishedHeader(headerSize, dataSize, buf);
    }

    public static Header from(SocketChannel channel) throws IOException {
        ByteBuffer headerSizeBuf = ByteBuffer.allocate(8);
        int tmp = channel.read(headerSizeBuf);
        if (tmp < 0) {
            throw new IOException();
        }
        headerSizeBuf.flip();
        int headerSize = headerSizeBuf.getInt();
        int dataSize = headerSizeBuf.getInt();

        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize - 8);

        UnFinishedHeader header = new UnFinishedHeader(headerSize, dataSize, headerBuf);
        return header.read(channel);

    }
}
