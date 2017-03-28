package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.remote.Disconnectable;
import jp.gr.java_conf.falius.communication.remote.Remote;
import jp.gr.java_conf.falius.communication.sender.Sender;

/**
 * 書き込み操作を行うハンドラ
 * @author "ymiyauchi"
 *
 */
public class WritingHandler implements Handler {
    private static final Logger log = LoggerFactory.getLogger(WritingHandler.class);

    private final Disconnectable mDisconnectable;
    private final Remote mRemote;
    private final boolean mIsClient;
    private Sender mSender = null;

    public WritingHandler(Disconnectable disconnectable, Remote remote, boolean isClient) {
        mDisconnectable = disconnectable;
        mRemote = remote;
        mIsClient = isClient;
        mSender = remote.sender();
    }

    @Override
    public void handle(SelectionKey key) {
        log.debug("writing handle");
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (!channel.isOpen()) {
                // チャンネルが閉じられている場合、書き込みを中止して正常終了させる
                log.warn("channel is closed. cancel writting.");
                mDisconnectable.disconnect(channel, key,
                        new IllegalStateException("channel is not open@writing handler"));
                return;
            }

            Sender sender = mSender;

            if (sender == null) {
                log.info("disconnect by send data returned null");
                mDisconnectable.disconnect(channel, key, null);
                return;
            }

            Sender.Result result = sender.send(channel);

            if (result == Sender.Result.UNFINISHED) {
                log.debug("!sender.isWrittenFinished()");
                return;
            }

            if (mIsClient || mRemote.doContinue()) {
                key.interestOps(SelectionKey.OP_READ);
                key.attach(new ReadingHandler(mDisconnectable, mRemote, mIsClient));
            } else {
                mDisconnectable.disconnect(channel, key, null);
            }

        } catch (Exception e) {
            mDisconnectable.disconnect(channel, key, new IOException("writing handler exception", e));
            log.error("writing handler error", e);
        }

    }
}
