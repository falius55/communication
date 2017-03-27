package jp.gr.java_conf.falius.communication.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import jp.gr.java_conf.falius.util.range.IntRange;

public class CollectionReceiveData extends ExtendableReceiveData {

    public CollectionReceiveData(ReceiveData receiveData) {
        super(receiveData);
    }

    public List<String> getList() {
        String jsonString = getString();
        if (jsonString == null) {
            return null;
        }
        JSONArray json = new JSONArray(jsonString);
        List<String> ret = new ArrayList<>();

        for (int i : new IntRange(json.length())) {
            ret.add(json.getString(i));
        }
        return ret;
    }

    public Map<String, String> getMap() {
        String jsonString = getString();
        if (jsonString == null) {
            return null;
        }
        JSONObject json = new JSONObject(jsonString);
        Map<String, String> ret = new HashMap<>();

        for (String key : json.keySet()) {
            ret.put(key, json.getString(key));
        }
        return ret;
    }
}
