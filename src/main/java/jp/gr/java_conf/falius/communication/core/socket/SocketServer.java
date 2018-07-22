package jp.gr.java_conf.falius.communication.core.socket;

import jp.gr.java_conf.falius.communication.core.Server;

/**
 *
 * @author "ymiyauchi"
 * @since 1.4.1
 *
 */
public interface SocketServer extends Server {

    /**
     * localhostのIPアドレスを取得します。取得に失敗するとnull
     * @return
     * @since 1.4.1
     */
    String getLocalHostAddress();

    /**
     *
     * @return
     * @since 1.4.1
     */
    int getPort();
}
