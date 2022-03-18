package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.TabularModel;
import cognitionmodel.models.decomposers.BasicDecomposer;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import static java.lang.Math.*;

public class InvertedBitTabularModel extends TabularModel {

    public HashMap<String, TreeMap<Object, BitSet>> invertedIndex = new HashMap();
    private TableDataSet dataSet;
    public HashMap<String, BitAgent> agentsindex =  new HashMap<>();


    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     * @param relationInstance - the instance of relation for this model
     */
    public InvertedBitTabularModel(TableDataSet dataSet, LightRelation relationInstance, String... enabledFieldsNames){
        super(dataSet, relationInstance, enabledFieldsNames);
        this.dataSet = (TableDataSet) dataSet;
        indexInit();
    }

    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     */
    public InvertedBitTabularModel(TableDataSet dataSet, String... enabledFieldsNames) {
        this(dataSet, new LightRelation(), enabledFieldsNames);
    }

    private HashSet<Integer> addToSet(Object set, Integer value){
        ((HashSet<Integer>)set).add(value);
        return ((HashSet<Integer>)set);
    }

    protected void indexInit() {

        for (int i = 0; i < dataSet.getHeader().size(); i++)
            if (getEnabledFields()[i] == 1)
                invertedIndex.put(dataSet.getHeader().get(i).getValue().toString(), new TreeMap<Object, BitSet>());

        int i = 0;
        for (Tuple tuple: dataSet) {
            int j = 0;
            for (TupleElement tupleElement: tuple){
                if (getEnabledFields()[j] == 1) {
                    String fieldName = dataSet.getHeader().get(j).getValue().toString();
                    BitSet idx;
                    if (invertedIndex.get(fieldName).containsKey(tupleElement.getValue()))
                        idx = invertedIndex.get(fieldName).get(tupleElement.getValue());
                    else {
                        idx = new BitSet();
                        invertedIndex.get(fieldName).put(tupleElement.getValue(), idx);
                    }
                    idx.set(i);
                }
                j++;
            }
            i++;
        }
    }


    public boolean canMerge(BitAgent a1, BitAgent a2){
        BitSet b = new BitSet();

        b.or(a1.fields);
        b.and(a2.fields);

        if (b.isEmpty())
            if (log(a1.getP()*a2.getP()*dataSet.size()) < (a1.getMR()+a2.getMR()))
                return false;

        return b.isEmpty();
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

        BasicDecomposer decomposer = new BasicDecomposer(this);

        for (Tuple record: records)
         if (record.size() > si){
            LinkedList<Object> predictingvalues = new LinkedList<>();
            predictingvalues.addAll(invertedIndex.get(predictingfield).keySet());

            double[] pa = new double[predictingvalues.size()];
            double[] pc = new double[predictingvalues.size()];
            int c[] = new int[predictingvalues.size()];

            BitSet f = new BitSet();

            for (BitAgent a : decomposer.decompose(record, predictingfield)) {
                int i = 0;
                if (a.relation.size() > 1)
                    if (!f.intersects(a.fields))
                         for (Object v : predictingvalues) {
                            if (a.relation.containsKey(predictingfield + ":" + v)) {
                                pa[i] += predictionfunction.predictionfunction(a, predictingfield);
                                pc[i] += (a.getConfP());
                                c[i]++;
                            }
                    i++;
                }
            }

            int i1 = 0;
            for (Object v : predictingvalues) {
                pa[i1] = exp(pa[i1++]);
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

    protected BitSet getIndexes(Point point){
        TreeMap<Object, BitSet> pointinvertedindex = invertedIndex.get(point.field);
        return pointinvertedindex.get(point.value);
    }

    public BitAgent merge(BitAgent a1, BitAgent a2) {
        BitAgent r = new BitAgent(null, this);

        for (Point p: a1.relation.values()) {
            r.relation.put(p.toString(), p);
            r.relationByField.get(p.field).add(p.toString());
        }

        for (Point p: a2.relation.values()) {
            r.relation.put(p.toString(), p);
            r.relationByField.get(p.field).add(p.toString());
        }

        r.resign();

        if (agentsindex.containsKey(r.signature))
            return agentsindex.get(r.signature);

        for (String f: invertedIndex.keySet()) {
            BitSet bs = r.recordsByField.get(f);
            bs.or(a1.recordsByField.get(f));
            bs.or(a2.recordsByField.get(f));
        }

        r.fields.or(a1.fields);
        r.fields.or(a2.fields);

        int i=0;
        for (BitSet bs: (r.recordsByField.values()))
            if (!bs.isEmpty())
                if (i++ == 0) r.records.or(bs);
                else
                    r.records.and(bs);

        r.dZ = r.getMR() - a1.getMR() - a2.getMR();

        return r;
    }

}
