package cognitionmodel.datasets;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

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

    /**
     * Randomly splits dataset to two parts according to percent
     * @param sourceData - split data set
     * @param percent - percent of second part
     * @return - array of two data sets
     * @throws IOException
     */
    public static TableDataSet[] split(TableDataSet sourceData, double percent) throws IOException {
        TableDataSet dataSet1 = new TableDataSet(null, (TabularParser) sourceData.getParser());
        TableDataSet dataSet2 = new TableDataSet(null, (TabularParser) sourceData.getParser());

        Random random = new Random();

        for (Tuple tuple: sourceData){
            if (dataSet1.size() >= sourceData.size() * (1 - percent))
                dataSet2.getRecords().add(tuple);
            else
                if (dataSet2.size() >= sourceData.size() * percent)
                    dataSet1.getRecords().add(tuple);
                else
                    if (random.nextDouble() < percent)
                        dataSet2.getRecords().add(tuple);
                    else
                        dataSet1.getRecords().add(tuple);
        }

        return new TableDataSet[]{dataSet1, dataSet2};
    }

}
