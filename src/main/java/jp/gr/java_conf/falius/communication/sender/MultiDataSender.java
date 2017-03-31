package jp.gr.java_conf.falius.communication.sender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(MultiDataSender.class);
    private OnSendListener mListener = null;
    private final ByteBuffer mData;

    public MultiDataSender(SendData data) {
        if (!data.hasRemain()) {
            log.debug("data is empty");
        }
        mData = initBuffer(data);
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
    public Sender addOnSendListener(OnSendListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public final Result send(SocketChannel channel) throws IOException {
        channel.write(mData);
        if (mData.hasRemaining()) {
            return Result.UNFINISHED;
        }
            if (mListener != null) {
                mListener.onSend(mData.limit());
            }
            log.debug("writing finish");
            return Result.FINISHED;
    }
}
