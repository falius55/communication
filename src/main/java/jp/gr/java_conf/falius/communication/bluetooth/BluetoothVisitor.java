package jp.gr.java_conf.falius.communication.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public class BluetoothVisitor implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(BluetoothVisitor.class);
    private final StreamConnection mConnection;
    private final Swapper mSwapper;
    private final InputStream mIn;
    private final OutputStream mOut;

    public BluetoothVisitor(StreamConnection connection, Swapper swapper) throws IOException {
        mConnection = connection;
        mSwapper = swapper;
        mIn = connection.openInputStream();
        mOut = connection.openOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        return mIn;
    }

    public OutputStream getOutputStream() throws IOException {
        return mOut;
    }

    public SendData newSendData(ReceiveData latestReceiveData) {
        return mSwapper.swap(mConnection.toString(), latestReceiveData);
    }

    public boolean doContinue() {
        return mSwapper.doContinue();
    }

    @Override
    public String toString() {
        return mConnection.toString();
    }

    @Override
    public void close() throws IOException {
        log.debug("visitor close");
        mIn.close();
        mOut.close();
        mConnection.close();
    }
}
