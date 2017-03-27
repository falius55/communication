package jp.gr.java_conf.falius.communication.sender;

/**
 * 送信直後に実行されるリスナーです。
 * @author "ymiyauchi"
 *
 */
public interface OnSendListener {

    /**
     *
     * @param writeBytes ヘッダも含めたすべての書き込みデータのサイズ
     */
    void onSend(int writeBytes);
}
