package tundokumanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import communication.receiver.FileReceiver;
import communication.receiver.Receiver;
import communication.sender.MultiDataSender;
import communication.sender.Sender;

public enum RequestHandler {
    FILE_SAVE(0) {
        @Override
        public Sender handle(Receiver receiver) {
            // 受け取り
            // ファイル名(文字列)
            // 保存パス
            // ファイルデータ
            // 送信
            //  保存成功： "save file"
            //　保存失敗： "failed save file"
            Sender sender = new MultiDataSender();

            String fileName = receiver.getString();
            String savePath = receiver.getString();
            try {
                FileReceiver fileReceiver = new FileReceiver(receiver);
                fileReceiver.getAndSave(Paths.get(savePath, fileName),
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
    },
    DIRECTORY_ASK(1) {
        @Override
        public Sender handle(Receiver receiver) {
            // 受け取り
            // 検索ディレクトリの絶対名
            // 送信
            // ディレクトリ一覧の配列のＪｓｏｎ文字列
            // ファイル一覧の配列のＪｓｏｎ文字列
            String directory = receiver.getString();
            StringJoiner sjDirs = new StringJoiner(",", "[", "]");
            StringJoiner sjFiles = new StringJoiner(",", "[", "]");

            File[] files;
            if (directory.equals("root")) {
                files = File.listRoots();
            } else {
                File dir = new File(directory);
                files = dir.listFiles();
            }

            for (File file : files) {
                if (!file.canRead() || !file.canWrite()
                        || file.getAbsolutePath().equals("C:\\Documents and Settings")
                        || file.getAbsolutePath().equals("C:\\$Recycle.Bin")
                        || file.getAbsolutePath().equals("C:\\System Volume Information")
                        || file.getAbsolutePath().equals("C:\\Recovery")) {
                    continue;
                }

                StringJoiner sjPathJson = new StringJoiner(",", "[", "]");
                String[] paths = file.getAbsolutePath().split("\\\\");
                for (String path : paths) {
                    sjPathJson.add("\"" + path + "\"");
                }

                if (file.isDirectory()) {
                    sjDirs.add(sjPathJson.toString());
                } else if (file.isFile()) {
                    sjFiles.add(sjPathJson.toString());
                }
            }

            Sender sender = new MultiDataSender();
            sender.put(sjDirs.toString());
            sender.put(sjFiles.toString());
            return sender;
        }
    },
    ROOT_INFO(2) {
        @Override
        public Sender handle(Receiver receiver) {
            return null;
        }
    };

    private static final Map<Integer, RequestHandler> mFromCodeToEnum;

    static {
        mFromCodeToEnum = new HashMap<>();
        for (RequestHandler handler : values()) {
            mFromCodeToEnum.put(handler.getCode(), handler);
        }
    }

    public static RequestHandler fromCode(int code) {
        return mFromCodeToEnum.get(code);
    }

    private final int mCode;

    RequestHandler(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    public abstract Sender handle(Receiver receiver);
}
