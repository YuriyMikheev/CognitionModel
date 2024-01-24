package cognitionmodel.models.inverted;

import cognitionmodel.datasets.*;
import cognitionmodel.datasets.parsers.ArffParser;
import cognitionmodel.datasets.parsers.CSVParser;
import cognitionmodel.models.inverted.index.BitInvertedIndex;
import cognitionmodel.models.inverted.index.StaticIntervaledBitInvertedIndex;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.predictionfunctions.PowerProbfunction;
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

//        tabularModel.predict(testDataSet.getRecords(), " INCOME", new Powerfunction(null, 10, 1), false, 4, a -> a.getMR() > 0).show(tabularModel.getDataSet().getFieldIndex(" INCOME"));
     //   tabularModel.predict(testDataSet.getRecords(), " INCOME", new Powerfunction(null, 11.2, 2), false, 4, a -> a.getMR() > 0).show(tabularModel.getDataSet().getFieldIndex(" INCOME"));
        tabularModel.predict1(testDataSet.getRecords(), " INCOME", new Powerfunction(null, 10, 1), false, 14, a -> a.getMR() > -10.01, null).show(tabularModel.getDataSet().getFieldIndex(" INCOME"));

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

        tabularModel.predict1(testDataSet.getRecords(), " TAXINC", new Powerfunction(null, 7,1), false, 5,  a -> a.getMR() > 0, null).show(tabularModel.getDataSet().getFieldIndex(" TAXINC"));

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

        //tabularModel.getInvertedIndex().setConfidenceIntervals(0.90);

        tabularModel.predict1(testDataSet.getRecords(), "lettr", null, false, 20, null, null).show(tabularModel.getDataSet().getFieldIndex("lettr"));

    }

    @Test
    public void createSpam() throws IOException {

        TableDataSet arrfDataSet = new TableDataSet(new FileInputStream(new File("E:\\Weka-3-8\\data\\KDDCup99_full.arff")),
                new ArffParser());//false, "duration", "src_bytes", "dst_bytes", "count", "srv_count"));



        TableDataSet[] dataSets = TableDataSet.split(arrfDataSet, 0.1, "label", 100);
        dataSets = TableDataSet.split(dataSets[1], 0.25, "label", 5);

 /*       HashMap<String, Integer> fr = new HashMap<>() , frs = new HashMap<>();
        int fi = arrfDataSet.getFieldIndex("label");

        for(Tuple t: dataSets[1]){
            fr.compute(t.get(fi).getValue().toString(), (k, v) -> (v == null) ? 1: v + 1);
        }

        for(Tuple t: dataSets[0]){
            frs.compute(t.get(fi).getValue().toString(), (k, v) -> (v == null) ? 1: v + 1);
        }

        for (Map.Entry<String, Integer> e: fr.entrySet())
            if (frs.containsKey(e.getKey()))
                System.out.println(e.getKey()+"\t"+e.getValue()+"\t"+frs.get(e.getKey())+"\t"+(1.0*e.getValue()/frs.get(e.getKey())));


*/
        InvertedTabularModel tabularModel = new InvertedTabularModel(dataSets[0]);

        tabularModel.predict(dataSets[1].getRecords(), "label", new Powerfunction(null, 0,1), false, 2, null, new double[]{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1}).show(tabularModel.getDataSet().getFieldIndex("label"));
    }


    @Test
    public void createDota2Test() throws IOException {

        TableDataSet arrfDataSet = new TableDataSet(new FileInputStream(new File("E:\\Weka-3-8\\data\\dota2.arff")),
                new ArffParser(false, "Cluster_ID","Game_mode","Game_type"));

        int game_mode = 8;

/*
        TableDataSet arrfDataSet1 = new TableDataSet(new FileInputStream(new File("E:\\Weka-3-8\\data\\dota2.arff")),
                new TransformParser(new ArffParser(false, "Cluster_ID"), t-> {
                    Tuple nt = new Tuple(); int j = 0;
                    if (t.get(0).getValue().toString().equals("-1"))
                        for (TupleElement te: t) {
                            if (j == 0 || j > 2)
                                if (te.getValue().toString().equals("-1")) nt.add(1);
                                    else
                                        if (te.getValue().toString().equals("1")) nt.add(-1);
                                            else
                                                nt.add(te);
                            else
                                nt.add(te);
                        j++;
                    }
                    else
                        nt = t;
                    return nt;
                }));
*//*        TableDataSet arrfDataSet2 = new TableDataSet(new FileInputStream(new File("E:\\Weka-3-8\\data\\dota2.arff")),
                new TransformParser(new ArffParser(false, "Cluster_ID"), t-> {
                    Tuple nt = new Tuple(); int j = 0;
                    if (t.get(0).getValue().toString().equals("1"))
                        for (TupleElement te: t) {
                            if (j == 0 || j > 2)
                                if (te.getValue().toString().equals("-1")) nt.add(1);
                                else
                                if (te.getValue().toString().equals("1")) nt.add(-1);
                                else
                                    nt.add(te);
                            else
                                nt.add(te);
                            j++;
                        }
                    else
                        nt = t;
                    return nt;

                }));

        arrfDataSet = TableDataSet.merge(arrfDataSet2, arrfDataSet1);*//*

        arrfDataSet = arrfDataSet1;*/

       // TableDataSet[] dataSets = TableDataSet.split(arrfDataSet, 0.5);// "Team_won", 1);
      //dataSets = TableDataSet.split(dataSets[1], 0.1, "Team_won", 1);

        TableDataSet[]  dataSets = TableDataSet.split(arrfDataSet, 0.05);//, "Team_won", 1);

        InvertedTabularModel tabularModel = new InvertedTabularModel(dataSets[0]);

        PredictionResults predictionResults = tabularModel.predict(dataSets[1].getRecords(), "Team_won", new PowerProbfunction(null, 1,1), false, 150, a->a.getRelation().values().stream().noneMatch(p->p.getValue().equals(0)), null);
        predictionResults.show(tabularModel.getDataSet().getFieldIndex("Team_won"));
      //  predictionResults.toCSVFile("dota2.predict.txt");
    }


    @Test
    public void featuresTestLetters() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.train.csv")),
                        new CSVParser(";","\r\n")));

        //TableDataSet testDataSet = TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
        //        new CSVParser(";","\r\n")));
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
                new CSVParser(";","\r\n"));

        tabularModel.predict1(testDataSet.getRecords(), "lettr", new Powerfunction(null, 0,1), false, 20, null, null).show(tabularModel.getDataSet().getFieldIndex("lettr"));

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

        tabularModel.predict1(testDataSet.getRecords(), "class", new Powerfunction(null, 0.01,0), false, 20, null, null).show(tabularModel.getDataSet().getFieldIndex("class"));

    }

    @Test
    public void createTestMush() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\mush\\mush.data")),
                        new CSVParser(",","\n")));


        TableDataSet testDataSet =  new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\mush\\mush.test")),
                new CSVParser(",","\n"));


        tabularModel.predict1(testDataSet.getRecords(), "class", null, false, 4,  a -> a.getMR() > 0.23, null).show(tabularModel.getDataSet().getFieldIndex("class"));

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
        tabularModel.setInvertedIndex(new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(),"Rings",5));

        PredictionResults pr = tabularModel.predict(testDataSet.getRecords(), "Rings", new Powerfunction(null, 2,3), false, 30, null/*a-> a.getMR()>0*/);
        pr.show(tabularModel.getDataSet().getFieldIndex("Rings"));
        tabularModel.regression(pr,"Rings").show(tabularModel.getDataSet().getFieldIndex("Rings"));

    }

    @Test
    public void createTestHouse() throws IOException {



        TableDataSet[] sets = TableDataSet.split(new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\house\\house_16H.arff")),
                        new ArffParser()), 0.1);

        InvertedTabularModel tabularModel = new InvertedTabularModel(sets[0]);


         //tabularModel.getInvertedIndex().setConfidenceIntervals(0.95);
        tabularModel.setInvertedIndex(new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex()," ",10));

        PredictionResults pr = tabularModel.predict(sets[1].getRecords(), "price", new Powerfunction(null, 0,1), false, 4, null/*a-> a.getMR()>0*/);
        pr.show(tabularModel.getDataSet().getFieldIndex("price"));
        tabularModel.regression(pr,"price").show(tabularModel.getDataSet().getFieldIndex("price"));

    }
}