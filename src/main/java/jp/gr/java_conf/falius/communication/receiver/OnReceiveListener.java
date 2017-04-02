package jp.gr.java_conf.falius.communication.receiver;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

public interface OnReceiveListener {

    /**
     *
     * @param remoteAddress 送信してきたリモートのアドレス
     * @param receiveData 受信データ
     */
    void onReceive(String remoteAddress, ReceiveData receiveData);
}
