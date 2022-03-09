package cognitionmodel.models;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.predictors.TabularDataPredictor;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

public class InvertedTabularModelTest {

    @Test
    public void createTest() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
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


        tabularModel.getAgents().get(35).addPoint(tabularModel.getAgents().get(36).relation.get(" capital-loss:1735"));
        tabularModel.getAgents().get(35).addPoint(tabularModel.getAgents().get(195).relation.get(" capital-gain:11678"));

        System.out.println("Model initialized");

    }

}