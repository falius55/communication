package jp.gr.java_conf.falius.communication.header;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.senddata.SendData;

/**
 * ヘッダを作成するファクトリクラス
 * 相手がすでに切断していて読み取りができない場合はnullを返します。
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public class HeaderFactory {
    private static final Logger log = LoggerFactory.getLogger(HeaderFactory.class);

    /**
     *
     * @param data
     * @return 読み取りが完全に終わったヘッダ
     * @since 1.4.0
     */
    public static Header from(SendData data) {
        IntBuffer buf = IntBuffer.allocate(data.size());
        int headerSize = 4 + 4 + data.size() * 4;
        int dataSize = headerSize;
        for (ByteBuffer elem : data) {
            int size = elem.limit();
            dataSize += size;
            buf.put(size);
        }
        buf.flip();
        log.debug("send header size : {}", headerSize);
        log.debug("send all data size: {}", dataSize);
        return new FinishedHeader(headerSize, dataSize, buf);
    }

    /**
     * チャネルからヘッダ情報を読み込みます。
     * ヘッダは一度にすべてを読み込まれる必要はありませんが、最低でも８バイトは読み込まれる必要があります。
     * @param channel
     * @return 読み取りが完全に終わったヘッダ、あるいはまだ読み取りが完全に終わっていないヘッダ
     * @throws IOException ヘッダの読み込みエラーが起きた場合、８バイト未満しか読み込めなかった場合
     * @since 1.4.0
     */
    public static Header from(SocketChannel channel) throws IOException {
        ByteBuffer headerSizeBuf = ByteBuffer.allocate(8);
        int tmp = channel.read(headerSizeBuf);
        if (tmp < 0) {
            return null;
        }
        if (tmp < 8) {
            throw new IOException("read less than 8 bytes");
        }
        headerSizeBuf.flip();
        int headerSize = headerSizeBuf.getInt();
        int dataSize = headerSizeBuf.getInt();

        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize - 8);

        Header header = new UnFinishedHeader(headerSize, dataSize, headerBuf);
        return header.read(channel);

    }

    /**
     *
     * @param is
     * @return 読み取りが完全に終わったヘッダ
     * @throws IOException ヘッダの読み取りエラーが起きた場合、データが8バイト未満の場合
     */
    public static Header from(InputStream is) throws IOException {
        byte[] headerBytes = new byte[8];
        int tmp = is.read(headerBytes);
        if (tmp < 0) {
            return null;
        }
        if (tmp < 8) {
            throw new IOException("read less than 8 bytes");
        }
        ByteBuffer headerSizeBuf = ByteBuffer.allocate(8);
        headerSizeBuf.put(headerBytes);
        headerSizeBuf.flip();
        int headerSize = headerSizeBuf.getInt();
        int dataSize = headerSizeBuf.getInt();

        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize - 8);
        byte[] remainHeaderBytes = new byte[headerSize - 8];
        tmp = is.read(remainHeaderBytes);
        headerBuf.put(remainHeaderBytes);
        headerBuf.flip();

        IntBuffer dataSizes = UnFinishedHeader.datasizesFromHeaderBuf(headerSize, headerBuf);

        Header header = new FinishedHeader(headerSize, dataSize, dataSizes);
        return header;
    }
}
