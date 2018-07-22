package jp.gr.java_conf.falius.communication.listener;

/**
 * 接続が切断したときに呼ばれるコールバックです。
 * 例外が発生したことによる切断の場合は引数のcauseにその原因が渡されますが、正常終了の場合はnullが渡されます。
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public interface OnDisconnectCallback {

    /**
     * サーバーが特定のクライアントとの接続を切断したときに呼ばれます。
     * @param remoteAddress 切断した相手クライアントを表すリモートアドレス
     * @param cause 異常が発生したことによる切断の場合はその原因を表すオブジェクト。正常に通信が終了した場合はnull
     * @since 1.4.0
     */
    void onDissconnect(String remoteAddress, Throwable cause);
}
