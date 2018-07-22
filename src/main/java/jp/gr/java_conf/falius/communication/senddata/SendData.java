package jp.gr.java_conf.falius.communication.senddata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * <p>
 * 送信データを管理するクラスのインターフェースです。
 *
 * <p>
 * 一度の送信ごとに使い捨てとなります。
 * そのため、再利用はできません。
 * Swapper#swapメソッドでは必ず新しく作成したインスタンスを返すようにしてください。
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public interface SendData extends Iterable<ByteBuffer> {

    /**
     * バイト列を書き込みます。
     *
     * @param buf
     * @return
     * @since 1.4.0
     */
    SendData put(ByteBuffer buf);

    /**
     * 複数データをバイト列の配列として渡して書き込みます。
     * 配列要素ひとつを一つのデータとしてみなします。
     * @param bufs
     * @return
     * @since 1.4.0
     */
    SendData put(ByteBuffer[] bufs);

    /**
     * バイトの配列を書き込みます。このメソッドに一度に渡されたバイトすべてでひとつの
     * データとしてみなします。
     * @param bytes
     * @return
     * @since 1.4.0
     */
    SendData put(byte[] bytes);

    /**
     * int値のデータを書き込みます。
     * @param num
     * @return
     * @since 1.4.0
     */
    SendData put(int num);

    /**
     * long値のデータを書き込みます。
     * @param num
     * @return
     * @since 1.4.0
     */
    SendData put(long num);

    /**
     * double値のデータを書き込みます。
     * @param num
     * @return
     * @since 1.4.0
     */
    SendData put(double num);

    /**
     * float値のデータを書き込みます。
     * @param num
     * @return
     * @since 1.4.0
     */
    SendData put(float num);

    /**
     * boolean値のデータを書き込みます。
     * @param bl
     * @return
     * @since 1.4.0
     */
    SendData put(boolean bl);

    /**
     * データを文字列として書き込みます。
     * @param msg
     * @return
     * @since 1.4.0
     */
    SendData put(String msg);

    /**
     * InputStreamから読み出したすべてのバイト列をひとつの
     * データとして書き込みます。
     * @param in
     * @return
     * @throws IOException
     * @since 1.4.0
     */
    SendData put(InputStream in) throws IOException;

    /**
     *
     * @return
     * @since 1.4.0
     */
    int size();

    /**
     * 書き込むデータがあるかどうかを返します。
     * @return
     * @since 1.4.0
     */
    boolean hasRemain();
}
