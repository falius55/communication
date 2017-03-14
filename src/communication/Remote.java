package communication;

import communication.receiver.MultiDataReceiver;
import communication.receiver.OnReceiveListener;
import communication.receiver.Receiver;
import communication.sender.OnSendListener;
import communication.sender.Sender;
import communication.server.Server.OnAcceptListener;

/**
 * 接続先に関する情報を管理するクラス
 * @author "ymiyauchi"
 *
 */
public class Remote {
    private final String mRemoteAddress;
    private final Swapper mSwapper;
    private Receiver mReceiver = null;

    private OnAcceptListener mOnAcceptListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    public Remote(String remoteAddress, Swapper.SwapperFactory swapperFactory) {
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
        if (mReceiver == null) {
            mReceiver = new MultiDataReceiver();
        }
        mReceiver.addOnReceiveListener(mOnReceiveListener);
        return mReceiver;
    }

    public Sender sender() {
        Sender sender = mSwapper.swap(mRemoteAddress, mReceiver);
        if (sender == null) {
            return null;
        }
        sender.addOnSendListener(mOnSendListener);
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
