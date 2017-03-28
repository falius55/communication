package jp.gr.java_conf.falius.communication.helper;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;

public interface ClientHelper {

    @SuppressWarnings("unchecked")
    <T> ReceiveData send(T... sendData) throws IOException, TimeoutException;
}
