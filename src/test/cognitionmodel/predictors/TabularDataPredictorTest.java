package cognitionmodel.predictors;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.TabularModel;
import cognitionmodel.patterns.FullGridIterativePatterns;
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

        t = System.currentTimeMillis()-t;

        System.out.println(String.format("work time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))));

        System.out.println("memory usage: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024) + " Mb");



    }
}