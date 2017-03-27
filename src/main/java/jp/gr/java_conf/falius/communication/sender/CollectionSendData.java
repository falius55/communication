package jp.gr.java_conf.falius.communication.sender;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class CollectionSendData extends ExtendableSendData {

    public CollectionSendData(SendData sender) {
        super(sender);
    }

    public SendData put(List<String> list) {
        JSONArray json = new JSONArray(list);
        return put(json.toString());
    }

    public SendData pub(Map<String, String> map) {
        JSONObject json = new JSONObject(map);
        return put(json.toString());
    }
}
