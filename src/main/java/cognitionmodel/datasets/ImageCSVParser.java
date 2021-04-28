package cognitionmodel.datasets;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Parses EMINST CSV Data set that consists columns {label,{(i<imageHeight) * (j<imageWidth)}}
 */

public class ImageCSVParser implements TabularParser {

    private String delimiter = "\t";
    private String endofline = "\r\n";
    private int[] intervals;
    private Tuple header;
    private HashSet<String>[] terminalindices;

    public ImageCSVParser() {
    }


    /**
     * Parses text data from csv format to inner representation
     * @param delimiter - values delimiter
     * @param endofline - lines break char sequence
     * @param intervals - array of intervals. Pixels are fitted to intervals, for example intervals {0,100,256} produce two value of pixels from data. in this case px = 50 -> 0 and px = 125 -> 1
     */
    public ImageCSVParser(String delimiter, String endofline, int[] intervals) {
        this.delimiter = delimiter;
        this.endofline = endofline;
        this.intervals = intervals;
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
            throw new UnsupportedOperationException("CSV Parser: Default charset decoder is undefined. Can't parse the data");
        }

        String in = new String(data);

        String[] lines = in.split(endofline);

        LinkedList<Tuple> r = new LinkedList<>();

        header = new Tuple().addAll(lines[0].split(delimiter, -1));

        terminalindices  =new HashSet[header.size()];

        for (int i = 0; i < terminalindices.length; i++)
            terminalindices[i] = new HashSet<>();

        for (int i = 1; i < lines.length; i++) {

            String[] line = lines[i].split(delimiter,-1);

            for (int j = 1; j < line.length; j++)
                line[j] = pixelfilter(line[j]);

            Tuple tuple = new Tuple().addAll(line);
            r.add(tuple);

            for (int j = 0; j < tuple.size(); j++)
                terminalindices[j].add(tuple.get(j).getValue().toString());
        }

        return r;
    }

    @Override
    public Tuple getHeader() {
        return header;
    }

    private String pixelfilter(String value){
        try {
            if (value != "" && value.trim() != "" && !value.isEmpty()) {
                int v = Integer.valueOf(value);

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
        return terminalindices[fieldIndex].toArray(new String[]{});
    }

}