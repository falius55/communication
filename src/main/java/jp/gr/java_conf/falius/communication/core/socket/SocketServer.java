package jp.gr.java_conf.falius.communication.core.socket;

import jp.gr.java_conf.falius.communication.core.Server;

public interface SocketServer extends Server {

    /**
     * localhostのIPアドレスを取得します。取得に失敗するとnull
     * @return
     */
    String getLocalHostAddress();

    int getPort();
}
