package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.TabularModel;
import cognitionmodel.models.decomposers.BruteForceDecomposer;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.predictors.PredictionResults;
import cognitionmodel.predictors.predictionfunctions.Powerfunction;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Double.NaN;
import static java.lang.Math.*;

public class InvertedTabularModel extends TabularModel {

    public HashMap<String, TreeMap<Object, BitSet>> invertedIndex = new HashMap();// = new HashMap<>();
    private TableDataSet dataSet;
   // private ArrayList<Point> points = new ArrayList<>();
    public HashMap<String, Agent> agentsindex =  new HashMap<>();


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
        indexInit();
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



    public boolean canMerge(Agent a1, Agent a2){
        BitSet b = new BitSet();

        b.or(a1.fields);
        b.and(a2.fields);

        if (b.isEmpty())
            if (log(a1.getP()*a2.getP()*dataSet.size()) < (a1.getZ()+a2.getZ()))
                return false;

        return b.isEmpty();
    }

    public void make(){
        predict(null, null, new Powerfunction(null, 0,1));
    }


 /*   private  LinkedList<Agent> decompose(Tuple record, String predictingfield){
        LinkedList<Agent> newAgents = new LinkedList<>();


        points.clear();
        if (record == null)
            initPoints();
        else
            initPoints(record, predictingfield);

        for (Map.Entry<Object, BitSet> entry : invertedIndex.get(predictingfield).entrySet())
            points.add(new Point(predictingfield, entry.getKey()));

        for (Point point : points) {
            TreeMap<Object, BitSet> tr = invertedIndex.get(point.field);
            Agent na = new Agent(point, this), nr = null;
            if (tr.size() * epsilon > 1) {
                Map.Entry<Object, BitSet> a = tr.ceilingEntry(point.value);
                Map.Entry<Object, BitSet> b = tr.floorEntry(point.value);
                while ((na.getConfP() > 1 - epsilon) & (a != null | b != null)) {
                    if (a != null) a = tr.lowerEntry(a.getKey());
                    if (b != null) b = tr.higherEntry(b.getKey());
                    if (a != null) {
                        na.addPoint(new Point(point.field, a.getKey()));
                    }
                    if (b != null) {
                        na.addPoint(new Point(point.field, b.getKey()));
                    }
                    if (na.getConfP() > 1 - epsilon)
                        nr = na;
                }
                if (nr != null)
                    if (!agentsindex.containsKey(nr.signature)) {
                        newAgents.add(nr);
                        agentsindex.put(nr.signature, nr);
                    }
            } else
            {
                newAgents.add(na);
                agentsindex.put(na.signature, na);
            }
        }
        double dz , ddz = -1;
        int it = newAgents.size(), cn = 0;

        HashMap<String, Agent> addagentindex = new HashMap<>();

        do {
            dz = ddz;
            cn = 0;
            LinkedList<Agent> addAgents = new LinkedList<>();

            for (Iterator<Agent> agentIterator = newAgents.descendingIterator(); agentIterator.hasNext() & (it--) > 0; ) {
                Agent a1 = agentIterator.next();
                if (a1.getP() > 1.0 / dataSet.size())
                    if (a1.relationByField.get(predictingfield).size() > 0 & a1.getLength() < d & (a1.relation.size() == 1 | a1.getCondP(predictingfield) < tau))// & a1.getCondP(predictingfield) > 1 - tau )
                        for (Agent a2 : newAgents)
                            if (a2.getP() > 1.0 / dataSet.size())
                                if (a1 != a2 & a2.relationByField.get(predictingfield).size() == 0 & canMerge(a1, a2) & a2.getLength() < d & (a2.relation.size() == 1 | a2.getCondP(predictingfield) < tau)){// & a2.getCondP(predictingfield) > 1 - tau ) {
                                    Agent na = merge(a1, a2);
                                    if (!addagentindex.containsKey(na.signature)  & (na.getZ() >= (a1.getZ() + a2.getZ()) * (1 + gamma)) & na.getConfP() >= 1 - epsilon) {
                                        addAgents.add(na);
                                        if (!agentsindex.containsKey(na.signature)) agentsindex.put(na.signature, na);
                                        addagentindex.put(na.signature, na);
                                        cn++;
                                    }
                                }
            }
            it = cn;
            newAgents.addAll(addAgents); //newAgents.sort(Comparator.comparing(a -> -a.iteration));

        } while (cn != 0);

        return newAgents;
    }
*/

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

        BruteForceDecomposer decomposer = new BruteForceDecomposer(this);

        for (Tuple record: records)
         if (record.size() > si){
            LinkedList<Object> predictingvalues = new LinkedList<>();
            predictingvalues.addAll(invertedIndex.get(predictingfield).keySet());

            double[] pa = new double[predictingvalues.size()];
            double[] pc = new double[predictingvalues.size()];
            int c[] = new int[predictingvalues.size()];

            BitSet f = new BitSet();

            for (Agent a : decomposer.decompose(record, predictingfield)) {
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

    public Agent merge(Agent a1, Agent a2) {
        Agent r = new Agent(null, this);

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

        r.dZ = r.getZ() - a1.getZ() - a2.getZ();

     //   r.resign();
        return r;
    }





}
