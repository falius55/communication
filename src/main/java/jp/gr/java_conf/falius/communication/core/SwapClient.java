package jp.gr.java_conf.falius.communication.core;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

/**
 * 受信データを取得し送信データを送信するサイクルを最短で行うクライアントのインターフェースです。
 * @author "ymiyauchi"
 *
 */
public interface SwapClient extends Client {

    /**
     * 送信して、受信データを取得します。送信から受信までの間、処理は戻ってきません。
     * そのため内部の実装にかかわらず、同期的なメソッドとなっています。
     * @param sendData
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    ReceiveData send(SendData sendData) throws IOException, TimeoutException;

    /**
     * 受信データの受け取りと送信データの作成を定義したswapperを渡して送受信を繰り返します。
     * @param swapper
     * @return 最終受信データ。受信エラーなど、何らかの理由で受信が完了する前に接続が切れた場合はnull
     * @throws IOException
     * @throws TimeoutException
     */
    ReceiveData start(Swapper swapper) throws IOException, TimeoutException;

    /**
     * 同一スレッドで実行されている場合は処理の終了タイミングはSwapperにより判断され、自動で終了処理が行われます。
     * 内部にスレッiドプールを作成する場合({@link Client#startOnNewThread}および{@link Client#call}を実行する場合)
     *     にのみ明示的に実行する必要があります。
     */
    @Override
    void close() throws IOException;
}
