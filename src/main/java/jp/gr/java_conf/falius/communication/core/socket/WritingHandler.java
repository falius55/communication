package jp.gr.java_conf.falius.communication.core.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 書き込み操作を行うハンドラ
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
class WritingHandler implements SocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WritingHandler.class);

    private final Disconnectable mDisconnectable;
    private final Remote mRemote;
    private final boolean mIsClient;
    private Sender mSender = null;

    WritingHandler(Disconnectable disconnectable, Remote remote, boolean isClient) {
        mDisconnectable = disconnectable;
        mRemote = remote;
        mIsClient = isClient;
    }

    /**
     * {@inheritDoc}
     * @since 1.0
     */
    @Override
    public void handle(SelectionKey key) throws IOException {
        log.debug(" {} writing handle", mIsClient ? "client" : "server");
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (!channel.isOpen()) {
                // チャンネルが閉じられている場合、書き込みを中止して正常終了させる
                log.warn("channel is closed. cancel writting.");
                mDisconnectable.disconnect(channel, key,
                        new IllegalStateException("channel is not open@writing handler"));
                return;
            }

            Sender sender;
            if (mSender == null) {
                sender = mSender = mRemote.sender();
            } else {
                sender = mSender;
            }

            if (sender == null) {
                log.debug("disconnect by send data returned null");
                mDisconnectable.disconnect(channel, key, null);
                return;
            }

            Sender.Result result = sender.send(channel);

            if (result == Sender.Result.UNFINISHED) {
                return;
            }

            if (mIsClient || mRemote.doContinue()) {
                key.interestOps(SelectionKey.OP_READ);
                key.attach(new ReadingHandler(mDisconnectable, mRemote, mIsClient));
            } else {
                mDisconnectable.disconnect(channel, key, null);
            }

        } catch (Throwable e) {
            mDisconnectable.disconnect(channel, key, e);
            log.warn("writing handler error", e);
        }

    }
}
