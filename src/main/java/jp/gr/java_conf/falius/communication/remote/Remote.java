package jp.gr.java_conf.falius.communication.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.receiver.MultiDataReceiver;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.server.Server.OnAcceptListener;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

/**
 * 接続先に関する情報を管理するクラスです。
 * 一度の接続を通して共有されます。
 * @author "ymiyauchi"
 *
 */
public class Remote {
    private static final Logger log = LoggerFactory.getLogger(Remote.class);
    private final String mRemoteAddress;
    private final Swapper mSwapper;
    private final Receiver mReceiver = new MultiDataReceiver();

    private OnAcceptListener mOnAcceptListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    public Remote(String remoteAddress, SwapperFactory swapperFactory) {
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
        log.debug("remote add on receive listener: {}", listener);
        mOnReceiveListener = listener;
    }

    public String getAddress() {
        return mRemoteAddress;
    }

    public Receiver receiver() {
        log.debug("add on receive listener to receiver : {}", mOnReceiveListener);
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
        Sender sender = new MultiDataSender(sendData, mOnSendListener);
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
