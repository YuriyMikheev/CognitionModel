package cognitionmodel;

import cognitionmodel.datasets.*;
import cognitionmodel.datasets.parsers.CSVParser;
import cognitionmodel.datasets.parsers.ImageCSVParser;
import cognitionmodel.datasets.parsers.ImageNoizyCSVParser;
import cognitionmodel.models.relations.ImageLightRelation;
import cognitionmodel.models.TabularModel;
import cognitionmodel.patterns.*;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.TabularDataPredictor;
import cognitionmodel.predictors.predictionfunctions.Imagefunction;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.round;


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

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel, TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\adult\\adult.test")),
                new CSVParser(",","\n"))), " INCOME" ,new Powerfunction(tabularModel,10 ,2));

        predictionResults.show(tabularModel.getDataSet().getFieldIndex(" INCOME"));
        tabularModel.close();
    }


    public static void census() throws IOException {
        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.data")),
                        new CSVParser(",","\n")),
                            (" AHGA, AWKSTAT, CAPLOSS, TAXINC, CAPGAIN").split(",")
        );

        tabularModel.setPatternSet(new FullGridIterativePatterns(tabularModel,3));

        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.test")),
                new CSVParser(",","\n"))), " TAXINC" , new Powerfunction(tabularModel, 10 ,2.0));

        predictionResults.show(tabularModel.getDataSet().getFieldIndex(" TAXINC"));
        tabularModel.close();
    }


    public static void letters() throws IOException {
        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.train.csv")),
                        new CSVParser(";","\r\n")));

        tabularModel.setPatternSet(new FullGridRecursivePatterns(tabularModel,5));

        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,TabularDataPredictor.fit2model(tabularModel, new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\letter\\letter-recognition.data.test.csv")),
                new CSVParser(";","\r\n"))), "lettr" ,new Powerfunction(tabularModel, 0 ,1));

        predictionResults.show(tabularModel.getDataSet().getFieldIndex("lettr"));
        tabularModel.close();
    }

    public static void mnist() throws IOException {
        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\EMNIST\\emnist-mnist-train.csv")),
                        new ImageNoizyCSVParser(",", "\n", new int[]{0, 4, 3000}, 0, 0.0, "100")),
                new ImageLightRelation(0));

       tabularModel.setPatternSet(new ImageRecursivePatterns(0, 28,28, 750, new int[]{-5,-3, -2, -1, 1, 2, 3,5}, new int[]{2, 3} ));
      //  tabularModel.setPatternSet(new ImageCellularPatterns(0, 28*28,700, new int[]{75, 35, 12}));
      //  tabularModel.setPatternSet(new ImageRandomPatterns(0, 28*28, 10, 10));

        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\EMNIST\\emnist-mnist-test.csv")),
                new ImageCSVParser(",", "\n", 0).setIntervals(new int[]{0, 4, 3000})), "label" ,
                new Imagefunction(tabularModel));

        predictionResults.show(tabularModel.getDataSet().getFieldIndex("label"));
        tabularModel.close();
    }

    public static void mnistletters() throws IOException {
        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\EMNIST\\emnist-balanced-train.csv")),
                        new ImageCSVParser(",", "\n", 0).setIntervals(new int[]{0, 4, 3000})), new ImageLightRelation(0));

       // tabularModel.setPatternSet(new ImageRecursivePatterns(0, 28,28, 50, new int[]{-3, -2, -1, 1, 2, 3}, new int[]{2,3} ));
        tabularModel.setPatternSet(new ImageCellularPatterns(0, 28*28,200, new int[]{75, 35, 12}));
        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\EMNIST\\emnist-balanced-test.csv")),
                new ImageCSVParser(",", "\n", 0).setIntervals(new int[]{0, 4, 3000})), "label" , new Imagefunction(tabularModel));

        predictionResults.show(tabularModel.getDataSet().getFieldIndex("label"));
        tabularModel.close();
    }

    private static String cifartransfer(String a){
        String r = "";
        String[] v = a.split("r|g|b");

        if (v.length != 4) return "";
        try {
            int[] c = new int[4];
            for (int i = 1; i < 4; i++)
                c[i] = Integer.parseInt(v[i]);

            double rv = Arrays.stream(c).average().getAsDouble();

            int intervalsamount = 10;

            r = ""+ round(intervalsamount*rv/256); //(rv > 200 ? 3 : rv > 100 ? 2 : rv > 50 ? 1 : 0);

/*
            r = "r" + (Integer.parseInt(v[1]) > 200 ? 3 : Integer.parseInt(v[1]) > 100 ? 2 : Integer.parseInt(v[1]) > 50 ? 1 : 0);
            r = r + "g" + (Integer.parseInt(v[2]) > 200 ? 3 : Integer.parseInt(v[2]) > 100 ? 2 : Integer.parseInt(v[2]) > 50 ? 1 : 0);
            r = r + "b" + (Integer.parseInt(v[3]) > 200 ? 3 : Integer.parseInt(v[3]) > 100 ? 2 : Integer.parseInt(v[3]) > 50 ? 1 : 0);;
*/
        } catch (NumberFormatException e){
            System.err.println(v+" is not transferabale");
        }
        return r;
    }


    private static Tuple cifartupletransfer(Tuple tuple){
        Tuple t = new Tuple();

        int[] intervals = new int[]{0, 4, 25, 50, 100, 150, 200, 250, 300};
        String[] tv = new String[1024*3];

        int i = 0;
        for (TupleElement tupleElement: tuple)
          if (i++ != 0) {
            String[] v = tupleElement.getValue().toString().split("r|g|b");

            if (v.length != 4)
                t.add(tupleElement);
            else
                try {
                    tv[i - 2] = ImageCSVParser.pixelfilter(v[1], intervals);
                    tv[i - 2 + 1024] = ImageCSVParser.pixelfilter(v[2], intervals);
                    tv[i - 2 + 2*1024] = ImageCSVParser.pixelfilter(v[3], intervals);

/*                    t.add("r" + ImageCSVParser.pixelfilter(v[1], intervals))
                     .add("g" + ImageCSVParser.pixelfilter(v[2], intervals))
                     .add("b" + ImageCSVParser.pixelfilter(v[3], intervals));*/
                }catch (NumberFormatException e){
                    t.add(tupleElement);
                }
        } else t.add(tupleElement);

        t.addAll(tv);

        return t;
    }


    public static void cifar() throws IOException {
        TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\CIFAR\\bin\\train.csv")),
                        new ImageCSVParser(",", "\r\n", 0).setTupleTransferFunction(Examples::cifartupletransfer)), new ImageLightRelation(0));

         tabularModel.setPatternSet(new ImageRecursivePatterns(0, 32,32*3, 50, new int[]{-3, -2, -1, 1, 2, 3}, new int[]{2,3} ));
      //  tabularModel.setPatternSet(new ImageCellularPatterns(0, 32*32*3,1000, new int[]{75, 35, 12}));
        tabularModel.make();

        PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\CIFAR\\bin\\test.csv")),
                new ImageCSVParser(",", "\r\n", 0).setTupleTransferFunction(Examples::cifartupletransfer)), "label" , new Imagefunction(tabularModel));

        predictionResults.show(tabularModel.getDataSet().getFieldIndex("label"));
        tabularModel.close();
    }

    public static void rz() throws IOException {
          TabularModel tabularModel = new TabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\R-Z\\R-Z 16.05 4.csv")),
                        new CSVParser(",","\r\n")),
                ("кп,кп1").split(","));


        tabularModel.setPatternSet(new FullGridIterativePatterns(tabularModel,2));
        tabularModel.make();

        Iterator<Map.Entry<int[], Integer>> it = tabularModel.relationIterator();

        double sz = 1;
        int n = 0;
        while (it.hasNext()) {
            int[] k = it.next().getKey();
            double dz = tabularModel.getMRd(k);//* tabularModel.getFrequency(k)/tabularModel.getDataSet().size();
            sz += dz;
            System.out.println(sz+"\t"+dz);
            n++;
        }

/*
        int si = 1, i =0;
        for (byte e: tabularModel.getEnabledFields()) {
            if (e == 1)
                si *= tabularModel.termsByField(i).length;
            i++;
        }
*/


        System.out.println("Sum of Z = "+sz);

    /*    PredictionResults predictionResults = TabularDataPredictor.predict(tabularModel,new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\CIFAR\\bin\\test.csv")),
                new ImageCSVParser(",", "\r\n", 0).setTupleTransferFunction(Examples::cifartupletransfer)), "label" , new Imagefunction(tabularModel));

        predictionResults.show(tabularModel.getDataSet().getFieldIndex("label"));*/
        tabularModel.close();
    }


    public static void main(String[] args) throws IOException {

        long t = System.currentTimeMillis();

       // adult();
       // census();
        letters();
        //mnist();
       // mnistletters();
      //  cifar();
      //  rz();

        t = System.currentTimeMillis()-t;

        System.out.println(String.format("working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))));

        System.out.println("memory usage: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024) + " Mb");


    }
}
