package jp.gr.java_conf.falius.communication.header;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

public interface Header {
    int size();

    int allDataSize();

    int itemDataSize(int index);

    IntBuffer dataSizeBuffer();

    ByteBuffer toByteBuffer();

    Header read(SocketChannel channel) throws IOException;

    boolean isReadFinished();
}
