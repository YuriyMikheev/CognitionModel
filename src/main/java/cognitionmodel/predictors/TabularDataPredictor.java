package cognitionmodel.predictors;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.LightRelation;
import cognitionmodel.models.TabularModel;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Encapsulates predictor method applied to @param Model
 */

public class TabularDataPredictor extends Predictor{

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
                       for (int j = 0; j < altTerminals.length; j++)
                            altP[j] += predictionfunction.predictionfunction(model.getRelationMethods().addTermToRelation(relation,signatureIndex,altTerminals[j]),signatureIndex);
                   }

                   int maxi = -1;
                   Double maxd = Double.MIN_NORMAL;

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

}
