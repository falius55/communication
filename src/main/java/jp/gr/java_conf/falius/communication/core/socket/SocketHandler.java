package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 接続や送受信などのソケット通信における処理を担当するクラスが実装するインターフェース
 * @author "ymiyauchi"
 * @since 1.4.3
 *
 */
interface SocketHandler {

    /**
     *
     * @param key
     * @throws IOException
     * @since 1.4.3
     */
    void handle(SelectionKey key) throws IOException;
}
