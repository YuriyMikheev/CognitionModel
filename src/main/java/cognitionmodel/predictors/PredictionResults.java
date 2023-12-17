package cognitionmodel.predictors;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.pow;

/**
 * Consists results of prediction saved in map
 */
public class PredictionResults {

    private ChronicleMap<int[],Tuple> datamap;
    private int tpr  = -1, wpr = -1, nfailed = 0;
    private int[][] confusionMatrix;

    private boolean ischanged = true;

    private Double MSE = null;


    /**
     * Creates PredictionResults object
     * Header is denominates predicted data {filedName +"Predicted", filedName +"From data", {field values} }
     * Data consists                        {Predicted value, Value from data, {probabilities of the values}}
     *
     *
     */

    public PredictionResults(){

        datamap = ChronicleMapBuilder.of(int[].class, Tuple.class)
                .name("predictionsMap")
                .entries(1000000)
                .maxBloatFactor(10)
                .averageKeySize(50)
                .averageValueSize(2000)
                .create();

    }

    public void addPredictedDataHeader(int elementIndex, Tuple header){
        put(-1,elementIndex, header);
    }


    /**
     * Get data form map
     * @param recordIndex - record number in data set
     * @param elementIndex - index of data, for example field number in dataset
     * @return - saved data
     */


    public Tuple get(int recordIndex, int elementIndex){
        return datamap.get(new int[]{recordIndex, elementIndex});
    }

    /**
     * Puts data to  map
     * @param recordIndex - record number in data set
     * @param elementIndex - index of data, for example field number in dataset
     * @param data - saving data
     */

    public void put(int recordIndex, int elementIndex, Tuple data){
        datamap.put(new int[]{recordIndex, elementIndex}, data);
    }

    public Tuple getHeader(int elementIndex) {
        return get(-1,elementIndex);
    }

    public int size(){
        return datamap.size() - 1;
    }


    public int[][] confusionMatrix(int elementIndex) {

        Tuple header = getHeader(elementIndex);

        int[][] confusionMatrix = new int[header.size() - 2][header.size() - 2];
        HashMap<String, Integer> altToIdx = new HashMap<>();

        for (int i = 2; i < header.size(); i++)
            altToIdx.put(header.get(i).getValue().toString(), i-2);


        Iterator<Map.Entry<int[],Tuple>> entryIterator = datamap.entrySet().iterator();

        while (entryIterator.hasNext()){
            Map.Entry<int[],Tuple> entry = entryIterator.next();
            Tuple tuple = entry.getValue();
            if (entry.getKey()[1] == elementIndex & entry.getKey()[0] >= 0)
                if (altToIdx.get(tuple.get(1).getValue().toString()) != null & altToIdx.get(tuple.get(0).getValue().toString()) != null & !tuple.get(1).getValue().equals("Prediction failed") & !tuple.get(0).getValue().equals("Prediction failed"))
                    confusionMatrix[altToIdx.get(tuple.get(0).getValue().toString())][altToIdx.get(tuple.get(1).getValue().toString())]++;
                else nfailed++;
        }


        return confusionMatrix;
    }

    public Double getMSE(){
        Iterator<Map.Entry<int[],Tuple>> entryIterator = datamap.entrySet().iterator();

        MSE = 0.0;

        while (entryIterator.hasNext()){
            Map.Entry<int[],Tuple> entry = entryIterator.next();
            if (entry.getKey()[0] != -1) {
                Tuple tuple = entry.getValue();
                try {
                    MSE += pow(tuple.get(0).asDouble() - tuple.get(1).asDouble(), 2);
                } catch (ClassCastException e){

                }
            }
        }

        MSE = MSE / (datamap.size() - 1);

        return MSE;
    }

    /**
     * Shows confusion matrix, accuracy, precision, recall and F1-score
     */

    public  void show(int elementIndex) {

        if (ischanged) confusionMatrix = confusionMatrix(elementIndex);

        Tuple terminals = getHeader(elementIndex).clone();
        terminals.getTupleElements().remove(0);
        terminals.getTupleElements().remove(0);



        System.out.println("Confusion matrix\n");
        System.out.print("\t\t\tactual \n predicted");

        for (int i = 0; i < confusionMatrix.length; i++)
            System.out.printf("%9s ",terminals.get(i).getValue());
        System.out.println("\t");

        int tpr = 0, wpr = 0;
        int fp[] = new int[confusionMatrix.length];
        int fn[] = new int[confusionMatrix.length];
        int tp[] = new int[confusionMatrix.length];

        for (int i = 0; i < confusionMatrix.length; i++) {
            System.out.printf("%9s ",terminals.get(i).getValue());
            tp[i] = tp[i] + confusionMatrix[i][i];
            tpr = tpr + confusionMatrix[i][i];
            for (int j = 0; j < confusionMatrix.length; j++) {
                System.out.printf("%9d ", confusionMatrix[i][j]);
                if (i != j) {
                    wpr = wpr + confusionMatrix[i][j];
                    fn[i] = fn[i] + confusionMatrix[i][j];
                    fp[i] = fp[i] + confusionMatrix[j][i];
                }
            }
            System.out.println();
        }


        System.out.println();
        System.out.printf("Accuracy\t%1$.5f\n",((double) tpr / (tpr + wpr)));
        System.out.printf("Error\t\t%1$.5f\n",((double) wpr / (tpr + wpr)));
        System.out.println();
        System.out.println("Class    \tPrecision\tRecall\t\tF1-Score");

        double mpr = 0, mrec = 0, mf1 = 0;

        for (int i = 0; i < confusionMatrix.length; i++) {
            double prec, rec, f1;
            System.out.printf("%1$9s\t%2$.5f\t\t%3$.5f\t\t%4$.5f\n", terminals.get(i).getValue().toString(),(prec = (double) tp[i]/(tp[i]+fp[i])),(rec = (double)tp[i]/(tp[i]+fn[i]) ),(f1 = 2*prec*rec/(prec+rec)));
            mpr = mpr + (Double.isNaN(prec)?0:prec); mrec = mrec + (Double.isNaN(rec)?0:rec); mf1 = mf1 + (Double.isNaN(f1)?0:f1);
        }

        System.out.printf("\nMean\t\t%1$.5f\t\t%2$.5f\n",(mpr/(confusionMatrix.length)),(mrec/(confusionMatrix.length)));
        System.out.printf("Total \nF1-Score\t\t\t\t\t\t\t%1$.5f\n",(2*mpr*mrec/((confusionMatrix.length)*(mpr+mrec))));
        if (terminals.get(0).getType() == TupleElement.Type.Double)
            System.out.printf("MSE\t\t\t\t\t\t\t\t\t%1$.5f\n", getMSE());
        System.out.println();
        System.out.println("Prediction failed in "+nfailed+" cases");

    }

    public void toCSVFile(String file) throws IOException {
        Iterator<Map.Entry<int[],Tuple>> entryIterator = datamap.entrySet().iterator();

        BufferedWriter writer = Files.newBufferedWriter(Path.of(file), StandardCharsets.UTF_8, new StandardOpenOption[]{StandardOpenOption.CREATE});

        while (entryIterator.hasNext()){
            Map.Entry<int[],Tuple> entry = entryIterator.next();
            Tuple tuple = entry.getValue();
            writer.write(tuple.toCSVString()+"\n");
        }

        writer.close();
    }

}
