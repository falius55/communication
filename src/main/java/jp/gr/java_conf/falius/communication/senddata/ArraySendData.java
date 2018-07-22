package jp.gr.java_conf.falius.communication.senddata;

import java.util.Arrays;

import org.json.JSONArray;

/**
 * 配列を送信する際に利用するSejindDataです。
 * 配列を１単位として送信します。
 * @author "ymiyauchi"
 * @since 1.4.1
 *
 */
public class ArraySendData extends ExtendableSendData {

    /**
     *
     * @param sendData
     * @since 1.4.1
     */
    public ArraySendData(SendData sendData) {
        super(sendData);
    }

    /**
     *
     * @param data
     * @since 1.4.1
     */
    public SendData put(int[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }

    /**
     *
     * @param data
     * @since 1.4.1
     */
    public SendData put(String[] data) {
        JSONArray json = new JSONArray(Arrays.asList(data));
        return put(json.toString());
    }

    /**
     *
     * @param data
     * @since 1.4.1
     */
    public SendData put(long[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }

    /**
     *
     * @param data
     * @since 1.4.1
     */
    public SendData put(double[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }

    /**
     *
     * @param data
     * @since 1.4.1
     */
    public SendData put(float[] data) {
        JSONArray json = new JSONArray(data);
        return put(json.toString());
    }
}
