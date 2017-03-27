package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.WrongMethodTypeException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * 受信データを保持するクラスです。
 * SendDataによって送信された際に利用します。
 *
 * <p>
 * 受信データを作成する際はまずBasicReceiveDataクラスをインスタンス化し、基本データ型以外の形式でデータを作成したい場合は
 * ExtendableReceiveDataを拡張したクラスのコンストラクタの引数に渡すことでさまざまな形式でデータを受信することができます。
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
 * クライアントの最終受信データはSwapper#swapメソッドには渡されません。
 * OnReceiveListener#onReceiveメソッドに渡された直後に接続が切断されるため、
 * 代わりにClient#satartメソッドの戻り値として返されます。<br>
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
     * @throws WrongMethodTypeException デコードできないデータをこのメソッドで取得しようとした場合
     */
    String getString();

    /**
     * データ１単位の最初の4バイトをint値で取得します。
     * @return データ１単位の最初の4バイトを整数とした値。
     * @throws NoSuchElementException 保持しているデータがない場合
     * @throws WrongMethodTypeException データが4バイトより少ないのにこのメソッドでデータを取得しようとした場合
     */
    int getInt();

    /**
     * データ１単位の最初の8バイトをlong値で取得します。
     * @return データ１単位の最初の8バイトを整数とした値。
     * @throws NoSuchElementException 保持しているデータがない場合
     * @throws WrongMethodTypeException データが8バイトより少ないのにこのメソッドでデータを取得しようとした場合
     */
    long getLong();

    /**
     * データ１単位の最初の8バイトをdouble値で取得します。
     * @return データ１単位の最初の8バイトをdouble値とした値。
     * @throws NoSuchElementException 保持しているデータがない場合
     * @throws WrongMethodTypeException データが8バイトより少ないのにこのメソッドでデータを取得しようとした場合
     */
    double getDouble();

    /**
     * データ１単位の最初の4バイトをfloat値で取得します。
     * @return データ１単位の最初の4バイトをfloat値とした値。
     * @throws NoSuchElementException 保持しているデータがない場合
     * @throws WrongMethodTypeException データが4バイトより少ないのにこのメソッドでデータを取得しようとした場合
     */
    float getFloat();

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
