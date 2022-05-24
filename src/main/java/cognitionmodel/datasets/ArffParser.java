package cognitionmodel.datasets;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class ArffParser implements TabularParser {

    private String delimiter = ",";
    private String endofline = "\n";
    private Tuple header = new Tuple();
    private HashSet<String>[] terminalsByfieldIndex;

    public ArffParser() {
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


        int i;
        for (i = 0; i < lines.length; i++){
            if (lines[i].equals("@data")) break;
            String[] l = lines[i].split(" ");
            if (l[0].equals("@attribute")) header.add(l[1]);
        }


        LinkedList<Tuple> r = new LinkedList<>();

        terminalsByfieldIndex =new HashSet[header.size()];

        for (int j = 0; j < terminalsByfieldIndex.length; j++)
            terminalsByfieldIndex[j] = new HashSet<>();

        i++;
        for (;i < lines.length; i++)
            if (!lines[i].isEmpty()) {
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
