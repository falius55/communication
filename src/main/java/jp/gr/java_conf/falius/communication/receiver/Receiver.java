package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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

    /**
     * 保持しているデータの個数を返します。
     * getXXXメソッドを呼ぶ度に保持しているデータの個数は減少します。
     * @return
     */
    int dataCount();

    /**
     * データ１単位をバイト列で取得します。
     * @return 次の保持データのバイト列。保持データがなければnull
     */
    ByteBuffer get();

    /**
     * 保持しているデータを一括して受け取ります。
     * @return
     */
    ByteBuffer[] getAll();

    /**
     * データ１単位を文字列で取得します。
     * @return
     */
    String getString();

    /**
     * データ１単位をint値で取得します。
     * @return データ１単位の最初の４バイトを整数とした値。保持しているデータがなければ0
     */
    int getInt();

    boolean getBoolean();

    /**
     * データ１単位をOutputStreamに書き込みます。
     * @param os
     * @throws IOException
     */
    void getAndOutput(OutputStream os) throws IOException;

    /**
     * 保持しているデータをすべて削除し、このオブジェクトの保持データを空にします。
     */
    void clear();
}
