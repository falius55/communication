package jp.gr.java_conf.falius.communication.core.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.Server.OnAcceptListener;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

/**
 * 接続先に関する情報を管理するクラスです。
 * 一度の接続を通して共有されます。
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
class Remote {
    private static final Logger log = LoggerFactory.getLogger(Remote.class);
    private final String mRemoteAddress;
    private final Swapper mSwapper;
    private final Receiver mReceiver = new Receiver();

    private OnAcceptListener mOnAcceptListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    Remote(String remoteAddress, SwapperFactory swapperFactory) {
        mRemoteAddress = remoteAddress;
        mSwapper = swapperFactory.get();
    }

    /**
     *
     * @param listener
     * @since 1.0
     */
    public void addOnAcceptListener(OnAcceptListener listener) {
        mOnAcceptListener = listener;
    }

    /**
     *
     * @param listener
     * @since 1.0
     */
    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    /**
     *
     * @param listener
     * @since 1.0
     */
    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    /**
     *
     * @return
     * @since 1.4.0
     */
    public String getAddress() {
        return mRemoteAddress;
    }

    /**
     *
     * @return
     * @since 1.0
     */
    public Receiver receiver() {
        mReceiver.addOnReceiveListener(mOnReceiveListener);
        return mReceiver;
    }

    /**
     * Swapper#swapメソッドを実行し、得られたデータを保持した新しいSenderオブジェクトを返します。
     * @return
     * @throws Exception
     * @since 1.0
     */
    public Sender sender() throws Exception {
        SendData sendData;
        try {
            sendData = mSwapper.swap(mRemoteAddress, mReceiver.getData());
        } catch (Exception e) {
            throw new Exception("thrown exception from swap method", e);
        }
        if (sendData == null) {
            return null;
        }
        Sender sender = new Sender(sendData, mOnSendListener);
        return sender;
    }

    /**
     *
     * @return
     * @since 1.0
     */
    public boolean doContinue() {
        return mSwapper.doContinue();
    }

    /**
     * @since 1.0
     */
    public void onAccept() {
        if (mOnAcceptListener != null) {
            mOnAcceptListener.onAccept(mRemoteAddress);
        }
    }
}
