package cognitionmodel.models;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.InvertedBitTabularModel;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class InvertedTabularModelTest {

    @Test
    public void createTest() throws IOException {

        InvertedBitTabularModel tabularModel = new InvertedBitTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.data")),
                        new CSVParser(",","\n")),
                       (" INCOME,"+
                        " education-num," +
                        " marital-status," +
                               " capital-gain," +
/*                               " education," +
                               " age," +
                               " race," +
                               " sex," +*/
                        " capital-loss").split(","));


        tabularModel.make();


        System.out.println("Model initialized");

    }

}