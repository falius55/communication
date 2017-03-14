package communication.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import communication.Disconnectable;
import communication.Remote;
import communication.Swapper;
import communication.handler.Handler;
import communication.handler.ReadingHandler;
import communication.receiver.OnReceiveListener;
import communication.sender.OnSendListener;

/**
 *  リスナーはいつでも変更することが可能ですが、新しいリスナーは変更後に開始された接続から有効になります。
 * @author "ymiyauchi"
 *
 */
public class RemoteStarter implements Handler {
    private final Swapper.SwapperFactory mSwapperFactory;
    private final Disconnectable mDisconnectable;

    private Server.OnAcceptListener mOnAcceptListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    public RemoteStarter(Disconnectable disconnectable, Swapper.SwapperFactory swapperFactory) {
        mDisconnectable = disconnectable;
        mSwapperFactory = swapperFactory;
    }

    @Override
    public void handle(SelectionKey key) {
        accept(key);
    }

    public void accept(SelectionKey key) {
        SocketChannel clientChannel = null;
        String remoteAddress = "";
        try {
            clientChannel = ((ServerSocketChannel) key.channel()).accept();
            remoteAddress = clientChannel.socket().getRemoteSocketAddress().toString();

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
            e.printStackTrace();
        }
    }

    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    public void addOnAcceptListener(Server.OnAcceptListener listener) {
        mOnAcceptListener = listener;
    }

}
