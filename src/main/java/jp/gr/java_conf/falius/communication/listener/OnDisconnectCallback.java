package jp.gr.java_conf.falius.communication.listener;

public interface OnDisconnectCallback {

    /**
     * サーバーが特定のクライアントとの接続を切断したときに呼ばれます。
     * @param remote 切断した相手クライアントを表すリモートアドレス
     * @param cause 異常が発生したことによる切断の場合はその原因を表すオブジェクト。正常に通信が終了した場合はnull
     */
    void onDissconnect(String remote, Throwable cause);
}
