package jp.gr.java_conf.falius.communication.core.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 *
 * @author "ymiyauchi"
 * @since 1.4.1
 *
 */
class Session implements Runnable, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Session.class);

    private final StreamConnection mChannel;
    private final String mRemoteAddress;
    private final Swapper mSwapper;
    private final OnSendListener mOnSendListener;
    private final OnReceiveListener mOnReceiveListener;
    private final OnDisconnectCallback mOnDisconnectCallback;
    private final boolean mIsClient;

    private final InputStream mIn;
    private final OutputStream mOut;
    private BluetoothHandler mNextHandler;

    private boolean mDoContinue = true;

    private ReceiveData mLatestData = null;

    /**
     *
     * @param channel
     * @param swapper
     * @param onSendListener
     * @param onReceiveListener
     * @param onDisconnectCallback
     * @param isClient
     * @throws IOException
     * @since 1.4.1
     */
    Session(StreamConnection channel, Swapper swapper,
            OnSendListener onSendListener, OnReceiveListener onReceiveListener,
            OnDisconnectCallback onDisconnectCallback, boolean isClient) throws IOException {
        mChannel = channel;
        RemoteDevice remote = RemoteDevice.getRemoteDevice(channel);
        mRemoteAddress = remote.getBluetoothAddress();
        mSwapper = swapper;
        mOnSendListener = onSendListener;
        mOnReceiveListener = onReceiveListener;
        mOnDisconnectCallback = onDisconnectCallback;
        mIn = channel.openInputStream();
        mOut = channel.openOutputStream();
        mIsClient = isClient;
    }

    /**
     * @since 1.4.1
     */
    @Override
    public void run() {
        log.debug("session start");
        try {
            if (mIsClient) {
                mNextHandler = new BluetoothWritingHandler(this, mSwapper.swap(mRemoteAddress, null));
            } else {
                mNextHandler = new BluetoothReadingHandler(this);
            }

            while (mDoContinue) {
                BluetoothHandler handler = mNextHandler;
                handler.handle();
            }
        } catch (Throwable e) {
            disconnect(e);
            log.warn("running error, session end ", e);
            return;
        }

        disconnect(null);
        log.debug("session end");
    }

    /**
     *
     * @param cause
     * @since 1.4.1
     */
    void disconnect(Throwable cause) {
        if (!mDoContinue) {
            return;
        }
        log.debug("session disconnect by {}", cause == null ? "null" : cause.getMessage());
        mDoContinue = false;
        try {
            mIn.close();
            mOut.close();
            mChannel.close();
        } catch (IOException e) {
            log.warn("error during disconnecting", e);
        }
        if (mOnDisconnectCallback != null) {
            mOnDisconnectCallback.onDissconnect(mRemoteAddress, cause);
        }
    }

    /**
     * @since 1.4.1
     */
    void onSend() {
        if (mOnSendListener != null) {
            mOnSendListener.onSend(mRemoteAddress);
        }
    }

    /**
     *
     * @param receiveData
     * @since 1.4.1
     */
    void onReceive(ReceiveData receiveData) {
        if (mOnReceiveListener != null) {
            mOnReceiveListener.onReceive(mRemoteAddress, receiveData);
        }
    }

    /**
     *
     * @return
     * @throws IOException
     * @since 1.4.1
     */
    InputStream getInputStream() throws IOException {
        return mIn;
    }

    /**
     *
     * @return
     * @since 1.4.1
     * @throws IOException
     */
    OutputStream getOutputStream() throws IOException {
        return mOut;
    }

    /**
     *
     * @param latestReceiveData
     * @return
     * @throws Exception
     * @since 1.4.1
     */
    SendData newSendData(ReceiveData latestReceiveData) throws Exception {
        try {
            return mSwapper.swap(mRemoteAddress, latestReceiveData);
        } catch (Exception e) {
            throw new Exception("thrown exception from swap method");
        }
    }

    /**
     *
     * @return
     * @since 1.4.1
     */
    boolean doContinue() {
        return mSwapper.doContinue();
    }

    /**
     * @since 1.4.1
     */
    @Override
    public String toString() {
        return mChannel.toString();
    }

    /**
     * @since 1.4.1
     */
    @Override
    public void close() throws IOException {
        disconnect(null);
    }

    /**
     *
     * @param handler
     * @since 1.4.1
     */
    void setHandler(BluetoothHandler handler) {
        mNextHandler = handler;
    }

    void setData(ReceiveData receiveData) {
        mLatestData = receiveData;
    }

    ReceiveData getData() {
        return mLatestData;
    }

    boolean isClient() {
        return mIsClient;
    }
}
