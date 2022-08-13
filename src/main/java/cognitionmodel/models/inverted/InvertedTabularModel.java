package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.TabularModel;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.*;

public class InvertedTabularModel {
    private byte[] enabledFields;

    private TableDataSet dataSet;
    //public HashMap<String, Agent> agentsindex =  new HashMap<String, Agent>();
    public InvertedIndex invertedIndex;
    HashMap<String, Agent> agentsindex = new HashMap<>();


    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     * @param relationInstance - the instance of relation for this model
     */
    public InvertedTabularModel(TableDataSet dataSet, LightRelation relationInstance, String... enabledFieldsNames){
        this.dataSet = (TableDataSet) dataSet;



        if (enabledFieldsNames != null)
            if (enabledFieldsNames.length == 0) enabledFieldsNames = null;

        if (enabledFieldsNames == null) {
            this.enabledFields = new byte[dataSet.getHeader().size()];
            Arrays.fill(this.enabledFields, (byte) 1);
        }
        else {
            this.enabledFields = new byte[dataSet.getHeader().size()];
            setEnabledFields(enabledFieldsNames);
        }
        invertedIndex = new BitInvertedIndex(this);

       // indexInit();
    }

    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     */
    public InvertedTabularModel(TableDataSet dataSet, String... enabledFieldsNames) {
        this(dataSet, null, enabledFieldsNames);
    }


    /**
     * Sets fields that are enabled in model making process
     * @param fields names of fields from table data header
     */

    private void setEnabledFields(String... fields){

        int i = 0;
        for (TupleElement t: (((TableDataSet)getDataSet()).getHeader())){
            enabledFields[i] = 0;

            for (String f: fields){
                if (f.equals(t.getValue().toString()))
                    enabledFields[i] = 1;
            }
            i++;
        }

    }

    public byte[] getEnabledFields() {
        return enabledFields;
    }

    public TableDataSet getDataSet() {
        return dataSet;
    }

    public void make(){
        predict(null, null, new Powerfunction(null, 0,1), false, 4,  a -> a.getMR() > 0);
    }

