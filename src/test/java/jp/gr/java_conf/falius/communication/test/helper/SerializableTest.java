package jp.gr.java_conf.falius.communication.test.helper;

import java.io.Serializable;

public class SerializableTest implements Serializable {
    private static final long serialVersionUID = 100;
    public enum Sex {
        MALE, FEMALE;
    }
    private final String mName;
    private final int mAge;
    private final Sex mSex;

    public SerializableTest(String name, int age, Sex sex) {
        mName = name;
        mAge = age;
        mSex = sex;
    }

    @Override
    public String toString() {
        return String.format("my name is %s. it\'s %d age. I\'m %s", mName, mAge, mSex);
    }
}
