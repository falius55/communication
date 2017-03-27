package jp.gr.java_conf.falius.communication.handler;

import java.nio.channels.SelectionKey;

/**
 * 接続や送受信などのソケット通信における処理を担当するクラスが実装するインターフェース
 * @author "ymiyauchi"
 *
 */
public interface Handler {

    void handle(SelectionKey key);
}
