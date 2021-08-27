package cognitionmodel.datasets;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Parses EMINST CSV Data set that consists columns {label,{(i<imageHeight) * (j<imageWidth)}}
 */

public class ImageCSVParser implements TabularParser {

    private String delimiter = "\t";
    private String endofline = "\r\n";
    private int[] intervals;
    private Tuple header = null;
    private HashMap<Integer, HashSet<String>> terminalindices = new HashMap<>();
    private int labelindex = 0;

    private UnaryOperator<String> elementtransferfunction = null;
    private UnaryOperator<Tuple> tupletransferfunction = null;

    /**
     * Creates ImageCSVParser with default parameters
     * ImageCSVParser parses text data from csv format to inner representation
     * delimiter = "\t" - values delimiter
     * endofline = "\r\n" - lines break char sequence
     * intervals = null - array of intervals. Pixels are fitted to intervals, for example intervals {0,100,256} produce two value of pixels from data. in this case px = 50 -> 0 and px = 125 -> 1
     * labelindex = 0 - index of label field
     * transferfunctions are null
     *
     */


    public ImageCSVParser() {
    }


    /**
     * Parses text data from csv format to inner representation
     * @param delimiter - values delimiter
     * @param endofline - lines break char sequence
     * @param labelindex - index of label field
     */
    public ImageCSVParser(String delimiter, String endofline,  int labelindex) {
        this.delimiter = delimiter;
        this.endofline = endofline;
        this.labelindex = labelindex;
    }

    /**
     * Sets integer intervals for rescaling input value to intervals scale
     * @param intervals - array of intervals. Pixels are fitted to intervals, for example intervals {0,100,256} produce two value of pixels from data. in this case px = 50 -> 0 and px = 125 -> 1
     *
     * @return this object
     */


    public ImageCSVParser setIntervals(int[] intervals){
        this.intervals = intervals;
        this.elementtransferfunction = this::pixelfilter;

        return this;
    }

    /**
     * Sets element transfer function String a -> String b
     * @param elementtransferfunction
     * @return this object
     */

    public ImageCSVParser setElementTransferFunction(UnaryOperator <String> elementtransferfunction){
        this.elementtransferfunction = elementtransferfunction;

        return this;
    }


    /**
     * Sets tuple transfer function String a -> String b
     * @param tupletransferfunction
     * @return this object
     */

    public ImageCSVParser setTupleTransferFunction(UnaryOperator<Tuple> tupletransferfunction){
        this.tupletransferfunction = tupletransferfunction;

        return this;
    }

    public ImageCSVParser setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public ImageCSVParser setEndofline(String endofline) {
        this.endofline = endofline;
        return this;
    }


    public ImageCSVParser setLabelindex(int labelindex) {
        this.labelindex = labelindex;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getEndofline() {
        return endofline;
    }

    private HashSet<String> getTermIndecies(int index){
        if (!terminalindices.containsKey(index))
            terminalindices.put(index, new HashSet<String>());

        return terminalindices.get(index);
    }


    /**
     * Returns set of @Link Tuples (set of @Link Tuple) representing data from csv stream
     * @param data
     * @return
     */

    @Override
    public List parse(byte[] data) {

        if (Charset.defaultCharset() == null) {
            throw new UnsupportedOperationException("CSV Parser: Default charset decoder is undefined. Can't parse the data");
        }

        String in = new String(data);

        String[] lines = in.split(endofline);

        LinkedList<Tuple> r = new LinkedList<>();

        header = new Tuple().addAll(lines[0].split(delimiter, -1));

        for (int i = 1; i < lines.length; i++) {

            String[] line = lines[i].split(delimiter,-1);

            if (elementtransferfunction != null)
                for (int j = 1; j < line.length; j++)
                    if (j != labelindex) line[j] = elementtransferfunction.apply(line[j]).toString();

            Tuple tuple = new Tuple().addAll(line);

            if (tupletransferfunction != null)
                tuple = tupletransferfunction.apply(tuple);

            r.add(tuple);

            for (int j = 0; j < tuple.size(); j++)
                getTermIndecies(j).add(tuple.get(j).getValue().toString());
        }

        return r;
    }

    @Override
    public Tuple getHeader() {
        return header;
    }

    private String pixelfilter(String value){
        try {
            String val = value.toString();
            if (value != "" && val.trim() != "" && !val.isEmpty()) {
                int v = Integer.valueOf(val);

                int ni = 0, dni = intervals.length / 2;
                if (intervals[0] < v)
                    while (ni >= 0 && ni < intervals.length - 1 && !(intervals[ni] <= v && v < intervals[ni + 1])) {
                        int sg = intervals[ni] < v ? 1 : -1;
                        ni = ni + sg * dni;
                        if (dni > 1) dni = dni / 2;
                    }

                return ni + "";
            }
        } catch (NumberFormatException e)
        {

        }
        return value;
    }

    public static String pixelfilter(String value, int[] intervals){
        try {
            String val = value.toString();
            if (value != "" && val.trim() != "" && !val.isEmpty()) {
                int v = Integer.valueOf(val);

                int ni = 0, dni = intervals.length / 2;
                if (intervals[0] < v)
                    while (ni >= 0 && ni < intervals.length - 1 && !(intervals[ni] <= v && v < intervals[ni + 1])) {
                        int sg = intervals[ni] < v ? 1 : -1;
                        ni = ni + sg * dni;
                        if (dni > 1) dni = dni / 2;
                    }

                return ni + "";
            }
        } catch (NumberFormatException e)
        {

        }
        return value;
    }


    @Override
    public String[] terminals(int fieldIndex) {
        return terminalindices.get(fieldIndex).toArray(new String[]{});
    }

}
