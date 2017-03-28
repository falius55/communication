package jp.gr.java_conf.falius.communication.sender;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * コレクションの形でデータを送信する際に利用するSendDataです。
 * @author "ymiyauchi"
 *
 */
public class CollectionSendData extends ExtendableSendData {

    public CollectionSendData(SendData sender) {
        super(sender);
    }

    public SendData put(List<?> list) {
        JSONArray json = new JSONArray(list);
        return put(json.toString());
    }

    public SendData put(Map<?, ?> map) {
        JSONObject json = new JSONObject(map);
        return put(json.toString());
    }
}
