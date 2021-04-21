package cognitionmodel.datasets;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Provides CSV data support
 *
 */

public class TableDataSet extends DataSet{

    private Tuple header;

    /**
     * Creates data set and reads table data from input stream.
     * Store parsed data from input stream.
     * The first line of table data stream should be a header.
     *
     * @param inputStream
     * @param parser
     * @throws IOException
     */

    public TableDataSet(InputStream inputStream, Parser parser) throws IOException {
        super(inputStream, parser);

        header = getRecords().get(0);
        getRecords().remove(0);


    }

    public Tuple getHeader() {
        return header;
    }
}
