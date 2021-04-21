package cognitionmodel.datasets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Saves data and data type. The basic element of inner data representation
 */

public class TupleElement {
    byte[] data;
    enum Type {
        Char,
        Empty,
        String,
        Int,
        Double,
        ByteArray
    }

    Type type = Type.Empty;

    public TupleElement(){
        data = new byte[0];
    }

    public TupleElement(String value) {

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

            if (value.length() == 1){
               data = value.getBytes(Charset.defaultCharset());
               type = Type.Char;
               return;
            }

            data = value.getBytes(Charset.defaultCharset());
            type = Type.String;
            return;
        }

        data = new byte[0];
        type = Type.Empty;
    }

    public TupleElement(Integer value) {
        data = ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
        type = Type.Int;
    }

    public TupleElement(Double value) {
        data = ByteBuffer.allocate(Double.BYTES).putDouble(value).array();
        type = Type.Double;
    }

    public TupleElement(byte[] value) {
        data = ByteBuffer.allocate(value.length).put(value).array();
        type = Type.ByteArray;
    }

    @Override
    public String toString() {

        String val = get().toString();



        return "{" +
                "data=" + val +
                ", type=" + type +
                '}';
    }

    public Object get() {

        Object val = null;

        switch (type) {
            case String: {
                val = new String(data);
                break;
            }

            case Double: {
                val = ByteBuffer.allocate(Double.BYTES).put(data).position(0).getDouble();
                break;
            }

            case Int: {
                val = ByteBuffer.allocate(Integer.BYTES).put(data).position(0).getInt();
                break;
            }

            case Empty: {
                val = "";
                break;
            }

            case ByteArray: {
                val = data;
                break;
            }

            case Char: {
                val = new String(data);
                break;
            }
        }

        return val;
    }


}
