package jp.gr.java_conf.falius.communication.client;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.senddata.SendData;

public interface JITClient extends Client {

    void send(SendData sendData) throws IOException, TimeoutException;
}
