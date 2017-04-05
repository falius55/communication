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
 *
 */
public interface Client extends Callable<ReceiveData>, AutoCloseable {

    /**
     * 内部のスレッドプールに自身のタスクを追加します。
     * @return
     */
    Future<ReceiveData> startOnNewThread();

    /**
     * 一度の送信で書き込みが完了した直後に実行されるリスナーを登録します。
     * @param listener
     */
    void addOnSendListener(OnSendListener listener);

    /**
     * 一度の受信で読み込みが完了した直後に実行されるリスナーを登録します。
     * @param listener
     */
    void addOnReceiveListener(OnReceiveListener listener);

    /**
     * サーバーとの接続を切断した直後に実行されるリスナーを登録します。
     * @param callback
     */
    void addOnDisconnectCallback(OnDisconnectCallback callback);

    void addOnConnectListener(OnConnectListener listener);

    void close() throws IOException;

    public interface OnConnectListener  {

        void onConnect(String remoteAddress);
    }
}
