package jp.gr.java_conf.falius.communication.bluetooth;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.handler.BluetoothReadingHandler;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * セッション。
 * - 並列にセッションを晴れるかは試していない。
 * - 基本的に Socket と同じ。
 */
public class Session implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Session.class);

    private final StreamConnection mChannel;
    private final Swapper mSwapper;

    public Session(StreamConnection channel, Swapper swapper) throws IOException {
        mChannel = channel;
        mSwapper = swapper;
    }

    /**
     * 英小文字の受信データを英大文字にしてエコーバックする。
     * - 入力が空なら終了。
     */
    public void run() {
        try (BluetoothVisitor visitor = new BluetoothVisitor(mChannel, mSwapper)) {
            BluetoothReadingHandler handler = new BluetoothReadingHandler(visitor);
            handler.receive();
        } catch (IOException e) {
            log.error("I/O error :\n{}", e.getMessage());
        }
    }
}
