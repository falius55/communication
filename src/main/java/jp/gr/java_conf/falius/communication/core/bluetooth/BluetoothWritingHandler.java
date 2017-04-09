package jp.gr.java_conf.falius.communication.core.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.senddata.SendData;

class BluetoothWritingHandler implements BluetoothHandler {
    private static final Logger log = LoggerFactory.getLogger(BluetoothWritingHandler.class);
    private final Session mSession;
    private final SendData mSendData;

    BluetoothWritingHandler(Session session, SendData data) {
        mSession = session;
        mSendData = data;
    }

    public void handle() throws IOException {
        log.debug("writing handle");
        OutputStream os = mSession.getOutputStream();
        Header header = HeaderFactory.from(mSendData);
        ByteBuffer headerBuf = header.toByteBuffer();
        byte[] headerBytes = headerBuf.array();
        os.write(headerBytes);

        header.size();
        for (ByteBuffer buf : mSendData) {
            byte[] b = buf.array();
            os.write(b);
        }
        os.flush();

        mSession.onSend();

        if (mSession.isClient() || mSession.doContinue()) {
            BluetoothHandler handler = new BluetoothReadingHandler(mSession);
            mSession.setHandler(handler);
        } else {
            mSession.disconnect(null);
        }
    }
}
