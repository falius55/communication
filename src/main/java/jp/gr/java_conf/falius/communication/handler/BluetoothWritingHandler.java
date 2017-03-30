package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.bluetooth.Session;
import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.senddata.SendData;

public class BluetoothWritingHandler implements BluetoothHandler {
    private static final Logger log = LoggerFactory.getLogger(BluetoothWritingHandler.class);
    private final Session mSession;
    private final SendData mSendData;

    public BluetoothWritingHandler(Session session, SendData data) {
        mSession = session;
        mSendData = data;
    }

    public void handle() throws IOException {
        log.debug("writing handler");
        OutputStream os = mSession.getOutputStream();
        Header header = HeaderFactory.from(mSendData);
        ByteBuffer headerBuf = header.toByteBuffer();
        byte[] headerBytes = headerBuf.array();
        os.write(headerBytes);

        int writeBytes = header.size();
        for (ByteBuffer buf : mSendData) {
            byte[] b = buf.array();
            os.write(b);
            writeBytes += b.length;
        }
        os.flush();

        log.debug("write size: {}", writeBytes);
        log.debug("on send listener");
        mSession.onSend(writeBytes);

        if (mSession.doContinue()) {
            log.debug("do continue");
            BluetoothHandler handler = new BluetoothReadingHandler(mSession);
            mSession.setHandler(handler);
        } else {
            mSession.disconnect(null);
        }
    }
}
