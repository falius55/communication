package jp.gr.java_conf.falius.communication.sender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.senddata.SendData;

/**
 * 複数データを送信するクラスです。
 *
 * 再利用はできません。
 * @author "ymiyauchi"
 *
 */
public class MultiDataSender implements Sender {
    private final OnSendListener mListener;
    private final ByteBuffer mData;

    public MultiDataSender(SendData data, OnSendListener listener) {
        mData = initBuffer(data);
        mListener = listener;
    }

    private ByteBuffer initBuffer(SendData data) {
        Header header = HeaderFactory.from(data);
        ByteBuffer headerBuf = header.toByteBuffer();
        int size = headerBuf.limit();
        for (ByteBuffer item : data) {
            size += item.limit();
        }
        ByteBuffer ret = ByteBuffer.allocate(size);
        ret.put(headerBuf);
        for (ByteBuffer item : data) {
            ret.put(item);
        }
        ret.flip();
        if (ret.limit() != header.allDataSize()) {
            throw new IllegalStateException();
        }
        return ret;
    }

    @Override
    public final Result send(SocketChannel channel) throws IOException {
        channel.write(mData);
        if (mData.hasRemaining()) {
            return Result.UNFINISHED;
        }
            if (mListener != null) {
                String remoteAddress = channel.socket().getInetAddress().toString();
                mListener.onSend(remoteAddress);
            }
            return Result.FINISHED;
    }
}
