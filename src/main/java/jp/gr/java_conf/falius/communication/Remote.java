package jp.gr.java_conf.falius.communication;

import jp.gr.java_conf.falius.communication.receiver.MultiDataReceiver;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.SendData;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.server.Server.OnAcceptListener;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * 接続先に関する情報を管理するクラスです。
 * 一度の接続を通して共有されます。
 * @author "ymiyauchi"
 *
 */
public class Remote {
    private final String mRemoteAddress;
    private final Swapper mSwapper;
    private final Receiver mReceiver = new MultiDataReceiver();

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
        mReceiver.addOnReceiveListener(mOnReceiveListener);
        return mReceiver;
    }

    public Sender sender() {
        SendData sendData = mSwapper.swap(mRemoteAddress, mReceiver.getData());
        if (sendData == null) {
            return null;
        }
        Sender sender = new MultiDataSender(sendData);
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
