package jp.gr.java_conf.falius.communication.core.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.rcvdata.BasicReceiveData;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;

class BluetoothReadingHandler implements BluetoothHandler {
    private static final Logger log = LoggerFactory.getLogger(BluetoothReadingHandler.class);

    private final Session mSession;

    public BluetoothReadingHandler(Session session) {
        mSession = session;
    }

    public void handle() throws Exception {
        log.debug("reading handler");
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
        log.debug("data get");
        ReceiveData data = entry.getData();

        log.debug("on receive");
        mSession.onReceive(data);

        log.debug("get sendData");
        SendData sendData = mSession.newSendData(data);
        log.debug("writing instance");
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
            log.debug("all data size: {}", mHeader.allDataSize());
            initItemData();
        }

        private void initItemData() {
            mItemData = new ArrayDeque<>();
            IntBuffer sizeBuf = mHeader.dataSizeBuffer();
            log.debug("data size buffer : {}", sizeBuf);
            while (sizeBuf.hasRemaining()) {
                int size = sizeBuf.get();
                log.debug("item buf size", size);
                ByteBuffer buf = ByteBuffer.allocate(size);
                mItemData.add(buf);
            }
        }

        private int read(InputStream is) throws IOException {
            log.debug("entry read");
            int readed = 0;
            for (ByteBuffer itemBuf : mItemData) {
                byte[] bytes = new byte[itemBuf.limit()];
                int tmp = is.read(bytes);
                if (tmp < 0) {
                    log.debug("read error -1 return");
                    return -1;
                }
                log.debug("read bytes", tmp);
                itemBuf.put(bytes);
                readed += tmp;
            }
            mRemain -= readed;
            log.debug("entry readed : mRemain is {}", mRemain);
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
