package cognitionmodel.models.analyze.invertedmodel;

import cognitionmodel.datasets.parsers.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.decomposers.RecursiveDecomposer;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class FeaturesTest {

    @Test
    public void testAdult() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.data")),
                        new CSVParser(",","\n")),
     /*                  (" INCOME,"+
                        " education-num," +
                        " marital-status," +
                        " capital-gain," +
*//*                               " education," +
                               "age," +
                               " race," +
                               " sex," +
                               " native-country" +
                               " workclass" +
                               " e-gov" +
                               " occupation" +*//*
                        " capital-loss")*/
                ("age, workclass, education, education-num, marital-status, occupation, relationship, race, sex, capital-gain, capital-loss, hours-per-week, native-country, INCOME")

                .split(",")
        );


/*        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.test")),
                new CSVParser(",","\n"));*/

 //       tabularModel.predict(testDataSet.getRecords(), " INCOME", new Powerfunction(null, 5, 1), false, 5, a -> a.getMR() > 0 & a.getFr() > 5).show(tabularModel.getDataSet().getFieldIndex(" INCOME"));

        HashMap<String, Features.Err> ferr = Features.errorAgentsMap(tabularModel, (a, b) -> {return 1.0;}," INCOME",new RecursiveDecomposer(tabularModel.getInvertedIndex(), " INCOME", false, 4, a -> ((Agent)a).getMR() > 0), new Powerfunction(null, 5, 1));
        System.out.println("Features cumulative errors");

       // ferr.entrySet().stream().filter(e -> e.getValue().fe > 0).sorted(Comparator.comparing(e -> -e.getValue().fe/(e.getValue().fe+e.getValue().ft))).collect(toList()).forEach(e -> System.out.println(e.getKey()+"\t"+(double)e.getValue().fe/(e.getValue().fe+e.getValue().ft)));

        HashMap<String, Features.Err> aerr = new HashMap<>();
        for (Map.Entry<String, Features.Err> e: ferr.entrySet().stream().filter(e -> e.getValue().fe > 0).collect(Collectors.toList())){
            String s[] = e.getKey().substring(1, e.getKey().length() - 1).split(",");
            //double er = e.getValue().fe/(e.getValue().fe+e.getValue().ft);
            for (String as: s)
                aerr.compute(as.substring(0, as.indexOf(":")), (k,v) -> v!=null ? v.addErr(e.getValue().ft, e.getValue().fe) : new Features.Err(e.getValue().ft, e.getValue().fe));
        }

       // aerr.forEach((k,v) -> System.out.println(k + "\t" + v));
        aerr.entrySet().stream().sorted(Comparator.comparing(e -> -e.getValue().fe/(e.getValue().fe+e.getValue().ft))).collect(toList()).forEach(e -> System.out.println(e.getKey()+"\t"+(double)e.getValue().fe/(e.getValue().fe+e.getValue().ft)));

    }


}