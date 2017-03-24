package jp.gr.java_conf.falius.communication.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class CollectionReceiver extends ExtendableReceiver {

    public CollectionReceiver(Receiver receiver) {
        super(receiver);
    }

    public List<String> getList() {
        String jsonString = getString();
        JSONArray json = new JSONArray(jsonString);
        List<String> ret = new ArrayList<>();

        json.forEach(value -> ret.add(value.toString()));
        return ret;
    }

    public Map<String, String> getMap() {
        String jsonString = getString();
        JSONObject json = new JSONObject(jsonString);
        Map<String, String> ret = new HashMap<>();

        for (String key : json.keySet()) {
            ret.put(key, json.getString(key));
        }
        return ret;
    }
}
