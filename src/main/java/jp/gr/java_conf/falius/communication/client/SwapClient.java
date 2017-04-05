package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public interface SwapClient extends Client {

    ReceiveData send(SendData sendData) throws IOException, TimeoutException;

    /**
     *
     * @param swapper
     * @return 受信エラーなど、何らかの理由で受信が完了する前に接続が切れた場合はnull
     * @throws IOException
     * @throws TimeoutException
     */
    ReceiveData start(Swapper swapper) throws IOException, TimeoutException;
}
