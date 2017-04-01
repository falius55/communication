package jp.gr.java_conf.falius.communication.senddata;

import java.util.Arrays;

import org.json.JSONArray;

public class ArraySendData extends ExtendableSendData {

    public ArraySendData(SendData sender) {
        super(sender);
    }

    public SendData put(int[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }

    public SendData put(String[] data) {
        JSONArray json = new JSONArray(Arrays.asList(data));
        return put(json.toString());
    }

    public SendData put(long[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }

    public SendData put(double[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }

    public SendData put(float[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }
}
