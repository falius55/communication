package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 接続や送受信などのソケット通信における処理を担当するクラスが実装するインターフェース
 * @author "ymiyauchi"
 *
 */
interface SocketHandler {

    void handle(SelectionKey key) throws IOException;
}
