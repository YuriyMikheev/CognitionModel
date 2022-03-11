package cognitionmodel.models;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class InvertedTabularAgentTest {

    @Test
    public void createTestAdult() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.data")),
                        new CSVParser(",","\n")),
                       (" INCOME,"+
                        " education-num," +
                        " marital-status," +
                               " capital-gain," +
/*                               " education," +
                               "age," +
                               " race," +
                               " sex," +*/
                        " capital-loss").split(","));


      //  tabularModel.make();
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.test")),
                new CSVParser(",","\n"));

        tabularModel.predict(testDataSet.getRecords(), " INCOME", new Powerfunction(null, 10,1)).show(tabularModel.getDataSet().getFieldIndex(" INCOME"));

/*        for (InvertedTabularModel.Agent a: tabularModel.getAgents().stream().sorted((a1, a2) -> (a1.getZ() > a2.getZ())?1:-1).collect(Collectors.toList()))
            System.out.println(a);*/


      //  tabularModel.getAgents().get(35).addPoint(tabularModel.getAgents().get(36).relation.get(" capital-loss:1735"));
      //  tabularModel.getAgents().get(35).addPoint(tabularModel.getAgents().get(195).relation.get(" capital-gain:11678"));

        System.out.println("Model initialized");

    }


    @Test
    public void createTestCensus() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.data")),
                        new CSVParser(",","\n")),
                (" AHGA, AWKSTAT, CAPLOSS, TAXINC, CAPGAIN").split(","));


        //  tabularModel.make();
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.test")),
                new CSVParser(",","\n"));

        tabularModel.predict(testDataSet.getRecords(), " TAXINC", new Powerfunction(null, 7,1)).show(tabularModel.getDataSet().getFieldIndex(" TAXINC"));

/*        for (InvertedTabularModel.Agent a: tabularModel.getAgents().stream().sorted((a1, a2) -> (a1.getZ() > a2.getZ())?1:-1).collect(Collectors.toList()))
            System.out.println(a);*/


        //  tabularModel.getAgents().get(35).addPoint(tabularModel.getAgents().get(36).relation.get(" capital-loss:1735"));
        //  tabularModel.getAgents().get(35).addPoint(tabularModel.getAgents().get(195).relation.get(" capital-gain:11678"));


    }


    @Test
    public void createTestLetters() throws IOException {

        InvertedTabularModel  tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.train.csv")),
                        new CSVParser(";","\r\n")));


        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
                new CSVParser(";","\r\n"));


        tabularModel.predict(testDataSet.getRecords(), "lettr", new Powerfunction(null, 0,1)).show(tabularModel.getDataSet().getFieldIndex("lettr"));

    }

}