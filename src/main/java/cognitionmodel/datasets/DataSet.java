package cognitionmodel.datasets;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Set of data processing abstract class.
 * Reads data from stream and process it into relations. The relations reflects the structure fi dataset.
 * For example, relation is one whole line in table data, or relation is one image form collection of images.
 * This relations are not the model. It is just a storage of the input information.
 *
 *
 */

public abstract class DataSet implements Iterable<Tuples> {

    private InputStream inputStream;
    private ArrayList<Tuples> records = new ArrayList<>();
    private Parser parser;

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

    }

    public ArrayList<Tuples> getRecords(){
        return records;
    }

    @Override
    public Iterator<Tuples> iterator() {
        return records.iterator();
    }
}
