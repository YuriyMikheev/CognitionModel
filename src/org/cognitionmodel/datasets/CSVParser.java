package org.cognitionmodel.datasets;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class CSVParser implements Parser {

    private String delimiter = "\t";
    private String endofline = "\r\n";

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

    @Override
    public List get(byte[] data) {

        if (Charset.defaultCharset() == null) {
            System.err.println("CSV Parser: Default charset decoder is undefined. Can't parse the data");
            throw new UnsupportedOperationException();
        }

        String in = new String(data);

        String[] lines = in.split(endofline);

        LinkedList<String[]> r = new LinkedList<>();

        for (String s: lines)
            r.add(s.split(delimiter));

        return r;
    }
}
