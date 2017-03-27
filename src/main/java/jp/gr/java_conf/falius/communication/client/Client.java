package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.Disconnectable;
import jp.gr.java_conf.falius.communication.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.ReceiveData;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.SendData;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public interface Client extends Callable<ReceiveData>, Disconnectable {

    /**
     * 送受信を一度だけ行う場合の、start(Swapper)の簡易メソッドです。
     * @param sendData
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    ReceiveData start(SendData sendData) throws IOException, TimeoutException;

    /**
     *
     * @param swapper
     * @return 受信エラーなど、何らかの理由で受信が完了する前に接続が切れた場合はnull
     * @throws IOException
     * @throws TimeoutException
     */
    ReceiveData start(Swapper swapper) throws IOException, TimeoutException;

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

}
