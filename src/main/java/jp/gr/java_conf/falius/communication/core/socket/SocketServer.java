package jp.gr.java_conf.falius.communication.core.socket;

import jp.gr.java_conf.falius.communication.core.Server;

public interface SocketServer extends Server {

    String getLocalHostAddress();

    int getPort();
}
