package sample;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import communication.OnceSwapper;
import communication.receiver.FileReceiver;
import communication.receiver.OnReceiveListener;
import communication.receiver.Receiver;
import communication.sender.MultiDataSender;
import communication.sender.OnSendListener;
import communication.sender.Sender;
import communication.server.NonBlockingServer;
import communication.server.Server;

public class FileReceiverMain {
    private static final int PORT = 7000;

    public static void main(String[] args) {
        FileReceiverMain filer = new FileReceiverMain();
        filer.exec();

    }

    public void exec() {
        try (Server server = new NonBlockingServer(PORT, FileSwapper::new)) {
            server.addOnAcceptListener(new Server.OnAcceptListener() {

                @Override
                public void onAccept(String remoteAddress) {
                    System.out.println("accept " + remoteAddress);

                }
            });
            server.addOnReceiveListener(new OnReceiveListener() {

                @Override
                public void onReceive(String fromAddress, int readByte, Receiver receiver) {
                    System.out.println("receive:" + readByte);
                }

            });
            server.addOnSendListener(new OnSendListener() {

                @Override
                public void onSend(int writeSize) {
                    System.out.println("send:" + writeSize);

                }

            });
            Future<?> future = server.startOnNewThread();
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static class FileSwapper extends OnceSwapper {
        private static final String SAVE_PATH = "C:\\";

        @Override
        public Sender swap(String remoteAddress, Receiver receiver) {
            Sender sender = new MultiDataSender();

            String fileName = receiver.getString();
            System.out.println("filename:" + fileName);
            try {
                FileReceiver fileReceiver = new FileReceiver(receiver);
                fileReceiver.getAndSave(Paths.get(SAVE_PATH, fileName),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException in swap()");
                sender.put("failed save file");
                return sender;
            }

            sender.put("save file");
            System.out.println("send");
            return sender;
        }
    }

}
