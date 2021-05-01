package cognitionmodel.datasets;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

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

public class TupleElement implements Serializable, Cloneable {
    Object data;
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


    /**
     * Creates element of the tuple. Autodetect type of the element
     * @param value
     */


    public TupleElement(String value) {

        if (value == null) throw new  NullPointerException();

        if (!value.isBlank()) {

            String s = value.trim();
            char firstchar = s.charAt(0);
            if (firstchar =='+' | firstchar =='-' | firstchar =='.' | (firstchar >= '0' & firstchar <= '9')) {

                try {
                    data = Integer.parseInt(s);
                    type = Type.Int;
                    return;
                } catch (NumberFormatException e) {
                    //System.out.println(value + " is No Int");
                }

                try {
                    data = Double.parseDouble(s);
                    type = Type.Double;
                    return;
                } catch (NumberFormatException e) {

                }
            }

            if (value.length() == 1){
               data = value;
               type = Type.Char;
               return;
            }

            data = value;
            type = Type.String;
            return;
        }

        data = new byte[0];
        type = Type.Empty;
    }

    public TupleElement(Integer value) {
        data = value;
        type = Type.Int;
    }

    public TupleElement(Double value) {
        data = value;
        type = Type.Double;
    }

    public TupleElement(byte[] value) {
        data = value;
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

        return data;
    }

    /**
     * Gets type of tuple element
     * @return
     */

    public Type getType() {
        return type;
    }
}
