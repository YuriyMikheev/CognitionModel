package cognitionmodel.datasets.parsers;

import cognitionmodel.datasets.Tuple;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Interface to parser classes. Defines the transformation data function from stream to inner representation.
 */


public interface Parser {

    public List<Tuple> parse(byte[] data);
    public List<Tuple> parse(InputStream inputStream) throws IOException;



}
