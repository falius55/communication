package tundokumanager;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import communication.OnceSwapper;
import communication.Swapper;
import communication.receiver.Receiver;
import communication.sender.Sender;
import communication.server.NonBlockingServer;
import communication.server.Server;

public class MainServer implements AutoCloseable {
    private static final int PORT = 7100;
    private Server mServer = null;

    public Future<?> exec(int port) {

        Swapper.SwapperFactory swapperFactory = new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public Sender swap(String remoteAddress, Receiver receiver) {
                        int requestCode = receiver.getInt();
                        RequestHandler handler = RequestHandler.fromCode(requestCode);
                        return handler.handle(receiver);
                    }

                };
            }
        };

        Server server = new NonBlockingServer(port, swapperFactory);
        mServer = server;
        return server.startOnNewThread();
    }

    public void shutDown() throws IOException {
        mServer.shutdown();
    }

    public void close() throws IOException {
        if (mServer != null) {
            mServer.close();
        }
    }

    public static void main(String... strings) {
        System.out.println("Main Server");

        try (MainServer server = new MainServer ()) {
            Future<?> future = server.exec(PORT);
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
