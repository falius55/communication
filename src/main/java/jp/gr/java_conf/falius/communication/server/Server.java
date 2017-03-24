package jp.gr.java_conf.falius.communication.server;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import jp.gr.java_conf.falius.communication.Disconnectable;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;

/**
 *
 * <p/>
 * サーバーを表すクラスです。
 *
 * <p/>
 * インスタンスを作成した後、startOnNewThreadメソッドを実行することで起動します。
 *
 * <p/>
 * 別のスレッドで起動するため、通信中に例外が発生しても検知できないことがあります。
 * 適宜startOnNewThreadメソッドの戻り値であるFutureオブジェクトを利用するなどして
 * 対応してください。
 * <p/>
 * 特定の接続先との間で例外が発生した場合、捕捉してその接続先との通信を切断した上で
 * 続行します。
 */
public interface Server extends Disconnectable, Callable<Throwable>, AutoCloseable {

    /**
     * 受信直後に実行するリスナーを登録します。
     *
     * @param listener
     */
    void addOnReceiveListener(OnReceiveListener listener);

    /**
     * 送信直後に実行するリスナーを登録します。
     *
     * @param listener
     */
    void addOnSendListener(OnSendListener listener);

    /**
     * 新しい接続要求を受け入れた際に実行するリスナーを登録します。
     * @param callback
     */
    void addOnAcceptListener(Server.OnAcceptListener callback);

    /**
     * shutdownメソッドを呼び出した際に実行されるリスナーを登録します。
     * @param callback
     */
    void addOnShutdownCallback(Server.OnShutdownCallback callback);

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

    /**
     * 新しいスレッドでこのサーバーを起動します。
     *
     * @return
     */
    Future<?> startOnNewThread();

    /**
     * shutdownメソッドと同義です。try-with-resources文で利用することもできます。
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * このサーバーを安全に終了します。
     *
     * @throws IOException
     */
    void shutdown() throws IOException;

    /**
     * 新しい接続要求を受け入れた際に実行されるリスナー
     */
    interface OnAcceptListener {

        /**
         *
         * @param remoteAddress 受け入れたリモートアドレス
         */
        void onAccept(String remoteAddress);
    }

    /**
     * shutdownメソッドを実行した際に実行されるリスナー
     */
    interface OnShutdownCallback {
        void onShutdown();
    }
}
