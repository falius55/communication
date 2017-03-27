package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * 受信データを保持するクラスです。
 * SendQueueによって送信された際に利用します。
 *
 * SendDataにputされたのと同じ順番でデータが格納されますので、getXXXメソッドにて取得してください。
 * 取得されたデータはこのクラスの内部から削除されます。
 * また、getXXXメソッドの実行によって例外が発生した場合、取得しようとしたデータは失われます。
 *
 *<p>
 * このオブジェクトはまずOnReceiveListener#onReceiveメソッドの引数に渡され、その後Swapper#swapメソッド
 * の引数に渡されます。クライアントに限り、最後に最終受信データがClient#startメソッドの戻り値として取得できます。<br>
 * 一度の受信に対し、OnReceiveListener#onReceiveメソッドの引数に渡されるオブジェクトとSwapper#swapメソッド
 * の引数に渡されるオブジェクトは同一になります。<br>
 * 最後に受信した際に限り、上記の二つとClient#startメソッドの戻り値は同一になります。<br>
 * このため、OnReceiverListenerの引数で渡されるReceiverオブジェクトから消費した受信データは Client#startメソッド
 * の戻り値で渡されるReceiverオブジェクトには含まれていませんので注意してください(startメソッドの戻り値も同様)。
 *
 * @author "ymiyauchi"
 *
 */
public interface ReceiveData {

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
     * 保持しているデータがなければnull。
     * UTF-8でデコードされます。
     * @return
     * @throws IllegalStateException デコードできないデータをこのメソッドで取得しようとした場合
     */
    String getString();

    /**
     * データ１単位の最初の４バイトをint値で取得します。
     * @return データ１単位の最初の４バイトを整数とした値。
     * @throws NoSuchElementException 保持しているデータがない場合
     */
    int getInt();

    /**
     * データ１単位を真偽値として取得します。
     * SendData#putBooleanに対応した値を返しますが、それ以外の方法で与えられた値に対する結果は保証しません。
     * @return
     */
    boolean getBoolean();

    /**
     * データ１単位をOutputStreamに書き込みます。
     * 保持しているデータがなければ何もしません。
     * @param os
     * @throws IOException
     */
    void getAndOutput(OutputStream os) throws IOException;

    /**
     * 保持しているデータをすべて削除し、このオブジェクトの保持データを空にします。
     */
    void clear();
}
