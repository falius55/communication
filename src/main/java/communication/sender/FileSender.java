package communication.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSender extends ExtendableSender {

    public FileSender(Sender sender) {
        super(sender);
    }

    public Sender put(Path filePath) throws IOException {
        return put(Files.readAllBytes(filePath));
    }

    public Sender put(File file) throws FileNotFoundException, IOException {
        return put(new FileInputStream(file));
    }

    public Sender putFile(String firstPathName, String... morePathName) throws IOException {
        Path path = Paths.get(firstPathName, morePathName);
        return put(path);
    }
}
