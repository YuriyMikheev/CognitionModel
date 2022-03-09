package cognitionmodel.predictors;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.models.TabularModel;
import cognitionmodel.patterns.FullGridIterativePatterns;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TabularDataPredictorTest {


    @Test
    public void testPredict() throws IOException {

        long t = System.currentTimeMillis();


        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.data")),
                        new CSVParser(",","\n")),new LightRelation(),
                (" INCOME,"+
                        " education-num," +
                        " marital-status," +
                        " capital-gain," +
                        " education," +
                        "age," +
                        " race," +
                        " sex," +
                        " capital-loss").split(","));

        tabularModel.setPatternSet(new FullGridIterativePatterns(tabularModel,3));

        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.test")),
                        new CSVParser(",","\n"))), " INCOME" ,new Powerfunction(tabularModel, 10 ,2));


        predictionResults.show(tabularModel.getDataSet().getFieldIndex(" INCOME"));

        t = System.currentTimeMillis()-t;

        System.out.println(String.format("work time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))));

        System.out.println("memory usage: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024) + " Mb");



    }
}