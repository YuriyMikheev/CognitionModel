package cognitionmodel.datasets;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.NumberFormat;

/**
 * Saves data and data type. The basic element of inner data representation.
 * Available types of data:
 *         Char,
 *         Empty,
 *         String,
 *         Int,
 *         Double,
 *         ByteArray
 *
 */

public class TupleElementOld implements Serializable, Cloneable {
    byte[] data;
    public enum Type {
        Char,
        Empty,
        String,
        Int,
        Double,
        ByteArray
    }

    public static NumberFormat numberFormat = NumberFormat.getInstance();

    Type type = Type.Empty;

    public TupleElementOld(){
        data = new byte[0];
    }

    /**
     * Creates element of the tuple. Autodetect type of the element
     * @param value
     */


    public TupleElementOld(String value) {

        if (value == null) throw new  NullPointerException();

        if (!value.isBlank()) {

            String s = value.trim();
            char firstchar = s.charAt(0);
            if (firstchar =='+' | firstchar =='-' | firstchar =='.' | (firstchar >= '0' & firstchar <= '9')) {

                try {
                    Integer d = Integer.parseInt(s);
                    data = ByteBuffer.allocate(Integer.BYTES).putInt(d).array();
                    type = Type.Int;
                    return;
                } catch (NumberFormatException e) {
                    //System.out.println(value + " is No Int");
                }

                try {
                    Double d = Double.parseDouble(s);
                    data = ByteBuffer.allocate(Double.BYTES).putDouble(d).array();
                    type = Type.Double;
                    return;
                } catch (NumberFormatException e) {

                }
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

    /**
     *
     * @param value
     */

    public TupleElementOld(Integer value) {
        data = ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
        type = Type.Int;
    }

    public TupleElementOld(Double value) {
        data = ByteBuffer.allocate(Double.BYTES).putDouble(value).array();
        type = Type.Double;
    }

    public TupleElementOld(byte[] value) {
        data = ByteBuffer.allocate(value.length).put(value).array();
        type = Type.ByteArray;
    }

    @Override
    public String toString() {
        return "{" +
                "data=" + getValue().toString() +
                ", type=" + type +
                '}';
    }

    /**
     * Get value of the tuple element.
     * @return - object with type according to element type
     */

    public Object getValue() {

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

    /**
     * Gets type of tuple element
     * @return
     */

    public Type getType() {
        return type;
    }
}
