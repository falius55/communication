package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import jp.gr.java_conf.falius.communication.bluetooth.BluetoothVisitor;
import jp.gr.java_conf.falius.communication.header.Header;
import jp.gr.java_conf.falius.communication.header.HeaderFactory;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;

public class BluetoothWritingHandler {
    private final BluetoothVisitor mVisitor;
    private OnSendListener mListener;

    public BluetoothWritingHandler(BluetoothVisitor visitor) {
        mVisitor = visitor;
    }

    public BluetoothWritingHandler addOnSendListener(OnSendListener listener) {
        mListener = listener;
        return this;
    }

    public void send(SendData data) throws IOException {
        OutputStream os = mVisitor.getOutputStream();

        Header header = HeaderFactory.from(data);
        ByteBuffer headerBuf = header.toByteBuffer();
        byte[] headerBytes = headerBuf.array();
        os.write(headerBytes);

        int writeBytes = 0;
        for (ByteBuffer buf : data) {
            byte[] b = buf.array();
            os.write(b);
            writeBytes += b.length;
        }
        os.flush();

        mListener.onSend(writeBytes);

        if (mVisitor.doContinue()) {
            BluetoothReadingHandler receiver = new BluetoothReadingHandler(mVisitor);
            receiver.receive();
        }
    }
}
