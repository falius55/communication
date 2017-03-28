package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.bluetooth.BluetoothVisitor;
import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.BasicReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;

public class BluetoothReadingHandler {
    private static final Logger log = LoggerFactory.getLogger(BluetoothReadingHandler.class);

    private OnReceiveListener mListener = null;
    private final BluetoothVisitor mVisitor;

    public BluetoothReadingHandler(BluetoothVisitor visitor) {
        mVisitor = visitor;
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mListener = listener;
    }

    public void receive() throws IOException {
        InputStream is = mVisitor.getInputStream();
        Header header = HeaderFactory.from(is);
        Entry entry = new Entry(header);
        int readBytes = entry.read(is);
        ReceiveData data = entry.getData();

        mListener.onReceive(mVisitor.toString(), readBytes, data);

        BluetoothWritingHandler sender = new BluetoothWritingHandler(mVisitor);
        sender.send(mVisitor.newSendData(data));
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
            log.debug("all data size: {}", mHeader.allDataSize());
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

        private int read(InputStream is) throws IOException {
            initItemData();

            int readed = 0;
            for (ByteBuffer itemBuf : mItemData) {
                byte[] bytes = new byte[itemBuf.limit()];
                int tmp = is.read(bytes);
                if (tmp < 0) {
                    return -1;
                }
                itemBuf.put(bytes);
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
