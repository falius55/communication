package communication.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReceiver extends ExtendableReceiver {


    public FileReceiver(Receiver receiver) {
        super(receiver);
    }

    /**
     * @throws InvalidPathException　不正なファイル名で保存しようとした場合。':'や空白、/などの文字が含まれているなど
     * @throws FileAlreadyExistsExcepton openOptionにCREATE_NEWを渡し、かつファイルがすでに存在した場合
     */
    public void getAndSave(Path savePath, OpenOption... openOptions) throws IOException {
        getAndOutput(Files.newOutputStream(savePath, openOptions));
    }

    public void getAndSave(File file) throws FileNotFoundException, IOException {
        getAndOutput(new FileOutputStream(file));
    }

    public void getAndSave(File file, boolean append) throws FileNotFoundException, IOException {
        getAndOutput(new FileOutputStream(file, append));
    }

    public void getAndSave(String firstPathName, String... morePathName) throws IOException {
        Path path = Paths.get(firstPathName, morePathName);
        getAndSave(path);
    }
}