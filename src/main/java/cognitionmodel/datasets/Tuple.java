package cognitionmodel.datasets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

        if (value == null) throw new  NullPointerException();

        if (!value.isBlank()) {
            try {
                Integer d = Integer.parseInt(value.trim());
                data = ByteBuffer.allocate(Integer.BYTES).putInt(d).array();
                type = Type.Int;
                return;
            } catch (NumberFormatException e) {
                //System.out.println(value + " is No Int");
            }

            try {
                Double d = Double.parseDouble(value);
                data = ByteBuffer.allocate(Double.BYTES).putDouble(d).array();
                type = Type.Double;
                return;
            } catch (NumberFormatException e){

            }
            data = value.getBytes(Charset.defaultCharset());
            type = Type.String;
            return;
        }

        data = new byte[0];
        type = Type.Empty;
    }

    public Tuple(Integer value) {
        data = ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
        type = Type.Int;
    }

    public Tuple(Double value) {
        data = ByteBuffer.allocate(Double.BYTES).putDouble(value).array();
        type = Type.Double;
    }

    public Tuple(byte[] value) {
        data = ByteBuffer.allocate(value.length).put(value).array();
        type = Type.ByteArray;
    }

    @Override
    public String toString() {

        String val = "Empty";

        switch (type) {
            case String: {
                val = new String(data);
                break;
            }

            case Double: {
                val = Double.toString(ByteBuffer.allocate(Double.BYTES).put(data).position(0).getDouble());
                break;
            }

            case Int: {
                val = Integer.toString(ByteBuffer.allocate(Integer.BYTES).put(data).position(0).getInt());
                break;
            }

            case Empty: {
                val = "";
                break;
            }

            case ByteArray: {
                val = Arrays.toString(data);
                break;
            }
        }

        return "Tuple{" +
                "data=" + val +
                ", type=" + type +
                '}';
    }
}
