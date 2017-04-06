package jp.gr.java_conf.falius.communication.listener;

/**
 * 送信直後に実行されるリスナーです。
 * @author "ymiyauchi"
 *
 */
public interface OnSendListener {

    void onSend(String remoteAddress);
}