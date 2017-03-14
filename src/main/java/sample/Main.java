package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import communication.RepeatSwapper;
import communication.Swapper;
import communication.receiver.OnReceiveListener;
import communication.receiver.Receiver;
import communication.sender.MultiDataSender;
import communication.sender.OnSendListener;
import communication.sender.Sender;
import communication.server.NonBlockingServer;
import communication.server.Server;
import communication.server.Server.OnAcceptListener;
import communication.server.Server.OnShutdownCallback;

public class Main {
    private static final int mPort = 6200;

    public static void main(String[] args) throws Exception {
        System.out.println("main class executed");
        Server server = new NonBlockingServer(mPort, new Swapper.SwapperFactory() {
            @Override
            public Swapper get() {
                return new RepeatSwapper() {
                    private int i = 0;

                    @Override
                    public Sender swap(String remoteAddress, Receiver receiver) {
                        System.out.println("from " + remoteAddress);

                        System.out.println("data count:" + receiver.dataCount());
                        System.out.println("receive msg:" + receiver.getString());
                        System.out.println("receive msg:" + receiver.getString());
                        System.out.println("receive msg:" + receiver.getString());
                        System.out.println("receive msg:" + receiver.getInt());
                        System.out.println("receive msg:" + receiver.getString());

                        Sender sender = new MultiDataSender();
                        String rtn = "I am server. I got message from you:" + remoteAddress + "(" + i;
                        System.out.println("return value :" + rtn);
                        sender.put(rtn);
                        i++;
                        if (i > 4) {
                            finish();
                        }
                        return sender;
                    }
                };
            }
        });

        server.addOnAcceptListener(new OnAcceptListener() {
            @Override
            public void onAccept(String remoteAddress) {
                System.out.println("accept from " + remoteAddress);
            }
        });

        server.addOnReceiveListener(new OnReceiveListener() {

            @Override
            public void onReceive(String fromAddress, int readByte, Receiver receiver) {
                System.out.println("on receive listener from " + fromAddress + ":" + readByte);

            }
        });

        server.addOnSendListener(new OnSendListener() {

            @Override
            public void onSend(int writeSize) {
                // TODO 自動生成されたメソッド・スタブ
             System.out.println("on send written:" + writeSize);
            }

        });

        server.addOnShutdownCallback(new OnShutdownCallback() {
            @Override
            public void onShutdown() {
                System.out.println("shutdown server.\nbye.");
            }
        });

        System.out.println("connect server");
        try (Server sv = server; BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            sv.startOnNewThread();
            while (true) {
                System.out.println("need exit or quit if you want to stop server.");
                String line = br.readLine();
                if (line.equals("exit") || line.equals("quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
