package cognitionmodel.models;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.patterns.FullGridRecursivePatterns;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TabularModelTest {

    @Test
    public void testModel() throws IOException {

        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.data")),
                        new CSVParser(",","\n")),
                        (" education-num," +
                     " marital-status," +
                      " capital-gain," +
                     " capital-loss").split(","));

        tabularModel.setPatternSet(new FullGridRecursivePatterns(tabularModel,4));

        tabularModel.make();

        System.out.println(tabularModel.frequencyMap.size());


    }

}