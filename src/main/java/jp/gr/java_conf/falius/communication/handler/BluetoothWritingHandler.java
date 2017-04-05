package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import jp.gr.java_conf.falius.communication.bluetooth.Session;
import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.senddata.SendData;

public class BluetoothWritingHandler implements BluetoothHandler {
    private final Session mSession;
    private final SendData mSendData;

    public BluetoothWritingHandler(Session session, SendData data) {
        mSession = session;
        mSendData = data;
    }

    public void handle() throws IOException {
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

        mSession.onSend();

        if (mSession.doContinue()) {
            BluetoothHandler handler = new BluetoothReadingHandler(mSession);
            mSession.setHandler(handler);
        } else {
            mSession.disconnect(null);
        }
    }
}
