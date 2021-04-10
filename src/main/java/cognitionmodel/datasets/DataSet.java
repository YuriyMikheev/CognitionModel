package cognitionmodel.datasets;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Set of data processing abstract class.
 * Reads data from stream and process it into relations. The relations reflects the structure fi dataset.
 * For example, relation is one whole line in table data, or relation is one image form collection of images.
 * This relations are not the model. It is just a storage of the input information.
 *
 *
 */

public abstract class DataSet implements Iterable<Tuple> {

    private InputStream inputStream;
    private ArrayList<Tuple> records = new ArrayList<>();
    private Parser parser;
    private HashMap<TupleElement, Integer> frequencies = new HashMap<>();

    /**
     * Creates data set and reads data from input stream.
     * Store parsed data from input stream.
     *
     * @param inputStream
     * @param parser
     * @throws IOException
     */

    public DataSet(InputStream inputStream, Parser parser) throws IOException {
        this.inputStream = inputStream;
        this.parser = parser;
        read();
    }

    private void read() throws IOException {
        records.addAll(parser.get(inputStream.readAllBytes()));

        for (Tuple t: records){
            for (TupleElement te: t.getTupleElements())
                if (frequencies.containsKey(te)) frequencies.put(te, frequencies.get(te) + 1);
                    else frequencies.put(te,1);
        }

    }

    public ArrayList<Tuple> getRecords(){
        return records;
    }

    @Override
    public Iterator<Tuple> iterator() {
        return records.iterator();
    }

    /**
     * Returns frequency of the @param tupleElement in the data set
     * @param tupleElement
     * @return - frequency
     */

    public int getFrequency(TupleElement tupleElement){
        Integer f = frequencies.get(tupleElement);
        return (f == null?0:f);
    }

    public double size() {
        return getRecords().size();
    }

    @Override
    public void forEach(Consumer<? super Tuple> action) {
        records.forEach(action);
    }

    @Override
    public Spliterator<Tuple> spliterator() {
        return records.spliterator();
    }
}
