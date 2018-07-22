package jp.gr.java_conf.falius.communication.senddata;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * コレクションの形でデータを送信する際に利用するSendDataです。
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public class CollectionSendData extends ExtendableSendData {

    /**
     *
     * @param sendData
     * @since 1.4.0
     */
    public CollectionSendData(SendData sendData) {
        super(sendData);
    }

    /**
     *
     * @param list
     * @return
     * @since 1.4.0
     */
    public SendData put(List<?> list) {
        JSONArray json = new JSONArray(list);
        return put(json.toString());
    }

    /**
     *
     * @param map
     * @return
     * @since 1.4.0
     */
    public SendData put(Map<?, ?> map) {
        JSONObject json = new JSONObject(map);
        return put(json.toString());
    }
}
