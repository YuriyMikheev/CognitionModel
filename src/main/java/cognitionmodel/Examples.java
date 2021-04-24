package cognitionmodel;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.TabularModel;
import cognitionmodel.patterns.FullGridIterativePatterns;
import cognitionmodel.patterns.FullGridRecursivePatterns;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.TabularDataPredictor;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Examples {


    public static void adult() throws IOException {
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

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.test")),
                new CSVParser(",","\n")), " INCOME" ,10 ,2.0);


        predictionResults.show(tabularModel.getDataSet().getFieldIndex(" INCOME"));
        tabularModel.close();
    }


    public static void census() throws IOException {
        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.data")),
                        new CSVParser(",","\n")),
        (" AHGA, AWKSTAT, CAPLOSS, TAXINC, CAPGAIN").split(","));

        tabularModel.setPatternSet(new FullGridIterativePatterns(tabularModel,3));

        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.test")),
                new CSVParser(",","\n")), " TAXINC" ,10 ,2.0);


        predictionResults.show(tabularModel.getDataSet().getFieldIndex(" TAXINC"));
        tabularModel.close();
    }


    public static void letters() throws IOException {
        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.train.csv")),
                        new CSVParser(";","\r\n")));

        tabularModel.setPatternSet(new FullGridRecursivePatterns(tabularModel,4));

        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
                new CSVParser(";","\r\n")), "lettr" ,1 ,1);


        predictionResults.show(tabularModel.getDataSet().getFieldIndex("lettr"));
        tabularModel.close();
    }

    public static void main(String[] args) throws IOException {

        long t = System.currentTimeMillis();

        adult();
      //  census();
       // letters();

        t = System.currentTimeMillis()-t;

        System.out.println(String.format("working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))));

        System.out.println("memory usage: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024) + " Mb");


    }
}
