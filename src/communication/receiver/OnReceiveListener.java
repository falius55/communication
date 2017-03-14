package communication.receiver;

public interface OnReceiveListener {

    void onReceive(String fromAddress, int readByte, Receiver receiver);
}
