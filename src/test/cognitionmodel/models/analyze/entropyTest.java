package cognitionmodel.models.analyze;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.TabularModel;
import cognitionmodel.patterns.FullGridIterativePatterns;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

public class entropyTest {


    @Test
    public void  recordsEntropyTest() throws IOException {

        TableDataSet tableDataSet = new TableDataSet(new FileInputStream("D:\\works\\Data\\Test\\entropyTest.data"),
                        new CSVParser("\t", "\n"));


        //System.out.println("Records entropy  " + entropy.recordsEntropy(tableDataSet));
        assert(3.367295829986472 == entropy.recordsEntropy(tableDataSet));
    }

    @Test
    public void  fieldsEntropyTest() throws IOException {

        TableDataSet tableDataSet = new TableDataSet(new FileInputStream("D:\\works\\Data\\Test\\entropyTest.data"),
                new CSVParser("\t", "\n"));


        //System.out.println("Fields entropy "+entropy.fieldsEntropy(tableDataSet));
        assert(10.840008158358575 == entropy.fieldsEntropy(tableDataSet));

    }

    @Test
    public void  modelEntropyTest() throws IOException {

        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.data")),
                        new CSVParser(",","\n")),
                (" education-num," +
                        " marital-status," +
                        " capital-gain," +
                        " capital-loss,"+
                        " INCOME").split(","));

        tabularModel.setPatternSet(new FullGridIterativePatterns(tabularModel,3));

        tabularModel.make();

        System.out.println("Model entropy "+entropy.modelEntropy(tabularModel));
       // assert(10.840008158358575 == entropy.fieldsEntropy(tableDataSet));

    }

}