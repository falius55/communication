package jp.gr.java_conf.falius.communication.sender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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

    /**
     *
     * Client及びServerにて内部的に使用するメソッドです。
     * このメソッドでリスナーを登録しても無効となりますので注意してください。
     * 送信時のリスナーを登録するにはClient及びServerのaddOnSendListenerメソッドを利用してください。
     * @param listener
     * @return
     */
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

    /**
     * バイト列を書き込みます。
     *
     * @param buf
     * @return
     */
    Sender put(ByteBuffer buf);

    /**
     * 複数データをバイト列の配列として渡して書き込みます。
     * 配列要素ひとつを一つのデータとしてみなします。
     * @param bufs
     * @return
     */
    Sender put(ByteBuffer[] bufs);

    /**
     * バイトの配列を書き込みます。このメソッドに一度に渡されたバイトすべてでひとつの
     * データとしてみなします。
     * @param bytes
     * @return
     */
    Sender put(byte[] bytes);

    /**
     * int値のデータを書き込みます。
     * @param num
     * @return
     */
    Sender put(int num);

    /**
     * boolean値のデータを書き込みます。
     * @param bl
     * @return
     */
    Sender put(boolean bl);

    /**
     * データを文字列として書き込みます。
     * @param msg
     * @return
     */
    Sender put(String msg);

    /**
     * InputStreamから読み出したすべてのバイト列をひとつの
     * データとして書き込みます。
     * @param in
     * @return
     * @throws IOException
     */
    Sender put(InputStream in) throws IOException;
}
