package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.core.Server;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.listener.OnSendListener;
import jp.gr.java_conf.falius.communication.swapper.SwapperFactory;

/**
 *  接続要求を受入れ、Remoteオブジェクトを作成して続く処理を別のハンドラに委譲します。
 *  Serverが利用します。
 *
 *  リスナーはいつでも変更することが可能ですが、新しいリスナーは変更後に開始された接続から有効になります。
 * @author "ymiyauchi"
 * @since 1.4.3
 *
 */
class AcceptHandler implements SocketHandler {
    private static final Logger log = LoggerFactory.getLogger(AcceptHandler.class);
    private final SwapperFactory mSwapperFactory;
    private final Disconnectable mDisconnectable;

    private Server.OnAcceptListener mOnAcceptListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    /**
     *
     * @param disconnectable
     * @param swapperFactory
     * @since 1.4.3
     */
    AcceptHandler(Disconnectable disconnectable, SwapperFactory swapperFactory) {
        mDisconnectable = disconnectable;
        mSwapperFactory = swapperFactory;
    }

    /**
     * @since 1.4.3
     */
    @Override
    public void handle(SelectionKey key) throws IOException {
        accept(key);
    }

    /**
     * @since 1.4.3
     */
    public void accept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = null;
        String remoteAddress = "";
        try {
            log.debug("try accepting ...");
            clientChannel = ((ServerSocketChannel) key.channel()).accept();
            remoteAddress = clientChannel.socket().getRemoteSocketAddress().toString();
            log.debug("success accept: {}", remoteAddress);

            Remote remote = new Remote(remoteAddress, mSwapperFactory);
            remote.addOnAcceptListener(mOnAcceptListener);
            remote.addOnSendListener(mOnSendListener);
            remote.addOnReceiveListener(mOnReceiveListener);
            remote.onAccept();

            clientChannel.configureBlocking(false);
            clientChannel.register(key.selector(), SelectionKey.OP_READ,
                    new ReadingHandler(mDisconnectable, remote, false)); // 新しいチャンネルなのでregister
        } catch (Exception e) {
            if (clientChannel != null) {
                mDisconnectable.disconnect(clientChannel, key,
                        new IOException("remote starter failed accept:" + remoteAddress, e));
            }
            log.warn("accept error to disconnect", e);
        }
    }

    /**
     * @since 1.4.3
     */
    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    /**
     * @since 1.4.3
     */
    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    /**
     * @since 1.4.3
     */
    public void addOnAcceptListener(Server.OnAcceptListener listener) {
        mOnAcceptListener = listener;
    }

}
