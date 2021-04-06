package main.cognitionmodel.datasets;

import java.util.List;

/**
 * Interface to parser classes. Defines the transformation data function from stream to inner representation.
 */


public interface Parser {

    public List<Tuple> get(byte[] data);
}
