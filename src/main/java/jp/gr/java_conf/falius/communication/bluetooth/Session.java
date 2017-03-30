package jp.gr.java_conf.falius.communication.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.handler.BluetoothHandler;
import jp.gr.java_conf.falius.communication.handler.BluetoothReadingHandler;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * セッション。
 * - 並列にセッションを晴れるかは試していない。
 * - 基本的に Socket と同じ。
 */
public class Session implements Runnable, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Session.class);

    private final StreamConnection mChannel;
    private final Swapper mSwapper;
    private final OnSendListener mOnSendListener;
    private final OnReceiveListener mOnReceiveListener;

    private final InputStream mIn;
    private final OutputStream mOut;
    private BluetoothHandler mNextHandler = null;

    private boolean mIsContinue = true;

    public Session(StreamConnection channel, Swapper swapper,
            OnSendListener onSendListener, OnReceiveListener onReceiveListener) throws IOException {
        mChannel = channel;
        mSwapper = swapper;
        mOnSendListener = onSendListener;
        mOnReceiveListener = onReceiveListener;
        mIn = channel.openInputStream();
        mOut = channel.openOutputStream();
        mNextHandler = new BluetoothReadingHandler(this);
    }

    /**
     * 英小文字の受信データを英大文字にしてエコーバックする。
     * - 入力が空なら終了。
     */
    public void run() {
        try (Session session = this) {
            while (mIsContinue) {
                BluetoothHandler handler = mNextHandler;
                handler.handle();
            }
        } catch (IOException e) {
            log.error("I/O error :\n{}", e.getMessage());
        }

        log.debug("session run end");
    }

    public void disconnect() {
        mIsContinue = false;
    }

    public void setHandler(BluetoothHandler handler) {
        mNextHandler = handler;
    }

    public void onSend(int writeBytes) {
        if (mOnSendListener != null) {
            mOnSendListener.onSend(writeBytes);
        }
    }

    public void onReceive(String fromAddress, int readByte, ReceiveData receiveData) {
        if (mOnReceiveListener != null) {
            mOnReceiveListener.onReceive(fromAddress, readByte, receiveData);
        }
    }

    public InputStream getInputStream() throws IOException {
        return mIn;
    }

    public OutputStream getOutputStream() throws IOException {
        return mOut;
    }

    public SendData newSendData(ReceiveData latestReceiveData) {
        return mSwapper.swap(mChannel.toString(), latestReceiveData);
    }

    public boolean doContinue() {
        return mSwapper.doContinue();
    }

    @Override
    public String toString() {
        return mChannel.toString();
    }

    @Override
    public void close() throws IOException {
        log.debug("visitor close");
        mIn.close();
        mOut.close();
        mChannel.close();
    }
}
