package jp.gr.java_conf.falius.communication.receiver;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

public interface OnReceiveListener {

    /**
     *
     * @param fromAddress 送信してきたリモートのアドレス
     * @param readByte ヘッダを含めたすべてのデータの受信サイズ
     * @param receiveData 受信データ
     */
    void onReceive(String fromAddress, int readByte, ReceiveData receiveData);
}
