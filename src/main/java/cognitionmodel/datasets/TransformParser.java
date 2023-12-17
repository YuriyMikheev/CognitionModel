package cognitionmodel.datasets;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;


public class TransformParser implements TabularParser {


    private Tuple header = new Tuple();
    private HashSet<String>[] terminalsByfieldIndex;
    private HashSet<String>  fields = new HashSet<>();
    private TabularParser parser;

    private Function<Tuple, Tuple> dataTransformer;

    public TransformParser() {
    }

    public TransformParser(TabularParser parser, Function<Tuple, Tuple> dataTransformer) {
        this.dataTransformer = dataTransformer;
        this.parser = parser;
    }

    /**
     * Returns set of @Link Tuples (set of @Link Tuple) representing transformed data by @dataTransformer
     * @param data - read data from stream
     * @return - list of parsed tuples
     */

    @Override
    public List parse(byte[] data) {
        LinkedList<Tuple> r = new LinkedList<>();

        for (Tuple t: parser.parse(data)) {
            Tuple nt = dataTransformer.apply(t);
            if (nt != null) r.add(nt);
        }

        header = parser.getHeader();

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
