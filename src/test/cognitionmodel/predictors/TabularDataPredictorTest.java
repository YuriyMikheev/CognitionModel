package cognitionmodel.predictors;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.TabularModel;
import cognitionmodel.patterns.FullGridIterativePatterns;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TabularDataPredictorTest {


    @Test
    public void testPredict() throws IOException {
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
                        new CSVParser(",","\n")), " INCOME" ,8 ,1.0);

        System.out.println(predictionResults.getHeader(tabularModel.getDataSet().getFieldIndex(" INCOME")).size());

        predictionResults.showInfo(tabularModel.getDataSet().getFieldIndex(" INCOME"));
    }
}