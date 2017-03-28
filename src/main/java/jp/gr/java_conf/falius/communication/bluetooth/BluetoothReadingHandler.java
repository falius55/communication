package jp.gr.java_conf.falius.communication.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.gr.java_conf.falius.communication.swapper.Swapper;

public class BluetoothReadingHandler {
    private final Swapper mSwapper;

    public BluetoothReadingHandler(Swapper swapper) {
        mSwapper = swapper;
    }

    public void handle(InputStream is, OutputStream os) throws IOException {
    }
}
