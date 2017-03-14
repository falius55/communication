package jp.gr.java_conf.falius.communication.handler;

import java.nio.channels.SelectionKey;

public interface Handler {

    void handle(SelectionKey key);
}
