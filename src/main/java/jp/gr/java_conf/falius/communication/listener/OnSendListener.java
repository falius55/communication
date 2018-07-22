package jp.gr.java_conf.falius.communication.listener;

/**
 * 送信直後に実行されるリスナーです。
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
public interface OnSendListener {

    /**
     *
     * @param remoteAddress
     */
    void onSend(String remoteAddress);
}
