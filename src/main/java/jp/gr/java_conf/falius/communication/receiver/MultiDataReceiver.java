package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.Header;

/**
 * 複数データの受信を管理するクラスです
 * MultiDataSenderによって送信された際に利用します
 *
 * Senderによってputされたのと同じ順番でデータが格納されますので、getXXXメソッドにて取得してください。
 * 取得されたデータはこのクラスの内部から削除されます。
 *
 * OnReceiverListenerの引数で渡されるReceiverオブジェクトから消費した受信データは
 *  Client#startメソッドの戻り値で渡されるReceiverオブジェクトには含まれていませんので注意してください。
 * @author "ymiyauchi"
 *
 */
public class MultiDataReceiver implements Receiver {
    private static final Logger log = LoggerFactory.getLogger(MultiDataReceiver.class);

    private final Queue<ByteBuffer> mReceivedData = new ArrayDeque<>();
    private Entry mNonFinishedEntry = null;

    private OnReceiveListener mListener = null;

    public MultiDataReceiver() {
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mListener = listener;
    }

    @Override
    public Result receive(SocketChannel channel) throws IOException {
        // もし送信側がByteBufferの配列を使って送信してきても、
        //  受け取り側ではその内容がすべて連結されて送られてくる

        Header header;
        Entry entry;
        if (mNonFinishedEntry == null) {
            try {
                header = Header.from(channel);
                log.debug("header size: {}", header.size());
            } catch (IOException e) {
                return Result.ERROR;
            }
            entry = new Entry(header);
        } else {
            header = mNonFinishedEntry.mHeader;
            entry = mNonFinishedEntry;
        }

        int tmp = entry.read(channel);
        log.debug("once data read: {}", tmp);
        if (tmp < 0) {
            return Result.ERROR;
        }

        if (entry.isFinished()) {
            log.debug("reading finish");
            entry.add(mReceivedData);
            if (mListener != null) {
                String remoteAddress = channel.socket().getRemoteSocketAddress().toString();
                mListener.onReceive(remoteAddress, header.allDataSize(), this);
            }
            mNonFinishedEntry = null;
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
        private final Header mHeader;
        private int mRemain;
        private final Queue<ByteBuffer> mItemData;

        private Entry(Header header) {
            mHeader = header;
            mRemain = header.allDataSize() - header.size();
            mItemData = new ArrayDeque<>();
            log.debug("all data size: {}", header.allDataSize());
            init();
        }

        private void init() {
            IntBuffer sizeBuf = mHeader.dataSizeBuffer();
            while (sizeBuf.hasRemaining()) {
                int size = sizeBuf.get();
                log.debug("data size: {}", size);
                ByteBuffer buf = ByteBuffer.allocate(size);
                mItemData.add(buf);
            }
        }

        private int read(SocketChannel channel) throws IOException {
            int readed = 0;
            for (ByteBuffer itemBuf : mItemData) {
                int tmp = channel.read(itemBuf);
                if (tmp < 0) {
                    return -1;
                }
                readed += tmp;
                log.debug("item read: {}", tmp);
            }
            mRemain -= readed;
            return readed;
        }

        private void add(Queue<ByteBuffer> dst) {
            if (!isFinished()) {
                return;
            }
            for (ByteBuffer item : mItemData) {
                item.flip();
                dst.add(item);
            }
        }

        private boolean isFinished() {
            return mRemain == 0;
        }
    }

    /**
     * @return 保持している受信データがあればそのデータ。なければnull
     */
    @Override
    public ByteBuffer get() {
        ByteBuffer data = mReceivedData.poll();
        log.debug("data: {}", data);
        log.debug("rest data count: {}", mReceivedData.size());
        return data;
    }

    @Override
    public ByteBuffer[] getAll() {
        ByteBuffer[] ret = mReceivedData.toArray(new ByteBuffer[0]);
        mReceivedData.clear();
        return ret;
    }

    @Override
    public int dataCount() {
        return mReceivedData.size();
    }

    @Override
    public void clear() {
        mReceivedData.clear();
    }

    /**
     * @throws CharcterCodingException
     */
    @Override
    public String getString() {
        ByteBuffer buf = get();
        if (buf == null) {
            log.warn("no data null");
            return null;
        }
        String ret = StandardCharsets.UTF_8.decode(buf).toString();
        return ret;
    }

    @Override
    public int getInt() {
        ByteBuffer buf = get();
        int ret = buf.getInt();
        return ret;
    }

    @Override
    public boolean getBoolean() {
        return getInt() == 1;
    }

    @Override
    public void getAndOutput(OutputStream os) throws IOException {
        ByteBuffer buf = get();
        try (OutputStream out = os) {
            if (buf.hasArray()) {
                byte[] bytes = buf.array();
                out.write(bytes);
            } else {
                for (int i = buf.position(), len = buf.limit(); i < len; i++) {
                    out.write(buf.get());
                }
            }
        }
    }
}
