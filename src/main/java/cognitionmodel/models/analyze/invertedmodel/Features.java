package cognitionmodel.models.analyze.invertedmodel;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.index.BitInvertedIndex;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.decomposers.RecursiveDecomposer;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import java.util.function.BiFunction;

import static java.lang.Math.log;

public class Features {

    public static class Err {
        public double ft = 0;
        public double fe = 0;

        public Err(double ft, double fe) {
            this.ft = ft;
            this.fe = fe;
        }

        public Err addErr(double dft, double dfe){
            ft += dft;
            fe += dfe;
            return this;
        }
    }

    public static HashMap<String, Err> errorAgentsMap(InvertedTabularModel model, BiFunction<Agent, Agent, Double> errorFunction, String predictingfield, RecursiveDecomposer decomposer, Predictionfunction predictionfunction){
        HashMap<String, Err> ferr = new HashMap<String, Err>();

        int si = model.getDataSet().getFieldIndex(predictingfield);

        if (si == -1)
            throw new IllegalArgumentException(predictingfield + " is not found in model data set");

        LinkedList<Object> predictingvalues = new LinkedList<>();
        predictingvalues.addAll(model.getInvertedIndex().getAllValues(predictingfield));

        int predictingFieldIndex = model.getDataSet().getFieldIndex(predictingfield);
        predictingFieldIndex = ((BitInvertedIndex)model.getInvertedIndex()).dataSetFieldIndexToInvertedFieldIndex(predictingFieldIndex);

        HashMap<Object, Integer> pvi = new HashMap<>();
        int recordIndex = 0;

        PredictionResults r = new PredictionResults();
        r.addPredictedDataHeader(si,new Tuple().add(model.getDataSet().getHeader().get(si).getValue()+" Predicted").add(model.getDataSet().getHeader().get(si).getValue()+" From data").addAll(predictingvalues));


        for (Iterator<Object> iterator = predictingvalues.iterator(); iterator.hasNext(); pvi.put(predictingfield+":"+iterator.next(), pvi.size()));
        for (Tuple record: model.getDataSet()){

            if (record.size() > si){
                double[] pa = new double[predictingvalues.size()];
                double[] pp = new double[predictingvalues.size()];
                double[] pc = new double[predictingvalues.size()];
                int c[] = new int[predictingvalues.size()];

                HashMap<Object, LinkedList<Agent>> d = decomposer.decompose(record, predictingfield);
                HashMap<String, Agent> zeroMap = new HashMap<>();

                LinkedList<Agent> zl = d.remove("null");
                if (zl != null){
                    for (Agent a: zl)
                        zeroMap.put(a.getFields().toString(), a);
                }


                for (Map.Entry<Object, LinkedList<Agent>> re : d.entrySet()) {
                    int i = pvi.get(predictingfield + ":" + re.getKey());//pvi.get(a.getRelationValue(predictingfield));
                    for (Agent a : re.getValue()) {
                        if (a.relation.size() > 1) {
                            Agent pva = null;
                            if (zl != null) {
                                BitSet fs = a.getFields();
                                fs.set(predictingFieldIndex, false);
                                pva = zeroMap.get(fs.toString());
                            }

                            pa[i] += predictionfunction.predictionfunction(a, pva);
                            pp[i] += log(a.getFr());//log(a.getP());
                            //pc[i] += (a.getConfP());
                            c[i]++;
                        }
                    }
                }
                int mi = 0;
                double[] pr = new double[c.length];
                for (int i = 0; i < c.length; i++)
                    if ((pr[i] = pa[i]  ) > pa[mi] )
                        mi = i;

                r.put(recordIndex, si, new Tuple().add((c[mi] == 0? "Prediction failed": predictingvalues.get(mi))).add(record.get(si).getValue()).addAll(pr));

                int ti = pvi.get(predictingfield+":"+record.get(si).getValue());
                boolean fitted = (predictingvalues.get(mi).toString().equals(record.get(si).getValue()));

                HashMap<String, Agent> actual = new HashMap<>();


                for (Agent a : d.get(predictingvalues.get(ti))) {
                    actual.put(a.getFields().toString(), a);
                }
                for (Agent a : d.get(predictingvalues.get(mi))) {
                    Agent ta = actual.get(a.getFields().toString());
                    if (!fitted)
                        ferr.compute(a.getSignature(), (k,v) -> (v != null ? v.addErr(0, errorFunction.apply(a, ta)):  new Err(0, errorFunction.apply(a, ta))));
                    else
                        ferr.compute(a.getSignature(), (k,v) -> (v != null ? v.addErr(errorFunction.apply(a, ta), 0):  new Err(errorFunction.apply(a, ta), 0)));
                }


                recordIndex++;
            }
        }

        return ferr;
    }




}
