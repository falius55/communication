package jp.gr.java_conf.falius.communication.sender;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class CollectionSender extends ExtendableSender {

    public CollectionSender(Sender sender) {
        super(sender);
    }

    public Sender put(List<String> list) {
        JSONArray json = new JSONArray(list);
        return put(json.toString());
    }

    public Sender pub(Map<String, String> map) {
        JSONObject json = new JSONObject(map);
        return put(json.toString());
    }
}
