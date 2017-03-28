package jp.gr.java_conf.falius.communication.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public class BluetoothVisitor implements AutoCloseable {
    private final StreamConnection mConnection;
    private final InputStream mIn;
    private final OutputStream mOut;
    private final Swapper mSwapper;

    public BluetoothVisitor(StreamConnection connection, Swapper swapper) throws IOException {
        mConnection = connection;
        mIn = connection.openInputStream();
        mOut = connection.openOutputStream();
        mSwapper = swapper;
    }

    public InputStream getInputStream() {
        return mIn;
    }

    public OutputStream getOutputStream() {
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
        mIn.close();
        mOut.close();
        mConnection.close();
    }
}
