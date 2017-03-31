package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

/**
 * チャネルからの受信を行うクラス。
 *
 * @author "ymiyauchi"
 */
public interface Receiver {
    enum Result {
        ERROR, UNFINISHED, FINISHED, DISCONNECT,
    }

    void addOnReceiveListener(OnReceiveListener listener);

    /**
     * チャネルからデータを読み取ります。
     *
     * @param channel 受信用ソケット・チャネル
     * @return すべての受信データを読み取ればFINISHED, まだ受信していないデータが残った状態で終わったなら
     * UNFINISHED, 通信が途切れるなどして受信できなかった場合にはERROR
     * @throws IOException チャネルからの読み取りで例外が発生した場合
     */
    Result receive(SocketChannel channel) throws IOException;

    /**
     * @return 最新の受信データ。まだ一度も受信していなければnull
     */
    ReceiveData getData();
}
