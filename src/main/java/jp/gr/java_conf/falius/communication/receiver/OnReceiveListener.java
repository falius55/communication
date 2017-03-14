package jp.gr.java_conf.falius.communication.receiver;

public interface OnReceiveListener {

    void onReceive(String fromAddress, int readByte, Receiver receiver);
}
