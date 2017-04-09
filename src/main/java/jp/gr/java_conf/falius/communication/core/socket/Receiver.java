package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.rcvdata.BasicReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

/**
 * 複数データの受信を管理するクラスです
 * Senderとは異なり、一度の通信の間保持されます。
 * @author "ymiyauchi"
 *
 */
class Receiver {
    private static final Logger log = LoggerFactory.getLogger(Receiver.class);
    enum Result {
        ERROR, UNFINISHED, FINISHED, DISCONNECT,
    }

    private ReceiveData mLatestData = null;
    private Entry mNonFinishedEntry = null;

    private OnReceiveListener mListener = null;

    public void addOnReceiveListener(OnReceiveListener listener) {
        mListener = listener;
    }

    /**
     * @return 最新の受信データ。まだ一度も受信していなければnull
     */
    public ReceiveData getData() {
        return mLatestData;
    }

    /**
     * チャネルからデータを読み取ります。
     *
     * @param channel 受信用ソケット・チャネル
     * @return すべての受信データを読み取ればFINISHED, まだ受信していないデータが残った状態で終わったなら
     * UNFINISHED, 通信が途切れるなどして受信できなかった場合にはERROR
     * @throws IOException チャネルからの読み取りで例外が発生した場合
     */
    public Result receive(SocketChannel channel) throws IOException {
        // もし送信側がByteBufferの配列を使って送信してきても、
        //  受け取り側ではその内容がすべて連結されて送られてくる

        Header header;
        Entry entry;
        if (mNonFinishedEntry == null) {
            try {
                header = HeaderFactory.from(channel);
            } catch (IOException e) {
                log.warn("header reading error", e);
                return Result.ERROR;
            }
            if (header == null) {
                log.warn("header could not read. disconnect");
                return Result.DISCONNECT;
            }
            entry = new Entry(header);
        } else {
            header = mNonFinishedEntry.mHeader;
            entry = mNonFinishedEntry;
        }

        int tmp = entry.read(channel);
        if (tmp < 0) {
            log.warn("recieve read returns -1");
            return Result.ERROR;
        }

        if (entry.isFinished()) {
            mLatestData = entry.getData();
            if (mListener != null) {
                String remoteAddress = channel.socket().getRemoteSocketAddress().toString();
                mListener.onReceive(remoteAddress, getData());
            }
            mNonFinishedEntry = null;
            log.debug("reading finish");
            return Result.FINISHED;
        } else {
            mNonFinishedEntry = entry;
            return Result.UNFINISHED;
        }
    }

    /**
     * 一度の受信単位
     * @author "ymiyauchi"
     *
     */
    private static class Entry {
        private Header mHeader;
        private int mRemain;
        private Queue<ByteBuffer> mItemData = null;

        private Entry(Header header) {
            mHeader = header;
            mRemain = mHeader.allDataSize() - mHeader.size();
        }

        private void initItemData() {
            if (mItemData != null) {
                return;
            }
            mItemData = new ArrayDeque<>();
            IntBuffer sizeBuf = mHeader.dataSizeBuffer();
            while (sizeBuf.hasRemaining()) {
                int size = sizeBuf.get();
                ByteBuffer buf = ByteBuffer.allocate(size);
                mItemData.add(buf);
            }
        }

        private int read(SocketChannel channel) throws IOException {
            mHeader = mHeader.read(channel);
            if (!mHeader.isReadFinished()) {
                return 0;
            }
            initItemData();

            int readed = 0;
            for (ByteBuffer itemBuf : mItemData) {
                int tmp = channel.read(itemBuf);
                if (tmp < 0) {
                    log.warn("entry read retuns -1");
                    return -1;
                }
                readed += tmp;
            }
            mRemain -= readed;
            return readed;
        }

        private ReceiveData getData() {
            if (!isFinished()) {
                return null;
            }
            for (ByteBuffer data : mItemData) {
                data.flip();
            }
            return new BasicReceiveData(mItemData);
        }

        private boolean isFinished() {
            return mHeader.isReadFinished() && mRemain == 0;
        }
    }
}
