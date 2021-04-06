package main.cognitionmodel.datasets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Saves data and data type. The basic element of inner data representation
 */



public class Tuple {
    byte[] data;
    enum Type {
        Empty,
        String,
        Int,
        Double,
        ByteArray
    }

    Type type = Type.Empty;

    public Tuple(){
        data = new byte[0];
    }

    public Tuple(String value) {
        data = value.getBytes(StandardCharsets.UTF_8);
        type = Type.String;
    }

    public Tuple(Integer value) {
        data = ByteBuffer.allocate(4).putInt(value).array();
        type = Type.Int;
    }

    public Tuple(Double value) {
        data = ByteBuffer.allocate(4).putDouble(value).array();
        type = Type.Double;
    }

    public Tuple(byte[] value) {
        data = ByteBuffer.allocate(value.length).put(value).array();
        type = Type.ByteArray;
    }
}
