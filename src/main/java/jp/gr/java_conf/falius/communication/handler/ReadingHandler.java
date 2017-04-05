package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.remote.Disconnectable;
import jp.gr.java_conf.falius.communication.remote.Remote;

/**
 * 読み込み処理を行うハンドラ
 * @author "ymiyauchi"
 *
 */
class ReadingHandler implements Handler {

    private final Disconnectable mDisconnectable;
    private final Remote mRemote;
    private final boolean mIsClient;

    public ReadingHandler(Disconnectable disconnectable, Remote remote, boolean isClient) {
        mDisconnectable = disconnectable;
        mRemote = remote;
        mIsClient = isClient;
    }

    @Override
    public void handle(SelectionKey key) throws  IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        try {

            Receiver receiver = mRemote.receiver();

            Receiver.Result result = receiver.receive(channel);

            if (result == Receiver.Result.DISCONNECT) {
                // 接続相手がcloseした
                mDisconnectable.disconnect(channel, key, null);
                return;
            }

            if (result == Receiver.Result.ERROR) {
                mDisconnectable.disconnect(channel, key, new IOException("reading channel returns -1"));
                return;
            }

            if (result == Receiver.Result.UNFINISHED) {
                return;  // 書き込み操作に移行せず、もう一度読み込みを行う
            }

            if (!mIsClient || mRemote.doContinue()) {
                key.interestOps(SelectionKey.OP_WRITE);
                key.attach(new WritingHandler(mDisconnectable, mRemote, mIsClient));
            } else {
                mDisconnectable.disconnect(channel, key, null);
            }

        } catch (Exception e) {
            mDisconnectable.disconnect(channel, key, new IOException("reading handler exception", e));
        }
    }
}
