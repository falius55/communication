package jp.gr.java_conf.falius.communication.helper;

import java.io.IOException;

public interface ServerHelper {

    void beforeClass() throws IOException;

    void afterClass() throws IOException;

    int getPort();

    String getAddress();
}
