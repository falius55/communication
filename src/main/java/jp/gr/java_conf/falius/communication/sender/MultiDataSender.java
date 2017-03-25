package jp.gr.java_conf.falius.communication.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;

/**
 * 複数データを送信する際に利用するクラスです。
 *
 * 再利用はできません。Swapper#swapメソッドでは必ず新しく作成したインスタンスを
 * 返すようにしてください。
 * @author "ymiyauchi"
 *
 */
public class MultiDataSender implements Sender {
    private static final Logger log = LoggerFactory.getLogger(MultiDataSender.class);
    private OnSendListener mListener = null;
    private final Deque<ByteBuffer> mData = new ArrayDeque<>();
    private State mState = null;

    public MultiDataSender() {
    }

    @Override
    public Sender addOnSendListener(OnSendListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public final Result send(SocketChannel channel) throws IOException {
        State state;
        if (mState == null) {
            state = mState = new State();
            state.mHeader = HeaderFactory.from(mData);
            state.mHeaderBuffer = state.mHeader.toByteBuffer();
        } else {
            state = mState;
        }

        state.mWriteSize += channel.write(state.mHeaderBuffer);
        for (ByteBuffer item : mData) {
            state.mWriteSize += channel.write(item);
        }

        log.debug("state.writeSize: {}", state.mWriteSize);
        log.debug("mHeader.allDataSize(): {}", state.mHeader.allDataSize());
        if (state.mWriteSize == state.mHeader.allDataSize()) {
            if (mListener != null) {
                mListener.onSend(state.mWriteSize);
            }
            return Result.FINISHED;
        } else {
            log.debug("written is not finished");
            return Result.UNFINISHED;
        }
    }

    /**
     * 書き込みが一度で終わらなかったときのために
     * 各情報を保持する
     * @author "ymiyauchi"
     *
     */
    private static class State {
        // 一度で最後までヘッダーを書き込めなかったときのために
        // 以前読み込んだデータの状態を覚えておくためのクラス
        private Header mHeader;
        private ByteBuffer mHeaderBuffer;
        private int mWriteSize = 0;
    }

    @Override
    public final Sender put(ByteBuffer buf) {
        mData.add(buf);
        return this;
    }

    @Override
    public final Sender put(ByteBuffer[] bufs) {
        mData.addAll(Arrays.asList(bufs));
        return this;
    }

    @Override
    public Sender put(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.allocate(bytes.length);
        buf.put(bytes);
        buf.flip();
        return put(buf);
    }

    @Override
    public Sender put(String str) {
        return put(str.getBytes(StandardCharsets.UTF_8));
    }

    public Sender put(Path filePath) throws IOException {
        return put(Files.readAllBytes(filePath));
    }

    @Override
    public Sender put(int num) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(num);
        buf.flip();
        return put(buf);
    }

    @Override
    public Sender put(boolean bl) {
        return put(bl ? 1 : 0);
    }

    @Override
    public Sender put(InputStream is) throws IOException {
        final int READ_SIZE = 4096 * 2;
        int size = READ_SIZE;
        ByteBuffer result = ByteBuffer.allocate(size);
        byte[] bytes = new byte[READ_SIZE];
        int len;
        while ((len = is.read(bytes)) != -1) {
            int rest = size - result.position();
            if (rest < len) {
                size += READ_SIZE;
                ByteBuffer newBuf = ByteBuffer.allocate(size);
                result.flip();
                newBuf.put(result);
                result = newBuf;
            }

            result.put(bytes, 0, len);
        }

        result.flip();
        return put(result);
    }

    public Sender put(File file) throws FileNotFoundException, IOException {
        return put(new FileInputStream(file));
    }

}
