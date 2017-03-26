package jp.gr.java_conf.falius.communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
