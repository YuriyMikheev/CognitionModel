package cognitionmodel.datasets;

import java.nio.charset.Charset;
import java.util.*;


public class CSVParser implements TabularParser {

    private String delimiter = "\t";
    private String endofline = "\r\n";
    private Tuple header;
    private HashSet<String>[] terminalsByfieldIndex;

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
     * @param data - read data from stream
     * @return - list of parsed tuples
     */

    @Override
    public List parse(byte[] data) {

        if (Charset.defaultCharset() == null) {
            throw new UnsupportedOperationException("CSV Parser: Default charset decoder is undefined. Can't parse the data");
        }

        String in = new String(data);

        String[] lines = in.split(endofline, -1);

        LinkedList<Tuple> r = new LinkedList<>();

        header = new Tuple().addAll(lines[0].split(delimiter, -1));

        terminalsByfieldIndex =new HashSet[header.size()];

        for (int i = 0; i < terminalsByfieldIndex.length; i++)
            terminalsByfieldIndex[i] = new HashSet<>();

        for (int i = 1; i < lines.length; i++) {
            Tuple tuple = new Tuple().addAll(lines[i].split(delimiter,-1));
            r.add(tuple);

            for (int j = 0; j < tuple.size(); j++)
                terminalsByfieldIndex[j].add(tuple.get(j).getValue().toString());
        }

        return r;
    }

    @Override
    public String[] terminals(int fieldIndex) {
        return terminalsByfieldIndex[fieldIndex].toArray(new String[]{});
    }


    @Override
    public Tuple getHeader() {
        return header;
    }
}
