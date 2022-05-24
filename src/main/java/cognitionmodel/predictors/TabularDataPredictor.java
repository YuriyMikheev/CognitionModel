package cognitionmodel.predictors;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.TabularModel;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Math.round;

/**
 * Encapsulates predictor method applied to @param Model
 */

public class TabularDataPredictor implements Predictor{

    /**
     * Makes predictions for the tabular data in @param signatureIndex from the @param dataSet based on @param model
     *
     * @param model - tabular model underlines prediction
     * @param dataSet - data set with data for making predictions
     * @param signatureIndex - index of the unknown data in the @param model relations
     *
     * @return - results of predictions saved in PredictionResults object
     */


    @NotNull
    public static PredictionResults predict(TabularModel model, TableDataSet dataSet, int signatureIndex, Predictionfunction predictionfunction){

        int[] altTerminals = model.termsByField(signatureIndex);
        String[] altTermNames = new String[altTerminals.length];
        AtomicInteger zerofit = new AtomicInteger();
        AtomicInteger fit = new AtomicInteger();

        for (int i = 0; i < altTerminals.length; i++)
            altTermNames[i] = model.getRelationMethods().getTerminalsArray().get(altTerminals[i]);


        PredictionResults r = new PredictionResults();
        r.addPredictedDataHeader(signatureIndex,new Tuple().add(dataSet.getHeader().get(signatureIndex).getValue()+" Predicted").add(dataSet.getHeader().get(signatureIndex).getValue()+" From data").addAll(altTermNames));

        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();


        int recordIndex = 0;
        for (Tuple record: dataSet)
           if (record.size() > signatureIndex){
               int finalRecordIndex = recordIndex;
               cfl.add(CompletableFuture.supplyAsync(() -> {

                   TupleElement stored = record.get(signatureIndex);
                   record.getTupleElements().set(signatureIndex, new TupleElement(""));

                   LinkedList<int[]> relations = model.generateRelations(record);

                   Double[] altP = new Double[altTerminals.length];
                   Arrays.fill(altP, 0.0);

                   for (int[] relation : relations) {
                       int c = 0;
                       for (int j = 0; j < altTerminals.length; j++) {
                           double da = predictionfunction.predictionfunction(model.getRelationMethods().addTermToRelation(relation, signatureIndex, altTerminals[j]), signatureIndex);
                           if (!Double.isNaN(da))altP[j] += da;
                           if (da == 0) c++;
                       }
                       if (c == altTerminals.length)
                           zerofit.getAndIncrement();
                       fit.getAndIncrement();
                   }

                   int maxi = -1;
                   Double maxd = Double.MIN_VALUE;

                   for (int i = 0; i < altP.length; i++) {
                       if (altP[i] > maxd) {
                           maxi = i;
                           maxd = altP[i];
                       }
                   }

                   r.put(finalRecordIndex, signatureIndex, new Tuple().add(maxi != -1 ? altTermNames[maxi] : "Prediction failed").add(stored).addAll(Arrays.stream(altP).collect(Collectors.toList())));

                   return null;
               }));

               if (recordIndex++ % (int) (dataSet.size() * 0.01 + 1) == 0 | recordIndex == dataSet.size()) {
                   cfl.stream().map(m -> m.join()).collect(Collectors.toList());
                   cfl.clear();
               }
        }

        System.err.println("no fitted relations "+((double)zerofit.get()/fit.get()));

        return r;
    }

    /**
     * Makes predictions for the tabular data in @param signatureIndex from the @param dataSet based on @param model
     *
     * @param model - tabular model underlines prediction
     * @param dataSet - data set with data for making predictions
     * @param fieldName - name of the predicted column in table
     * @return - results of predictions saved in PredictionResults object
     */


    public static PredictionResults predict(TabularModel model, TableDataSet dataSet, String fieldName, Predictionfunction predictionfunction){

        int i = 0;
        for (TupleElement t:dataSet.getHeader())
            if (t.getValue().toString().equals(fieldName)) break;
               else i++;

        if (i == dataSet.getHeader().size() | !model.getDataSet().getHeader().get(i).getValue().toString().equals(fieldName)) {
            throw new IllegalArgumentException(fieldName + " is not found in model data set");
        }

        return predict(model, dataSet, i, predictionfunction);
    }

    /**
     * Fits numeric values in data set to nearest values in the dataset of the model
     * @param model - the model to fit to
     * @param dataSet - fitting dataset
     * @return dataset with modified numeric values
     */


    public static TableDataSet fit2model(TabularModel model, TableDataSet dataSet) {

        TableDataSet modelDataSet = model.getDataSet();

        TreeSet<Double>[] valuesMap = new TreeSet[dataSet.getHeader().size()];
        for (int i = 0; i < modelDataSet.getHeader().size(); i++) {
            valuesMap[i] = new TreeSet<>();
        }

        for (Tuple record: modelDataSet){
            for (int i = 0; i < modelDataSet.getHeader().size(); i++) {
                if (i < record.size())
                    if (record.get(i).isNumber())
                        valuesMap[i].add(record.get(i).asDouble());
            }
        }

        for (Tuple record: dataSet){
            for (int i = 0; i < dataSet.getHeader().size(); i++){
                if (i < record.size())
                    if (record.get(i).isNumber() & !valuesMap[i].isEmpty()) {
                        Double value = record.get(i).asDouble();
                        if (!valuesMap[i].contains(value)) {
                            if (record.get(i).getType() == TupleElement.Type.Double)
                                record.set(i, valuesMap[i].floor(value) != null ? valuesMap[i].floor(value)  : valuesMap[i].higher(value));
                            if (record.get(i).getType() == TupleElement.Type.Int)
                                record.set(i, valuesMap[i].floor(value) != null ? (int)round(valuesMap[i].floor(value)) : (int)round(valuesMap[i].higher(value)));
                        }
                    }
            }
        }

        return dataSet;
    }


}
