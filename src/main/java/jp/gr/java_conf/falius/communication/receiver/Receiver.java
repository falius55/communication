package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author "ymiyauchi"
 *
 * <p/>
 * 受信データを管理するクラス
 *
 * <p>
 * Senderによってputされたのと同じ順番でデータが格納されますので、getXXXメソッドにて取得してください。
 * 取得されたデータはこのクラスの内部から削除されます。
 *
 *<p>
 * このオブジェクトはまずOnReceiveListener#onReceiveメソッドの引数に渡され、その後Swapper#swapメソッド
 * の引数に渡されます。クライアントに限り、最後にClient#startメソッドの戻り値として取得できます。
 *
 *<p>
 * 同じ接続では同じReceiverオブジェクトが利用されます。
 * そのため、同じ接続で何度も送受信を繰り返す場合、新たに受信したデータは以前に受信して消費しなかったデータに
 * 追記されることになります。送信側のSenderでのpush回数と受信側のReceiverオブジェクトのget回数は一度の
 * 送受信ごとに同数となるよう呼び出すことをおすすめします。
 *
 * <p>
 * getXXXメソッドの実行によって例外が発生した場合、取得しようとしたデータは失われます。
 *
 */
public interface Receiver {
    enum Result {
        ERROR, UNFINISHED, FINISHED,
    }

    /**
     * 内部的に使用するメソッドです。
     * 受信時のリスナーを登録する場合はClient及びServerのaddOnReceiveLisnerメソッドを使用してください。
     * @param listener
     */
    void addOnReceiveListener(OnReceiveListener listener);

    /**
     * 内部的に使用するメソッドです。
     * チャネルからデータを読み取ります。
     *
     * @param channel 受信用ソケット・チャネル
     * @return すべての受信データを読み取ればFINISHED, まだ受信していないデータが残った状態で終わったなら
     * UNFINISHED, 通信が途切れるなどして受信できなかった場合にはERROR
     * @throws IOException チャネルからの読み取りで例外が発生した場合
     */
    Result receive(SocketChannel channel) throws IOException;

    ReceiveData getData();
}
