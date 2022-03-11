package cognitionmodel;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.ImageNoizyCSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.TabularModel;
import cognitionmodel.models.analyze.Entropy;
import cognitionmodel.models.relations.ImageLightRelation;
import cognitionmodel.patterns.FullGridIterativePatterns;
import cognitionmodel.patterns.ImageRecursivePatterns;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Experiments {

    public static void adultEntropy() throws IOException {

        TableDataSet tableDataSet = new TableDataSet(new FileInputStream("D:\\works\\Data\\adult\\adult.data"),
                new CSVParser(",", "\n"));


        System.out.println("Records entropy " + Entropy.recordsEntropy(tableDataSet));
        System.out.println("Fields entropy " + Entropy.fieldsEntropy(tableDataSet));

        TabularModel tabularModel = new TabularModel(tableDataSet,
                (" education-num," +
                        " marital-status," +
                        " capital-gain," +
                        " capital-loss,"+
                        " INCOME").split(","));

        tabularModel.setPatternSet(new FullGridIterativePatterns(tabularModel,3));

        tabularModel.make();
        System.out.println("Model entropy "+ Entropy.modelEntropy(tabularModel));

    }

    public static void censusEntropy() throws IOException {
        TableDataSet tableDataSet = new TableDataSet(new FileInputStream("D:\\works\\Data\\Census\\census-income.data"),
                new CSVParser(",", "\n"));


        System.out.println("Records entropy " + Entropy.recordsEntropy(tableDataSet));
        System.out.println("Fields entropy " + Entropy.fieldsEntropy(tableDataSet));

        TabularModel tabularModel = new TabularModel(tableDataSet,
                (" AHGA, AWKSTAT, CAPLOSS, TAXINC, CAPGAIN").split(","));

        tabularModel.setPatternSet(new FullGridIterativePatterns(tabularModel,5));

        tabularModel.make();
        System.out.println("Model entropy "+ Entropy.modelEntropy(tabularModel));
    }

    public static void mnistEntropy() throws IOException {

        TableDataSet tableDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\EMNIST\\emnist-mnist-train.csv")),
                new ImageNoizyCSVParser(",", "\n", new int[]{0, 4, 100, 200, 3000}, 0, 0.0, "100"));


        System.out.println("Records entropy " + Entropy.recordsEntropy(tableDataSet));
        System.out.println("Fields entropy " + Entropy.fieldsEntropy(tableDataSet));

        TabularModel tabularModel = new TabularModel(
                tableDataSet,
                new ImageLightRelation(0));

        tabularModel.setPatternSet(new ImageRecursivePatterns(0, 28,28, 750, new int[]{-5,-3, -2, -1, 1, 2, 3,5}, new int[]{2, 3} ));

        tabularModel.make();

        System.out.println("Model entropy "+ Entropy.imagesModelEntropy(tabularModel));
    }

    public static void main(String[] args) throws IOException {
        adultEntropy();
        //censusEntropy();
        //mnistEntropy();
    }

}
