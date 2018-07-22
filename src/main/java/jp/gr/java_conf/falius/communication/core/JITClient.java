package jp.gr.java_conf.falius.communication.core;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.senddata.SendData;

/**
 * 接続を維持し、任意のタイミングで送信データを供給する形で利用するクライアントのインターフェースです。
 * @author "ymiyauchi"
 * @since 1.4.3
 *
 */
public interface JITClient extends Client {

    /**
     * 送信データを供給します。実行して即座に送信されることまでは保証しません。
     * このメソッドはスレッドセーフです。
     * @param sendData
     * @throws IOException
     * @throws TimeoutException
     * @throws NullPointerException sendDataがnullの場合
     * @since 1.4.3
     */
    void send(SendData sendData) throws IOException, TimeoutException;
}
