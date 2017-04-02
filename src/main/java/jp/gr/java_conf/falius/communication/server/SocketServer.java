package jp.gr.java_conf.falius.communication.server;

public interface SocketServer extends Server {

    String getLocalHostAddress();

    int getPort();
}
