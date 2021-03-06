package jp.gr.java_conf.falius.communication.header;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public interface Header {

    /**
     * ヘッダ自体のサイズを返します。
     * @return
     * @since 1.4.0
     */
    int size();

    /**
     * ヘッダ自体も含む、すべてのデータのサイズを返します。
     * @return
     * @since 1.4.0
     */
    int allDataSize();

    /**
     * 各データのサイズが格納された整数バッファを返します。
     * ヘッダ自体のサイズは含まれません。
     * ヘッダの読み取りが最後まで終わっていない場合は常に空のバッファが返されます。
     * @return
     * @since 1.4.0
     */
    IntBuffer dataSizeBuffer();

    /**
     *
     * @return
     * @throws IllegalStateException ヘッダの読み取りが完全に終わっていない場合
     * @since 1.4.0
     */
    ByteBuffer toByteBuffer();

    /**
     * まだ読み込まれていないヘッダ情報をチャネルから読み取ります。
     * すでにヘッダ情報がすべて読み込まれていた場合には何もしません。
     * このヘッダが完全に読み取りが終わっている場合、あるいはこのメソッドの実行結果によっても
     *     まだ読み取りが完全に終わっていない場合はこのオブジェクトが、そうでなければ完全に読み取りの終わった
     *     新しいヘッダオブジェクトが返されます。
     *     そのためこのヘッダとは異なるヘッダが新しく返される可能性があるため、戻り値を破棄しないように注意してください。
     * @param channel
     * @return 読み取りが終わったヘッダ、あるいは読み取りが終わっていないヘッダ
     * @throws IOException
     * @since 1.4.0
     */
    Header read(SocketChannel channel) throws IOException;

    /**
     * このヘッダが最後まで読み取りが終わったかどうかの真偽値を返します。
     * @return
     * @since 1.4.0
     */
    boolean isReadFinished();
}
