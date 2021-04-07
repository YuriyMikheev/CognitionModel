package cognitionmodel.datasets;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides CSV data support
 *
 */

public class CSVDataSet extends DataSet{


    private Tuples header;

    /**
     * Creates data set and reads CSV data from input stream.
     * Store parsed data from input stream.
     * The first line of CSV data stream should be a header.
     *
     * @param inputStream
     * @param parser
     * @throws IOException
     */
    public CSVDataSet(InputStream inputStream, Parser parser) throws IOException {
        super(inputStream, parser);

        header = getRecords().get(0);
        getRecords().remove(0);

    }

    public Tuples getHeader() {
        return header;
    }
}
