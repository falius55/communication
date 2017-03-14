package main;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import communication.OnceSwapper;
import communication.Swapper;
import communication.receiver.Receiver;
import communication.sender.MultiDataSender;
import communication.sender.Sender;
import communication.server.NonBlockingServer;
import communication.server.Server;

public class DirectoryServer implements AutoCloseable {
    private static final int PORT = 7100;
    private Server mServer = null;

    public Future<?> exec(int port) {

        Swapper.SwapperFactory swapperFactory = new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public Sender swap(String remoteAddress, Receiver receiver) {
                        String directory = receiver.getString();
                        System.out.println("directory name:" + directory);
                        StringJoiner sjDirs = new StringJoiner(",", "[", "]");
                        StringJoiner sjFiles = new StringJoiner(",", "[", "]");
                        File dir = new File(directory);

                        System.out.println("file exists:" + dir.exists());
                        for (File file : dir.listFiles()) {
                            if (file.isDirectory()) {
                                sjDirs.add("\"" + file.getAbsolutePath() + "\"");
                            } else if (file.isFile()) {
                                sjFiles.add("\"" + file.getAbsolutePath() + "\"");
                            }
                        }
                        System.out.println("dirs:" + sjDirs.toString());
                        System.out.println("files:" + sjFiles.toString());

                        Sender sender = new MultiDataSender();
                        sender.put(sjDirs.toString());
                        sender.put(sjFiles.toString());
                        return sender;
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

        try (DirectoryServer server = new DirectoryServer()) {
            Future<?> future = server.exec(PORT);
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
