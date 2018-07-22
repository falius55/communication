package jp.gr.java_conf.falius.communication.rcvdata;

import java.lang.invoke.WrongMethodTypeException;
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONException;

import jp.gr.java_conf.falius.util.range.IntRange;

/**
 *
 * @author "ymiyauchi"
 * @since 1.4.1
 *
 */
public class ArrayReceiveData extends ExtendableReceiveData {

    /**
     *
     * @param receiveData
     * @since 1.4.1
     */
    public ArrayReceiveData(ReceiveData receiveData) {
        super(receiveData);
    }

    /**
     *
     * @return
     * @since 1.4.1
     */
    public int[] getIntArray() {
        JSONArray json = getJson();
        int size = json.length();
        int[] ret = new int[size];
        for (int i : new IntRange(size)) {
            ret[i] = json.getInt(i);
        }
        return ret;
    }

    /**
     *
     * @return
     * @since 1.4.1
     */
    public String[] getStringArray() {
        JSONArray json = getJson();
        int size = json.length();
        String[] ret = new String[size];
        for (int i : new IntRange(size)) {
            ret[i] = json.getString(i);
        }
        return ret;
    }

    /**
     *
     * @return
     * @since 1.4.1
     */
    public long[] getLongArray() {
        JSONArray json = getJson();
        int size = json.length();
        long[] ret = new long[size];
        for (int i : new IntRange(size)) {
            ret[i] = json.getLong(i);
        }
        return ret;
    }

    /**
     *
     * @return
     * @since 1.4.1
     */
    public double[] getDoubleArray() {
        JSONArray json = getJson();
        int size = json.length();
        double[] ret = new double[size];
        for (int i : new IntRange(size)) {
            ret[i] = json.getDouble(i);
        }
        return ret;
    }

    /**
     *
     * @return
     * @since 1.4.1
     */
    public float[] getFloatArray() {
        JSONArray json = getJson();
        int size = json.length();
        float[] ret = new float[size];
        for (int i : new IntRange(size)) {
            ret[i] = (float)json.getDouble(i);
        }
        return ret;
    }

    /**
     *
     * @return
     * @since 1.4.1
     */
    private JSONArray getJson() {
        try {
            String strJson = getString();
            if (strJson == null) {
                throw new NoSuchElementException();
            }
            return new JSONArray(strJson);
        } catch (JSONException e) {
            throw new WrongMethodTypeException();
        }
    }
}
