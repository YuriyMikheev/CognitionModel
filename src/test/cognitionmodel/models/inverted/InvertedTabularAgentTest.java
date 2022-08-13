package cognitionmodel.models.inverted;

import cognitionmodel.datasets.ArffParser;
import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.TabularDataPredictor;
import cognitionmodel.predictors.predictionfunctions.LogPowerfunction;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

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
                               " sex," +
                               " native-country" +
                               " workclass" +
                               " e-gov" +
                               " occupation" +*/
                        " capital-loss")
            //    ("age, workclass, education, education-num, marital-status, occupation, relationship, race, sex, capital-gain, capital-loss, hours-per-week, native-country, INCOME")
/*                (" INCOME,"+
                        " native-country," +
                        " marital-status," +
                        " capital-loss," +
                        " capital-gain," +
                        " workclass")*/
                .split(",")
        );

       // tabularModel.getInvertedIndex().setConfidenceIntervals(0.95);
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.test")),
                new CSVParser(",","\n"));

        tabularModel.predict(testDataSet.getRecords(), " INCOME", new Powerfunction(null, 10, 1), false, 4, a -> a.getMR() > 0).show(tabularModel.getDataSet().getFieldIndex(" INCOME"));

    }


    @Test
    public void createTestCensus() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.data")),
                        new CSVParser(",","\n")),
                (" AHGA, AWKSTAT, CAPLOSS, TAXINC, CAPGAIN")
//                ("AAGE, ACLSWKR, ADTIND, ADTOCC, AGI, AHRSPAY, AHSCOL, AMJIND, AMJOCC, ARACE, AREORGN, ASEX, AUNMEM, AUNTYPE, DIVVAL, FEDTAX, FILESTAT, GRINREG, GRINST, HHDFMX, HHDREL, MARSUPWT, MIGMTR1, MIGMTR3, MIGMTR4, MIGSAME, MIGSUN, NOEMP, PARENT, PEARNVAL, PEFNTVTY, PEMNTVTY, PENATVTY, PRCITSHP, PTOTVAL, SEOTR, VETQVA, VETYN, WKSWORK, AMARITL, TAXINC")
 //      (" ACLSWKR, ADTOCC, AGI, AHRSPAY, AHSCOL, AMJIND, AMJOCC, ARACE, AREORGN, ASEX, AUNMEM, AUNTYPE, DIVVAL, FEDTAX, FILESTAT, GRINREG, GRINST, HHDREL, MARSUPWT, MIGMTR1, MIGMTR3, MIGMTR4, MIGSUN, NOEMP, PARENT, PEARNVAL, PEFNTVTY, PEMNTVTY, PENATVTY, PRCITSHP, AMARITL, TAXINC")
                        .split(","));


        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.test")),
                new CSVParser(",","\n"));

        tabularModel.predict(testDataSet.getRecords(), " TAXINC", new Powerfunction(null, 7,1), false, 4,  a -> a.getMR() > 0).show(tabularModel.getDataSet().getFieldIndex(" TAXINC"));

    }


    @Test
    public void createTestLetters() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.train.csv")),
                        new CSVParser(";","\r\n")));


        //TableDataSet testDataSet = TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
        //        new CSVParser(";","\r\n")));
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
                new CSVParser(";","\r\n"));

        tabularModel.getInvertedIndex().setConfidenceIntervals(0.90);

        tabularModel.predict(testDataSet.getRecords(), "lettr", new Powerfunction(null, 0,1), false, 5, null).show(tabularModel.getDataSet().getFieldIndex("lettr"));

    }

    @Test
    public void featuresTestLetters() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.train.csv")),
                        new CSVParser(";","\r\n")));

        //TableDataSet testDataSet = TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
        //        new CSVParser(";","\r\n")));
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.train.csv")),
                new CSVParser(";","\r\n"));

        tabularModel.predict(testDataSet.getRecords(), "lettr", new Powerfunction(null, 0,1), false, 7, null).show(tabularModel.getDataSet().getFieldIndex("lettr"));

    }


    @Test
    public void createTestSegment() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\segment\\segment.test")),
                        new CSVParser("\t","\r\n")),
                (
                        "region-centroid-col\n" +
                                "region-centroid-row\n" +
                                "intensity-mean\n" +
                                "rawred-mean\n" +
                                "rawblue-mean\n" +
                                "rawgreen-mean\n" +
                                "exred-mean\n" +
                                "exblue-mean\n" +
                                "exgreen-mean\n" +
                                "value-mean\n" +
                                "saturatoin-mean\n" +
                                "hue-mean\n" +
                                "class"

                )
                        .split("\n")

        );


        //TableDataSet testDataSet = TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
        //        new CSVParser(";","\r\n")));
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\segment\\segment.train")),
                new CSVParser("\t","\r\n"));

      //  tabularModel.getInvertedIndex().setConfidenceIntervals(0.92);
        tabularModel.setInvertedIndex(new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(),"class",35));

        tabularModel.predict(testDataSet.getRecords(), "class", new Powerfunction(null, 0.01,0), false, 20, null).show(tabularModel.getDataSet().getFieldIndex("class"));

    }

    @Test
    public void createTestMush() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\mush\\mush.data")),
                        new CSVParser(",","\n")));


        TableDataSet testDataSet =  new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\mush\\mush.test")),
                new CSVParser(",","\n"));


        tabularModel.predict(testDataSet.getRecords(), "class", new Powerfunction(null, 0,1), false, 4,  a -> a.getMR() > 0).show(tabularModel.getDataSet().getFieldIndex("class"));

    }

    @Test
    public void createTestAbalon() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\abalone\\abalone.data")),
                        new CSVParser(",","\n"))

        );


        //TableDataSet testDataSet = TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
        //        new CSVParser(";","\r\n")));
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\abalone\\abalone.test")),
                new CSVParser(",","\n"));

        // tabularModel.getInvertedIndex().setConfidenceIntervals(0.88);
       // tabularModel.setInvertedIndex(new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(),"Rings",40));

        PredictionResults pr = tabularModel.predict(testDataSet.getRecords(), "Rings", new LogPowerfunction(null, 0.1,0), false, 30, null/*a-> a.getMR()>0*/);
        pr.show(tabularModel.getDataSet().getFieldIndex("Rings"));
        tabularModel.regression(pr,"Rings").show(tabularModel.getDataSet().getFieldIndex("Rings"));

    }

    @Test
    public void createTestHouse() throws IOException {



        TableDataSet[] sets = TableDataSet.split(new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\house\\house_16H.arff")),
                        new ArffParser()), 0.1);

        InvertedTabularModel tabularModel = new InvertedTabularModel(sets[0]);


         tabularModel.getInvertedIndex().setConfidenceIntervals(0.95);
        //tabularModel.setInvertedIndex(new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex()," ",10));

        PredictionResults pr = tabularModel.predict(sets[1].getRecords(), "price", new Powerfunction(null, 0,1), false, 2, null/*a-> a.getMR()>0*/);
        pr.show(tabularModel.getDataSet().getFieldIndex("price"));
        tabularModel.regression(pr,"price").show(tabularModel.getDataSet().getFieldIndex("price"));

    }
}