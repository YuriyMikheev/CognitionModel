package cognitionmodel.datasets;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



public class CSVParser implements Parser {

    private String delimiter = "\t";
    private String endofline = "\r\n";

    public CSVParser() {
    }


    /**
     * Parses text data from csv format to inner representation
     * @param delimiter - values delimiter
     * @param endofline - lines break char sequence
     */
    public CSVParser(String delimiter, String endofline) {
        this.delimiter = delimiter;
        this.endofline = endofline;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getEndofline() {
        return endofline;
    }

    /**
     * Returns set of @Link Tuples (set of @Link Tuple) representing data from csv stream
     * @param data
     * @return
     */

    @Override
    public List get(byte[] data) {

        if (Charset.defaultCharset() == null) {
            System.err.println("CSV Parser: Default charset decoder is undefined. Can't parse the data");
            throw new UnsupportedOperationException();
        }

        String in = new String(data);

        String[] lines = in.split(endofline);

        LinkedList<Tuple> r = new LinkedList<>();

        for (String s: lines) {
            ArrayList<TupleElement> t = new ArrayList<>();
            for (String ss : s.split(delimiter)) {
                t.add(new TupleElement(ss));
            }
            r.add(new Tuple(t));
        }
        return r;
    }
}
