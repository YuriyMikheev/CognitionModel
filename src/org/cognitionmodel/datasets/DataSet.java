package org.cognitionmodel.datasets;

import org.cognitionmodel.Relation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Set of data processing abstract class.
 * Reads data from stream and process it into relations. The relations reflects the structure fi dataset.
 * For example, relation is one whole line in table data, or relation is one image form collection of images.
 * This relations are not the model. It is just a storage of the input information.
 *
 *
 */

public abstract class DataSet implements Iterable<Relation> {

    private InputStream inputStream;
    private ArrayList<Relation> records = new ArrayList<>();
    private Parser parser;

    public DataSet(InputStream inputStream, Parser parser) throws IOException {
        this.inputStream = inputStream;
        this.parser = parser;
        read();
    }

    private void read() throws IOException {
        records.addAll(parser.get(inputStream.readAllBytes()));

    }

    @Override
    public Iterator<Relation> iterator() {
        return records.iterator();
    }
}
