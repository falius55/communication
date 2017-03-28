package jp.gr.java_conf.falius.communication.sender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;

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
    private final SendData mData;
    private State mState = null;
    private boolean mIsWritten = false;

    public MultiDataSender(SendData data) {
        if (!data.hasRemain()) {
            log.debug("data is empty");
        }
        mData = data;
    }

    @Override
    public Sender addOnSendListener(OnSendListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public final Result send(SocketChannel channel) throws IOException {
        if (mIsWritten) {
            throw new IllegalStateException("send data is already written");
        }
        State state;
        if (mState == null) {
            state = mState = new State();
            state.mHeader = HeaderFactory.from(mData);
            state.mHeaderBuffer = state.mHeader.toByteBuffer();
        } else {
            state = mState;
        }

        state.mWriteSize += channel.write(state.mHeaderBuffer);
        boolean hasRemain = false;
        for (ByteBuffer item : mData) {
            hasRemain = hasRemain && item.hasRemaining();
            state.mWriteSize += channel.write(item);
        }

        log.debug("state.writeSize: {}", state.mWriteSize);
        log.debug("mHeader.allDataSize(): {}", state.mHeader.allDataSize());
        if (state.isWrittenFinished()) {
            if (mListener != null) {
                mListener.onSend(state.mWriteSize);
            }
            mIsWritten = true;
            log.debug("writing finish");
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
        private Header mHeader;
        private ByteBuffer mHeaderBuffer;
        private int mWriteSize = 0;

        private boolean isWrittenFinished() {
            return mWriteSize == mHeader.allDataSize();
        }
    }
}
