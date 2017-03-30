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

public class BluetoothWritingHandler {
    private static final Logger log = LoggerFactory.getLogger(BluetoothWritingHandler.class);
    private final Session mSession;

    public BluetoothWritingHandler(Session session) {
        mSession = session;
    }

    public void handle(SendData data) throws IOException {
        log.debug("writing handler");
        try (OutputStream os = mSession.getOutputStream()) {
            Header header = HeaderFactory.from(data);
            ByteBuffer headerBuf = header.toByteBuffer();
            byte[] headerBytes = headerBuf.array();
            os.write(headerBytes);

            int writeBytes = header.size();
            for (ByteBuffer buf : data) {
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
                BluetoothReadingHandler receiver = new BluetoothReadingHandler(mSession);
                receiver.handle();
            }
        }
    }
}
