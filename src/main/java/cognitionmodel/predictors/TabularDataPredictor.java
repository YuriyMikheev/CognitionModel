package cognitionmodel.predictors;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.LightRelation;
import cognitionmodel.models.TabularModel;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
     * @param wz - weight of Z in prediction
     * @param wp - weight of P in prediction
     *
     * @return - results of predictions saved in PredictionResults object
     */


    @NotNull
    public static PredictionResults predict(TabularModel model, TableDataSet dataSet, int signatureIndex, double wp, double wz){

        LinkedList<Integer> altTerminalsIndices = new LinkedList<>();
        LinkedList<String> altTerminals = new LinkedList<>();

        for (int i = 1; i < LightRelation.getTerminalsArray().size(); i++){
            if (model.getFieldIndex(i) == signatureIndex) {
                altTerminalsIndices.add(i);
                altTerminals.add(LightRelation.getTerminalsArray().get(i));
            }
        }

        PredictionResults r = new PredictionResults();
        r.addPredictedDataHeader(signatureIndex,new Tuple().add(dataSet.getHeader().get(signatureIndex).getValue()+" Predicted").add(dataSet.getHeader().get(signatureIndex).getValue()+" From data").addAll(altTerminals));

        int recordIndex = 0;
        for (Tuple record: dataSet){

            TupleElement stored = record.get(signatureIndex);
            record.getTupleElements().set(signatureIndex, new TupleElement(""));

            LinkedList<int[]> relations = model.generateRelations(record);

            Double[] altP = new Double[altTerminals.size()];
            Arrays.fill(altP,0.0);

            for (int[] relation: relations) {
                relation[signatureIndex] = 0;
                double fp = model.getFrequency(relation);
                int j = 0;
                if (fp != 0)
                    for (int altTerm: altTerminalsIndices){
                        relation[signatureIndex] = altTerm;
                        double p = model.getFrequency(relation) / fp;
                        double z = model.getZ(relation);
                        altP[j++] += Math.pow(p,wp) * Math.pow(z, wz);
                    }
            }

            int maxi = -1;
            Double maxd = -1000000000.0;

            for (int i = 0; i < altP.length; i++) {
                if (altP[i] > maxd) {
                    maxi = i;
                    maxd = altP[i];
                }
            }

            r.put(recordIndex,signatureIndex,new Tuple().add(maxi!= -1? altTerminals.get(maxi):"Prediction failed").add(stored).addAll(Arrays.stream(altP).collect(Collectors.toList())));

            recordIndex++;
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


    public static PredictionResults predict(TabularModel model, TableDataSet dataSet, String fieldName, double wp, double wz){

        int i = 0;
        for (TupleElement t:dataSet.getHeader())
            if (t.getValue().toString().equals(fieldName)) break;
               else i++;

        if (!model.getDataSet().getHeader().get(i).getValue().toString().equals(fieldName)) {
            System.err.println(fieldName + "is not found in model data set");
            throw new IllegalArgumentException();
        }

        return predict(model, dataSet, i, wp, wz);
    }




}
