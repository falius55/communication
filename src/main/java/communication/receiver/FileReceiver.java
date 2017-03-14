package communication.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.gr.java_conf.falius.communication.receiver.ExtendableReceiver;

public class FileReceiver extends ExtendableReceiver {


    public FileReceiver(Receiver receiver) {
        super(receiver);
    }

    public void getAndSave(File file) throws FileNotFoundException, IOException {
        getAndOutput(new FileOutputStream(file));
    }

    public void getAndSave(File file, boolean append) throws FileNotFoundException, IOException {
        getAndOutput(new FileOutputStream(file, append));
    }

    public void getAndSave(String filePath) throws IOException {
        getAndSave(new File(filePath));
    }
}
