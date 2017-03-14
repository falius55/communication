package communication;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface Disconnectable {

    /**
     * 
     * @param channel
     * @param key
     * @param cause 切断の原因。正常な終了のため呼び出した場合はnullが渡される
     */
    public void disconnect(SocketChannel channel, SelectionKey key, Throwable cause);
}
