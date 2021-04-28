package cognitionmodel.datasets;

import java.util.List;

/**
 * Interface to parser classes. Defines the transformation data function from stream to inner representation.
 */


public interface TabularParser extends Parser{


    public List<Tuple> get(byte[] data);

    /**
     * Gets array of terminals indices for the field associated with @param fieldIndex
     *
     * @param fieldIndex - field index
     * @return - array of indices
     */

    public String[] terminals(int fieldIndex);

    public Tuple getHeader();

}
