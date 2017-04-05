package jp.gr.java_conf.falius.communication.core;

public interface SocketServer extends Server {

    String getLocalHostAddress();

    int getPort();
}
