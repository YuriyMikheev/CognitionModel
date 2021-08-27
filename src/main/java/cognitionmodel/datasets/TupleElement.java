package cognitionmodel.datasets;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

import static java.lang.Math.round;

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


    public TupleElement(Object value) {

        String val;
        if (value == null)  val = "";
            else
                val = value.toString();

        if (!val.isBlank()) {

            String s = val.trim();
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

            if (val.length() == 1){
               data = value;
               type = Type.Char;
               return;
            }

            data = value;
            type = Type.String;
            return;
        }

        data = "";
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

    /**
     * Gets tuple as double.
     *
     * @throws ClassCastException if can't convert data to double
     */

    public double asDouble(){
        if (type == Type.Int)  return (int) data*1.0;
        if (type == Type.Double) return (double) data*1.0;
        throw new ClassCastException ();
    }

    /**
     * Gets tuple as double.
     * Rounds double
     * @throws ClassCastException if can't convert data to double
     */

    public int asInt(){
        if (type == Type.Int) return (int) data;
        if (type == Type.Double) return (int)round((double)data);

        throw new ClassCastException ();
    }

    /**
     * Checks if value is numeric (int or double)
     * @return true if value is int or double
     */

    public boolean isNumber(){
        return (type == Type.Int | type == Type.Double);
    }

}
