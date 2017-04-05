package jp.gr.java_conf.falius.communication.core.socket;

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
 *
 */
class Remote {
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

    public void addOnAcceptListener(OnAcceptListener listener) {
        mOnAcceptListener = listener;
    }

    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    public String getAddress() {
        return mRemoteAddress;
    }

    public Receiver receiver() {
        mReceiver.addOnReceiveListener(mOnReceiveListener);
        return mReceiver;
    }

    /**
     * Swapper#swapメソッドを実行し、得られたデータを保持した新しいSenderオブジェクトを返します。
     * @return
     * @throws Exception
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

    public boolean doContinue() {
        return mSwapper.doContinue();
    }

    public void onAccept() {
        if (mOnAcceptListener != null) {
            mOnAcceptListener.onAccept(mRemoteAddress);
        }
    }
}
