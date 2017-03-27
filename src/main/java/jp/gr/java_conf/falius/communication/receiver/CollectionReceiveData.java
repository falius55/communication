package jp.gr.java_conf.falius.communication.receiver;

import java.lang.invoke.WrongMethodTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jp.gr.java_conf.falius.util.range.IntRange;

public class CollectionReceiveData extends ExtendableReceiveData {
    private static final List<String> EMPTY_LIST = new ArrayList<>(0);
    private static final Map<String, String> EMPTY_MAP = new HashMap<>(0);

    public CollectionReceiveData(ReceiveData receiveData) {
        super(receiveData);
    }

    /**
     *
     * @return
     * @throws WrongMethodTypeException Listに変換できないデータをこのメソッドで取得しようとした場合
     */
    public List<String> getList() {
        String jsonString = getString();
        if (jsonString == null) {
            return EMPTY_LIST;
        }
        try {
            JSONArray json = new JSONArray(jsonString);
            List<String> ret = new ArrayList<>();

            for (int i : new IntRange(json.length())) {
                ret.add(json.getString(i));
            }
            return ret;
        } catch (JSONException e) {
            throw new WrongMethodTypeException("could not convert to List data " + jsonString);
        }
    }

    /**
     *
     * @return
     * @throws WrongMethodTypeException Mapに変換できないデータをこのメソッドで取得しようとした場合
     */
    public Map<String, String> getMap() {
        String jsonString = getString();
        if (jsonString == null) {
            return EMPTY_MAP;
        }
        try {
            JSONObject json = new JSONObject(jsonString);
            Map<String, String> ret = new HashMap<>();

            for (String key : json.keySet()) {
                ret.put(key, json.getString(key));
            }
            return ret;
        } catch (JSONException e) {
            throw new WrongMethodTypeException("could not convert to Map data " + jsonString);
        }
    }
}
