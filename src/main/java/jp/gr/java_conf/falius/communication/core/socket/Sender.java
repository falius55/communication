package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.senddata.SendData;

/**
 *
 * <p>
 * 送信データを管理するクラスです。
 *
 * <p>
 * 接続の間ずっと共有されるReceiverオブジェクトとは異なり、こちらは一度の送信ごとに使い捨てとなります。
 * そのため、再利用はできません。
 * @author "ymiyauchi"
 *
 */
class Sender {
    private static final Logger log = LoggerFactory.getLogger(Sender.class);
    enum Result {
        FINISHED, UNFINISHED,
    }
    private final OnSendListener mListener;
    private final ByteBuffer mData;

    public Sender(SendData data, OnSendListener listener) {
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

    /**
     * 実際の送信を行うメソッドです。
     *
     * @param channel 送信するチャネル
     * @return 送信予定のデータをすべて送信し終えた場合はFINISHED, まだ未送信のデータが残っていればUNFINISHED
     * @throws IOException
     */
    public final Result send(SocketChannel channel) throws IOException {
        channel.write(mData);
        if (mData.hasRemaining()) {
            return Result.UNFINISHED;
        }
            if (mListener != null) {
                String remoteAddress = channel.socket().getInetAddress().toString();
                mListener.onSend(remoteAddress);
            }
            log.debug("writing finish");
            return Result.FINISHED;
    }
}
