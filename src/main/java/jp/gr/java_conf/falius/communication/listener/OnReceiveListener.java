package jp.gr.java_conf.falius.communication.listener;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

/**
 *
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
public interface OnReceiveListener {

    /**
     *
     * @param remoteAddress 送信してきたリモートのアドレス
     * @param receiveData 受信データ
     * @since 1.0
     */
    void onReceive(String remoteAddress, ReceiveData receiveData);
}
