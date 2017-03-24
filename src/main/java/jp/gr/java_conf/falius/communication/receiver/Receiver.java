package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author "ymiyauchi"
 *
 * 同じ接続では同じReceiverオブジェクトが利用されます。
 * そのため、同じ接続で何度も送受信を繰り返す場合、新たに受信したデータは以前に受信して消費しなかったデータに
 * 追記されることになります。
 *
 *<p>
 * このオブジェクトはまずOnReceiveListener#onReceiveメソッドの引数に渡され、その後Swapper#swapメソッド
 * の引数に渡されます。クライアントに限り、最後にClient#startメソッドの戻り値として取得できます。
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

    Result receive(SocketChannel channel) throws IOException;

    /**
     * 保持しているデータの個数を返します。
     * getXXXメソッドを呼ぶ度に保持しているデータの個数は減少します。
     * @return
     */
    int dataCount();

    ByteBuffer get();

    ByteBuffer[] getAll();

    String getString();

    int getInt();

    boolean getBoolean();

    void getAndOutput(OutputStream os) throws IOException;

    void clear();
}
