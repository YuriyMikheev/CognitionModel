package cognitionmodel.datasets;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.Math.floor;

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


    /**
     * Randomly splits dataset to two parts according to percent
     * @param sourceData - split data set
     * @param percent - percent of second part
     * @return - array of two data sets
     * @throws IOException
     */
    public static TableDataSet[] split(TableDataSet sourceData, double percent, int balancingFieldIndex, int minAmount) throws IOException {

        HashMap<String, ArrayList<Integer>> fr = new HashMap<>();//, frs = new HashMap<>();

        int i = 0;
        for(Tuple t: sourceData){
            ArrayList<Integer> idx = fr.get(t.get(balancingFieldIndex).getValue().toString());
            if (idx == null)
               fr.put(t.get(balancingFieldIndex).getValue().toString(), idx = new ArrayList<>());
            idx.add(i++);
        }
        TableDataSet dataSet1 = new TableDataSet(null, (TabularParser) sourceData.getParser());
        TableDataSet dataSet2 = new TableDataSet(null, (TabularParser) sourceData.getParser());

        Random random = new Random();

        for (Map.Entry<String, ArrayList<Integer>> e: fr.entrySet()){
            for (Integer idx: e.getValue()) {
                if (random.nextDouble() < percent || e.getValue().size() < minAmount)
                    dataSet2.getRecords().add(sourceData.getRecords().get(idx));
                else
                    dataSet1.getRecords().add(sourceData.getRecords().get(idx));
            }
        }

        return new TableDataSet[]{dataSet1, dataSet2};
    }

    /**
     * Randomly splits dataset to two parts according to percent
     * @param sourceData - split data set
     * @param percent - percent of second part
     * @param balancingField - field for balancing samples, each value of the field represented in proportion fitted original distribution
     * @return - array of two data sets
     * @throws IOException
     */
    public static TableDataSet[] split(TableDataSet sourceData, double percent, String balancingField, int minAmount) throws IOException {
        return split(sourceData, percent, sourceData.getFieldIndex(balancingField), minAmount);

    }

    public static TableDataSet merge(TableDataSet dataSet1, TableDataSet dataSet2) throws IOException {
        TableDataSet rdataSet = new TableDataSet(null, (TabularParser) dataSet1.getParser());

        if (!dataSet1.getHeader().toCSVString().equals(dataSet2.getHeader().toCSVString()))
            System.err.println("Merging datasets have headers are different");

        for (Tuple t: dataSet1)
            rdataSet.getRecords().add(t);

        for (Tuple t: dataSet2)
            rdataSet.getRecords().add(t);

        return rdataSet;
    }


}

