package jp.gr.java_conf.falius.communication.server;

import jp.gr.java_conf.falius.communication.remote.Disconnectable;

public interface SocketServer extends Server, Disconnectable {

    String getLocalHostAddress();

    int getPort();
}
