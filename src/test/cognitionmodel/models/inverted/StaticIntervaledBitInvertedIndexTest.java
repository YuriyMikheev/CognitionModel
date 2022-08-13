package cognitionmodel.models.inverted;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class StaticIntervaledBitInvertedIndexTest {

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


        StaticIntervaledBitInvertedIndex invertedIndex = new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(), "class", 10);

        assertTrue(invertedIndex.invertedIndex.entrySet().stream().filter(e->{
            //System.out.println(e.getKey() + "\t"+e.getValue().size());
            return e.getValue().size() > 10;
        }).collect(Collectors.toList()).size() == 0);


    }

    @Test
    public void testofintervalsprocentile() throws IOException {

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


        StaticIntervaledBitInvertedIndex invertedIndex = new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(), "class", 0.1);

        assertTrue(invertedIndex.invertedIndex.entrySet().stream().filter(e->{
            //System.out.println(e.getKey() + "\t"+e.getValue().size());
            return e.getValue().size() > 10;
        }).collect(Collectors.toList()).size() == 0);


    }
    @Test
    public void testofintervalsres() throws IOException {

        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\segment\\segment.test")),
                        new CSVParser("\t","\r\n")),
                (
/*                        "region-centroid-col," +
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
                                "class"*/
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

//region-centroid-col	region-centroid-row	region-pixel-count	short-line-density-5	short-line-density-2	vedge-mean	vegde-sd	hedge-mean	hedge-sd	intensity-mean	rawred-mean	rawblue-mean	rawgreen-mean	exred-mean	exblue-mean	exgreen-mean	value-mean	saturatoin-mean	hue-mean	class
//        StaticIntervaledBitInvertedIndex invertedIndex = new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(), "class", 35);
        StaticIntervaledBitInvertedIndex invertedIndex = new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(), "class", 1.0/35);
//        StaticIntervaledBitInvertedIndex invertedIndex = new StaticIntervaledBitInvertedIndex((BitInvertedIndex) tabularModel.getInvertedIndex(), "class", new double[]{0.02, 0.14, 0.34, 0.34, 0.14, 0.02});

        tabularModel.setInvertedIndex(invertedIndex);
/*
        assertTrue(invertedIndex.invertedIndex.entrySet().stream().filter(e->{
            //System.out.println(e.getKey() + "\t"+e.getValue().size());
            return e.getValue().size() > 10;
        }).collect(Collectors.toList()).size() == 0);
*/
        TableDataSet testDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\segment\\segment.train")),
                new CSVParser("\t","\r\n"));


        tabularModel.predict(testDataSet.getRecords(), "class", new Powerfunction(null, 0.01,0), false, 13, null).show(tabularModel.getDataSet().getFieldIndex("class"));


    }

}