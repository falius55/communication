package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.bluetooth.BluetoothVisitor;
import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;

public class BluetoothWritingHandler {
    private static final Logger log = LoggerFactory.getLogger(BluetoothWritingHandler.class);
    private final BluetoothVisitor mVisitor;
    private OnSendListener mListener;

    public BluetoothWritingHandler(BluetoothVisitor visitor) {
        mVisitor = visitor;
    }

    public BluetoothWritingHandler addOnSendListener(OnSendListener listener) {
        mListener = listener;
        return this;
    }

    public void handle(SendData data) throws IOException {
        log.debug("writing handler");
        try (OutputStream os = mVisitor.getOutputStream()) {
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
            if (mListener != null) {
                mListener.onSend(writeBytes);
            }

            if (mVisitor.doContinue()) {
                log.debug("do continue");
                BluetoothReadingHandler receiver = new BluetoothReadingHandler(mVisitor);
                receiver.handle();
            }
        }
    }
}
