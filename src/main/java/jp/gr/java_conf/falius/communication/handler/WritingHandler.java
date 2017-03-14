package jp.gr.java_conf.falius.communication.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import jp.gr.java_conf.falius.communication.Disconnectable;
import jp.gr.java_conf.falius.communication.Remote;
import jp.gr.java_conf.falius.communication.sender.Sender;

public class WritingHandler implements Handler {
    private final Disconnectable mDisconnectable;
    private final Remote mRemote;
    private final boolean mIsClient;
    private Sender mSender = null;

    public WritingHandler(Disconnectable disconnectable, Remote remote, boolean isClient) {
        mDisconnectable = disconnectable;
        mRemote = remote;
        mIsClient = isClient;
    }

    @Override
    public void handle(SelectionKey key) {
        System.out.println("writing handle");
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (!channel.isOpen()) {
                // チャンネルが閉じられている場合、書き込みを中止して正常終了させる
                System.err.println("channel is closed. cancel writting.");
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
                mDisconnectable.disconnect(channel, key, null);
                return;
            }

            Sender.Result result = sender.send(channel);

            if (result == Sender.Result.UNFINISHED) {
                System.out.println("!sender.isWrittenFinished()");
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
            e.printStackTrace();
        }

    }
}
