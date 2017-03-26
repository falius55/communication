package jp.gr.java_conf.falius.communication.sender;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 *
 * <p>
 * 送信データを管理するクラスです。
 *
 * <p>
 * 接続の間ずっと共有されるReceiverオブジェクトとは異なり、こちらは一度の送信ごとに使い捨てとなります。
 * そのため、再利用はできません。
 * Swapper#swapメソッドでは必ず新しく作成したインスタンスを返すようにしてください。
 * @author "ymiyauchi"
 *
 */
public interface Sender {
    enum Result {
        FINISHED, UNFINISHED,
    }

    Sender addOnSendListener(OnSendListener listener);

    /**
     * Client及びServerの内部で使用します。
     * 実際の送信を行うメソッドです。
     *
     * @param channel 送信するチャネル
     * @return 送信予定のデータをすべて送信し終えた場合はFINISHED, まだ未送信のデータが残っていればUNFINISHED
     * @throws IOException
     */
    Result send(SocketChannel channel) throws IOException;
}
