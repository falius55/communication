package jp.gr.java_conf.falius.communication.server;

import jp.gr.java_conf.falius.communication.remote.OnDisconnectCallback;

public interface SocketServer extends Server {

    /**
     * <p>
     * 特定のクライアントとの接続が切断されたときに実行されるコールバックを登録します。
     * このコールバックは正常に通信が終了したとき、異常が発生したことによる切断ともに実行されます。
     * 正常な切断の場合にはcallbackの引数であるcauseにはnullが渡されます。
     *
     *<p>
     * 特定のクライアントとの読み込み操作および書き込み操作で発生した例外はすべて捕捉されて
     * Disconnectable#disconnectメソッドが呼ばれます。つまり例外が発生したのみではサーバーは
     * 動き続けますので、例外発生とともにサーバーが落ちるようにするにはこのOnDisconnectCallbackの
     * 中でサーバーをclose()する必要があります。
     * @param callback
     */
    void addOnDisconnectCallback(OnDisconnectCallback callback);

    String getLocalHostAddress();

    int getPort();
}
