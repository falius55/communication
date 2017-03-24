package jp.gr.java_conf.falius.communication.sender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Senderを拡張する場合、このクラスを継承してください
 * @author "ymiyauchi"
 *
 */
public abstract class ExtendableSender implements Sender {
    private final Sender mSource;

    public ExtendableSender(Sender sender) {
        mSource = sender;
    }

    @Override
    public Result send(SocketChannel channel) throws IOException {
        return mSource.send(channel);
    }

    @Override
    public Sender addOnSendListener(OnSendListener listener) {
        return mSource.addOnSendListener(listener);
    }

    @Override
    public Sender put(ByteBuffer buf) {
        return mSource.put(buf);
    }

    @Override
    public Sender put(ByteBuffer[] bufs) {
        return mSource.put(bufs);
    }

    @Override
    public Sender put(byte[] bytes) {
        return mSource.put(bytes);
    }

    @Override
    public Sender put(int num) {
        return mSource.put(num);
    }

    @Override
    public Sender put(boolean bl) {
        return mSource.put(bl);
    }

    @Override
    public Sender put(String msg) {
        return mSource.put(msg);
    }

    @Override
    public Sender put(InputStream in) throws IOException {
        return mSource.put(in);
    }
}
