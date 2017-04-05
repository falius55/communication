package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import jp.gr.java_conf.falius.communication.bluetooth.Session;
import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.rcvdata.BasicReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;

public class BluetoothReadingHandler implements BluetoothHandler {

    private final Session mSession;

    public BluetoothReadingHandler(Session session) {
        mSession = session;
    }

    public void handle() throws Exception {
        InputStream is = mSession.getInputStream();
        Header header = HeaderFactory.from(is);
        if (header == null) {
            mSession.disconnect(null);
            return;
        }
        Entry entry = new Entry(header);
        int readBytes = entry.read(is);
        if (readBytes < 0) {
            mSession.disconnect(null);
            return;
        }
        ReceiveData data = entry.getData();

        mSession.onReceive(data);

        SendData sendData = mSession.newSendData(data);
        BluetoothHandler handler = new BluetoothWritingHandler(mSession, sendData);
        mSession.setHandler(handler);
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
            initItemData();
        }

        private void initItemData() {
            mItemData = new ArrayDeque<>();
            IntBuffer sizeBuf = mHeader.dataSizeBuffer();
            while (sizeBuf.hasRemaining()) {
                int size = sizeBuf.get();
                ByteBuffer buf = ByteBuffer.allocate(size);
                mItemData.add(buf);
            }
        }

        private int read(InputStream is) throws IOException {
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
