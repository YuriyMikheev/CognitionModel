package cognitionmodel.datasets;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Provides CSV data support
 *
 */

public class TableDataSet extends DataSet{

    /**
     * Creates data set and reads table data from input stream.
     * Store parsed data from input stream.
     * The first line of table data stream should be a header.
     *
     * @param inputStream - data form input stream
     * @param parser - tabular parser object
     * @throws IOException
     */

    public TableDataSet(InputStream inputStream, TabularParser parser) throws IOException {
        super(inputStream, parser);
    }


    public Tuple getHeader() {
        return ((TabularParser)getParser()).getHeader();
    }

    public int getFieldIndex(String field){
        return getHeader().findFirstIndex(field);
    }

    public TupleElement get(int recordIndex, String field){
        return getRecords().get(recordIndex).get(getFieldIndex(field));
    }

}
