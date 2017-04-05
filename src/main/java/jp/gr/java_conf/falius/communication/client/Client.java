package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.remote.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * 通信におけるクライアントを表すインターフェースです。
 * 詳細は各具象クラスをご覧ください。
 * @author "ymiyauchi"
 *
 */
public interface Client extends Callable<ReceiveData>, AutoCloseable {

    /**
     * データを送信します。具体的な処理は実装により異なります。
     * @param sendData
     * @return 同期通信であれば受信データ。非同期通信であればnull
     * @throws IOException
     * @throws TimeoutException
     */
    ReceiveData send(SendData sendData) throws IOException, TimeoutException;

    /**
     *
     * @param swapper
     * @return 受信エラーなど、何らかの理由で受信が完了する前に接続が切れた場合はnull
     * @throws IOException
     * @throws TimeoutException
     */
    ReceiveData start(Swapper swapper) throws IOException, TimeoutException;

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
