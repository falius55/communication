package jp.gr.java_conf.falius.communication.core;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import jp.gr.java_conf.falius.communication.listener.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

/**
 * 通信におけるクライアントを表すインターフェースです。
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
public interface Client extends Callable<ReceiveData>, AutoCloseable {

    /**
     * 内部のスレッドプールに自身のタスクを追加します。
     * @return
     * @since 1.4.1
     */
    Future<ReceiveData> startOnNewThread();

    /**
     * 一度の送信で書き込みが完了した直後に実行されるリスナーを登録します。
     * @param listener
     * @since 1.0
     */
    void addOnSendListener(OnSendListener listener);

    /**
     * 一度の受信で読み込みが完了した直後に実行されるリスナーを登録します。
     * @param listener
     * @since 1.0
     */
    void addOnReceiveListener(OnReceiveListener listener);

    /**
     * サーバーとの接続を切断した直後に実行されるリスナーを登録します。
     * @param callback
     * @since 1.4.0
     */
    void addOnDisconnectCallback(OnDisconnectCallback callback);

    /**
     *
     * @param listener
     * @since 1.4.2
     */
    void addOnConnectListener(OnConnectListener listener);

    /**
     * @since 1.4.1
     */
    @Override
    void close() throws IOException;

    /**
     *
     * @author "ymiyauchi"
     * @since 1.4.2
     *
     */
    public interface OnConnectListener  {

        /**
         *
         * @param remoteAddress
         * @since 1.4.2
         */
        void onConnect(String remoteAddress);
    }
}
