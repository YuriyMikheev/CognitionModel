package cognitionmodel.datasets.parsers;

import cognitionmodel.datasets.Tuple;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ArffParser implements TabularParser {

    private String delimiter = ",";
    private String endofline = "\n";
    private Tuple header = new Tuple();
    private HashSet<String>[] terminalsByfieldIndex;
    private HashSet<String>  fields = new HashSet<>();
    private boolean include;

    public ArffParser() {
    }

    public ArffParser(boolean include, String ...fields) {
        this.include = include;
        this.fields = new  HashSet();
        Arrays.stream(fields).forEach(this.fields::add);
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getEndofline() {
        return endofline;
    }


    @Override
    public List<Tuple> parse(InputStream inputStream) throws IOException {
        return parse(inputStream.readAllBytes());
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
            if (lines[i].toLowerCase().equals("@data")) break;
            String[] l = lines[i].split(" ");
            if (l[0].toLowerCase().equals("@attribute")) header.add(l[1]);
        }


        LinkedList<Tuple> r = new LinkedList<>();

        terminalsByfieldIndex =new HashSet[header.size()];

        for (int j = 0; j < terminalsByfieldIndex.length; j++)
            terminalsByfieldIndex[j] = new HashSet<>();

        LinkedList<CompletableFuture<Tuple>> cfl = new LinkedList<>();


        i++;
        for (;i < lines.length; i++){
            if (!lines[i].isEmpty()) {
                int finalI = i;
                cfl.add(CompletableFuture.supplyAsync(() -> {

                    Tuple tuple = new Tuple();//.addAll(lines[i].split(delimiter,-1));

                    int j = 0;
                    for (String v : lines[finalI].split(delimiter, -1)) {
                        if (!(include ^ fields.contains(header.get(j).getValue().toString())))
                            tuple.add(v);
                        j++;
                    }
                    //r.add(tuple);

                    for (j = 0; j < tuple.size(); j++)
                        terminalsByfieldIndex[j].add(tuple.get(j).getValue().toString());
                    return tuple;
                }));
            }
            if (i % (int) (lines.length * 0.01 + 1) == 0 | i == lines.length)
            {
                cfl.stream().forEach(t -> r.add(t.join()));//map(m -> m.join()).collect(Collectors.toList());
                cfl.clear();
            }

        }

        Tuple h = new Tuple();
        for ( i = 0; i < header.size(); i++)
            if (!(include ^ fields.contains(header.get(i).getValue().toString())))
                h.add(header.get(i));

        header = h;
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