    public PredictionResults predict(List<Tuple> records, String predictingfield, Predictionfunction predictionfunction, boolean modelcashed, int maxDepth, Function<Agent, Boolean> agentFilter){
        int si = dataSet.getFieldIndex(predictingfield);

        if (si == -1)
            throw new IllegalArgumentException(predictingfield + " is not found in model data set");

        LinkedList<Object> predictingvalues = new LinkedList<>();
        predictingvalues.addAll(invertedIndex.getAllValues(predictingfield));

        int predictingFieldIndex = getDataSet().getFieldIndex(predictingfield);
        predictingFieldIndex = ((BitInvertedIndex)getInvertedIndex()).dataSetFieldIndexToInvertedFieldIndex(predictingFieldIndex);

        HashMap<Object, Integer> pvi = new HashMap<>();

        for (Iterator<Object> iterator = predictingvalues.iterator(); iterator.hasNext(); pvi.put(predictingfield+":"+iterator.next(), pvi.size()));

        PredictionResults r = new PredictionResults();
        r.addPredictedDataHeader(si,new Tuple().add(dataSet.getHeader().get(si).getValue()+" Predicted").add(dataSet.getHeader().get(si).getValue()+" From data").addAll(predictingvalues));

        int recordIndex = 0;

  //      BasicDecomposer decomposer = new BasicDecomposer(this);
  //      PatternDecomposer decomposer = new PatternDecomposer(new FullGridRecursivePatterns(this, 3).getPatterns(), this, predictingfield, false, a -> a.getMR() > 0);
//       RecursiveDecomposer decomposer = new RecursiveDecomposer(this, predictingfield, false, 3,  a -> a.getMR() > 0);
//        RecursiveLevelValuesDecomposer decomposer = new RecursiveLevelValuesDecomposer(new EqualIntervaledBitInvertedIndex((BitInvertedIndex) getInvertedIndex(), predictingfield, 10), predictingfield, modelcashed, maxDepth,  agentFilter);
        RecursiveLevelValuesDecomposer decomposer = new RecursiveLevelValuesDecomposer(getInvertedIndex(), predictingfield, modelcashed, maxDepth,  agentFilter);
//        getInvertedIndex().setConfidenceIntervals(0.95);

       // HashMap<String, Features.Err> ferr = Features.errorAgentsMap(this, (a, b) -> {return 1.0;}, predictingfield, decomposer, predictionfunction);
        double pcth = getInvertedIndex().getConfidenceIntervals() != null ? getInvertedIndex().getConfidenceIntervals()[predictingFieldIndex]: 1;

        for (Tuple record: records)
            if (record.size() > si){
                double[] pa = new double[predictingvalues.size()];
                double[] pp = new double[predictingvalues.size()];
                double[] pc = new double[predictingvalues.size()];
                int c[] = new int[predictingvalues.size()];

                HashMap<Object, LinkedList<Agent>> d = decomposer.decompose(record, predictingfield, getInvertedIndex().getConfidenceIntervals() == null? getInvertedIndex() : new DynamicIntervaledBitInvertedIndex((BitInvertedIndex) getInvertedIndex(), record, predictingfield));
//                HashMap<Object, LinkedList<Agent>> d = decomposer.decompose(record, predictingfield);
                HashMap<String, Agent> zeroMap = new HashMap<>();

                LinkedList<Agent> zl = d.remove("null");
                if (zl != null){
                    for (Agent a: zl)
                        zeroMap.put(a.getFields().toString(), a);
                }

                for (Map.Entry<Object, LinkedList<Agent>> re : d.entrySet()) {
                    int i = pvi.get(predictingfield + ":" + re.getKey());//pvi.get(a.getRelationValue(predictingfield));
                    for (Agent a : re.getValue()) {
                        if (a.getConfP() >= pow(pcth, a.relation.size()) /*&  a.relation.size() > 1*/) {
                            Agent pva = null;
                            if (zl != null) {
                                BitSet fs = a.getFields();
                                fs.set(predictingFieldIndex, false);
                                pva = zeroMap.get(fs.toString());
                            }

                            pa[i] += predictionfunction.predictionfunction(a, pva);
                         //   pp[i] += log(a.getFr());//log(a.getP());
                            pc[i] += log(a.getConfP());
                            c[i]++;
                        }
                    }
                }

                int mi = 0;
                double[] pr = new double[c.length];
                for (int i = 0; i < c.length; i++)
                    if ((pr[i] = pa[i]  ) > pa[mi])
                        mi = i;

                Object val;

                if (record.get(si).getType() == TupleElement.Type.Int) val = (int)record.get(si).getValue() * 1.0;
                    else val = record.get(si).getValue();

                r.put(recordIndex, si, new Tuple().add((c[mi] == 0 ? "Prediction failed": predictingvalues.get(mi))).add(val).addAll(pr));

                recordIndex++;
        }

     //   double sc = ferr.values().stream().mapToDouble(Double::doubleValue).sum();
    //    ferr.keySet().forEach(e -> {ferr.compute(e, (k, v) -> v = v/sc);});
    //    System.out.println("Features cumulative errors");
   //     ferr.entrySet().stream().sorted(Comparator.comparing(e -> -e.getValue())).collect(toList()).forEach(System.out::println);

        return r;
    }

    /**
     * Changes result of prediction of the numerical values to fit regression logic according to probability density result distribution
     * @param predictionResults - result of predict method
     */

    public PredictionResults regression(PredictionResults predictionResults, String predictingfield){
        int si = dataSet.getFieldIndex(predictingfield), l = predictionResults.size();

        Tuple h = predictionResults.getHeader(si);
        TreeSet<Double> hv = new TreeSet<>();
        for (int j = 2; j < h.size(); j++)
            hv.add(h.get(j).asDouble());
//        r.put(recordIndex, si, new Tuple().add((c[mi] == 0 ? "Prediction failed": predictingvalues.get(mi))).add(val).addAll(pr));

        for (int i = 0; i < l; i++){
            Tuple pr = predictionResults.get(i, si);
            double pvals[] = new double[pr.size() - 2], sm = 0, r = 0;
            for (int j = 2; j < pr.size(); j++)
                sm += (pvals[j-2] = pr.get(j).asDouble());
            for (int j = 0; j < pvals.length; j++)
//                if ((pvals[j] / sm) > 0.3)
                    r += (pvals[j] = (pvals[j] / sm) * h.get(j+2).asDouble());
            Double f = hv.floor(r), hi = hv.higher(r);
            if (f == null & hi == null) throw new IllegalArgumentException("Results of prediction has no approppriet values");
            if (f == null) r = hi;
                else
                    if (hi == null) r = f;
                        else
                            if (abs(f-r) > abs(hi - r)) r = hi;
                                else r = f;
            pr.set(0, r);
            predictionResults.put(i, si, pr);
        }
        return predictionResults;
    }

    public InvertedIndex getInvertedIndex(){
        return invertedIndex;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }
}
