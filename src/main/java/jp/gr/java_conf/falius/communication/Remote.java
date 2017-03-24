package jp.gr.java_conf.falius.communication;

import jp.gr.java_conf.falius.communication.receiver.MultiDataReceiver;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.server.Server.OnAcceptListener;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

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
