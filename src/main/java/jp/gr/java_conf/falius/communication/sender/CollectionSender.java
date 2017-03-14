package jp.gr.java_conf.falius.communication.sender;

import java.util.List;

import net.sf.json.JSONArray;

public class CollectionSender extends ExtendableSender {

    public CollectionSender(Sender sender) {
        super(sender);
    }

    public Sender put(List<String> list) {
        JSONArray json = JSONArray.fromObject(list);
        return put(json.toString());
    }
}
