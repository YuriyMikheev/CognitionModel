package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.TabularModel;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.patterns.FullGridRecursivePatterns;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import java.util.function.Function;

public class InvertedTabularModel extends TabularModel {

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
        super(dataSet, relationInstance, enabledFieldsNames);
        this.dataSet = (TableDataSet) dataSet;

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
        this(dataSet, new LightRelation(), enabledFieldsNames);
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
        RecursiveLevelValuesDecomposer decomposer = new RecursiveLevelValuesDecomposer(this, predictingfield, modelcashed, maxDepth,  agentFilter);

        for (Tuple record: records)
         if (record.size() > si){

            double[] pa = new double[predictingvalues.size()];
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

            recordIndex++;

        }

        return r;
    }

    protected InvertedIndex getInvertedIndex(){
        return invertedIndex;
    }



}
