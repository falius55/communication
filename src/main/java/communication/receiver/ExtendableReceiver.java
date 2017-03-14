package communication.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class ExtendableReceiver implements Receiver {
    private final Receiver mSource;

    public ExtendableReceiver(Receiver receiver) {
        mSource = receiver;
    }

    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mSource.addOnReceiveListener(listener);
    }

    @Override
    public Result receive(SocketChannel channel) throws IOException {
        return mSource.receive(channel);
    }

    @Override
    public int dataCount() {
        return mSource.dataCount();
    }

    @Override
    public ByteBuffer get() {
        return mSource.get();
    }

    @Override
    public ByteBuffer[] getAll() {
        return mSource.getAll();
    }

    @Override
    public String getString() {
        return mSource.getString();
    }

    @Override
    public int getInt() {
        return mSource.getInt();
    }

    @Override
    public void getAndOutput(OutputStream os) throws IOException {
        mSource.getAndOutput(os);
    }

    @Override
    public void clear() {
        mSource.clear();

    }

}
