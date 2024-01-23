package cognitionmodel.models.inverted;

import cognitionmodel.datasets.parsers.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BitInvertedIndexTest {

    @Test
    public void getRecords() throws IOException {
        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.data")),
                        new CSVParser(",","\n")),
                (" INCOME,"+
                        " education-num," +
                        " marital-status," +
                        " capital-gain," +
                               " education," +
                               "age," +
                               " race," +
                               " sex," +
                               " race," +
                               " native-country," +
                               " workclass," +
                               " e-gov," +
                               " occupation," +
                        " capital-loss").split(",")
        );

        tabularModel.getInvertedIndex().setConfidenceIntervals(0.95);
        RoaringBitmap r = tabularModel.getInvertedIndex().getRecords(" capital-gain", 500.0);
/*        assertTrue(tabularModel.getInvertedIndex().getRecords(" capital-gain", 500).getCardinality() / tabularModel.getDataSet().size() == 0.04830932710911827);
        assertTrue(tabularModel.getInvertedIndex().getRecords(" education-num", 0).getCardinality() / tabularModel.getDataSet().size() == 0.05257823776911028);
        assertTrue(tabularModel.getInvertedIndex().getRecords(" education-num", 7).getCardinality() / tabularModel.getDataSet().size() == 0.049384232670986766);
         assertTrue(tabularModel.getInvertedIndex().getRecords(" education-num", 8).getCardinality() / tabularModel.getDataSet().size() == 0.049384232670986766);
         assertTrue(tabularModel.getInvertedIndex().getRecords(" education-num", 9).getCardinality() / tabularModel.getDataSet().size() == 0.32250238014802984);*/

        System.out.println((double) r.getCardinality() / tabularModel.getDataSet().size());



/*
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.test")),
                new CSVParser(",","\n"));

        tabularModel.predict(testDataSet.getRecords(), " INCOME", new Powerfunction(null, 10,1)).show(tabularModel.getDataSet().getFieldIndex(" INCOME"));

*/


    }
}