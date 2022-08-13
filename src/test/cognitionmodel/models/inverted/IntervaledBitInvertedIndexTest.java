package cognitionmodel.models.inverted;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class IntervaledBitInvertedIndexTest {

    @Test
    public void testofintervals() throws IOException {

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

        tabularModel.getInvertedIndex().setConfidenceIntervals(0.95);

        DynamicIntervaledBitInvertedIndex invertedIndex = new DynamicIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(), tabularModel.getDataSet().getRecords().get(13), " INCOME");

        invertedIndex.invertedIndex.entrySet().forEach(e->{
            System.out.println(e.getKey() + "\t"+(1 - (double)e.getValue().values().stream().findFirst().orElseThrow().getCardinality()/invertedIndex.getDataSetSize()));
        });

        Arrays.stream(invertedIndex.confidenceLevels).forEach(System.out::println);

    }

    @Test
    public void testofintervals1() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\segment\\segment.test")),
                        new CSVParser("\t","\r\n")),
                (
                        "region-centroid-col," +
                                "region-centroid-row," +
                                "short-line-density-5," +
                                "hedge-sd," +
                                "intensity-mean," +
                                "rawred-mean," +
                                "rawblue-mean," +
                                "rawgreen-mean," +
                                "exred-mean," +
                                "exblue-mean," +
                                "exgreen-mean," +
                                "value-mean," +
                                "saturatoin-mean," +
                                "hue-mean," +
                                "class"
                )
                        .split(",")

        );

        tabularModel.getInvertedIndex().setConfidenceIntervals(0.95);

        DynamicIntervaledBitInvertedIndex invertedIndex = new DynamicIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(), tabularModel.getDataSet().getRecords().get(13), "class");

        invertedIndex.invertedIndex.entrySet().forEach(e->{
            System.out.println(e.getKey() + "\t"+(1 - (double)e.getValue().values().stream().findFirst().orElseThrow().getCardinality()/invertedIndex.getDataSetSize()));
        });

        Arrays.stream(invertedIndex.confidenceLevels).forEach(System.out::println);

    }

}