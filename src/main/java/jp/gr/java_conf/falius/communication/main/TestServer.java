package jp.gr.java_conf.falius.communication.main;

import java.io.IOException;
import java.io.UncheckedIOException;

import jp.gr.java_conf.falius.communication.OnDisconnectCallback;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.server.NonBlockingServer;
import jp.gr.java_conf.falius.communication.server.Server;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper.SwapperFactory;
import jp.gr.java_conf.falius.util.range.IntRange;

public class TestServer {
    private static final int PORT = 9000;

    public static void main(String...strings ) {
        try (Server server = new NonBlockingServer(PORT, new SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public Sender swap(String remoteAddress, Receiver receiver) {
                        Sender sender = new MultiDataSender();

                        for (int i : new IntRange(receiver.dataCount())) {
                            sender.put(receiver.get());
                        }
                        return sender;
                    }

                };
            }

        })) {

            server.addOnReceiveListener(new OnReceiveListener() {

                @Override
                public void onReceive(String fromAddress, int readByte, Receiver receiver) {
                    boolean isContinue = receiver.getBoolean();
                    if (!isContinue) {
                        try {
                            server.shutdown();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }

            });

            server.addOnDisconnectCallback(new OnDisconnectCallback() {

                @Override
                public void onDissconnect(String remote, Throwable cause) {
                    if (cause != null) {
                        try {
                            server.shutdown();
                        } catch (IOException e) {
                            throw new IllegalStateException();
                        }
                    }
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
