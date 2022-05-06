package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.TabularModel;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;
import org.roaringbitmap.RoaringBitmap;

import java.lang.reflect.ParameterizedType;
import java.util.*;

import static java.lang.Math.exp;
import static java.lang.Math.log;

public class InvertedTabularModel extends TabularModel {

    private TableDataSet dataSet;
    //public HashMap<String, Agent> agentsindex =  new HashMap<String, Agent>();
    public InvertedIndex invertedIndex;


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




    private HashSet<Integer> addToSet(Object set, Integer value){
        ((HashSet<Integer>)set).add(value);
        return ((HashSet<Integer>)set);
    }


    public void make(){
        predict(null, null, new Powerfunction(null, 0,1));
    }

    public PredictionResults predict(List<Tuple> records, String predictingfield, Predictionfunction predictionfunction){

        int si = 0;
        for (TupleElement t:dataSet.getHeader())
            if (t.getValue().toString().equals(predictingfield)) break;
            else si++;

        if (si == dataSet.getHeader().size() | !getDataSet().getHeader().get(si).getValue().toString().equals(predictingfield)) {
            throw new IllegalArgumentException(predictingfield + " is not found in model data set");
        }

        int[] altTerminals = termsByField(si);
        String[] altTermNames = new String[altTerminals.length];

        for (int i = 0; i < altTerminals.length; i++)
            altTermNames[i] = getRelationMethods().getTerminalsArray().get(altTerminals[i]);

        PredictionResults r = new PredictionResults();
        r.addPredictedDataHeader(si,new Tuple().add(dataSet.getHeader().get(si).getValue()+" Predicted").add(dataSet.getHeader().get(si).getValue()+" From data").addAll(altTermNames));

        int recordIndex = 0;

//        MonteCarloDecomposer decomposer = new MonteCarloDecomposer(this);
        BasicDecomposer decomposer = new BasicDecomposer(this);


        LinkedList<Object> predictingvalues = new LinkedList<>();
        predictingvalues.addAll(invertedIndex.getAllValues(predictingfield));

        HashMap<Object, Integer> pvi = new HashMap<>();

        int i=0;
        for (Iterator<Object> iterator = predictingvalues.iterator(); iterator.hasNext(); pvi.put(predictingfield+":"+iterator.next(), i++));

        for (Tuple record: records)
         if (record.size() > si){
            double[] pa = new double[predictingvalues.size()];
            double[] pc = new double[predictingvalues.size()];
            int c[] = new int[predictingvalues.size()];


            for (Agent a : decomposer.decompose(record, predictingfield)) {
                if (a.hasPerdictingField()) {
                    i = pvi.get(a.relationByField.get(predictingfield).toArray(new Object[]{})[0]);
                    pa[i] += predictionfunction.predictionfunction(a, predictingfield);
                    pc[i] += (a.getConfP());
                    c[i]++;
                }
            }

/*            int i1 = 0;
            for (Object v : predictingvalues) {
                pa[i1] = exp(pa[i1++]);
            }*/

            int mi = 0;
            double[] pr = new double[c.length];
            for (i = 0; i < c.length; i++)
                if ((pr[i] = pa[i]  ) > pa[mi] )
                    mi = i;

            r.put(recordIndex, si, new Tuple().add((c[mi] == 0? "Prediction failed": predictingvalues.get(mi))).add(record.get(si).getValue()).addAll(pr));

            recordIndex++;

        }

        return r;
    }

    protected InvertedIndex getIndexes(){
        return invertedIndex;
    }



}
