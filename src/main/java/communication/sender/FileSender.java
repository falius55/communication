package communication.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import jp.gr.java_conf.falius.communication.sender.ExtendableSender;
import jp.gr.java_conf.falius.communication.sender.Sender;

public class FileSender extends ExtendableSender {

    public FileSender(Sender sender) {
        super(sender);
    }

    public Sender put(File file) throws FileNotFoundException, IOException {
        return put(new FileInputStream(file));
    }

    public Sender putFile(String filePath) throws IOException {
        File file = new File(filePath);
        return put(file);
    }
}