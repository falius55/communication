package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 *
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
interface Disconnectable {

    /**
     *
     * @param channel
     * @param key
     * @param cause 切断の原因。正常な終了のため呼び出した場合はnullが渡される
     * @since 1.0
     */
    void disconnect(SocketChannel channel, SelectionKey key, Throwable cause) throws IOException;
}
