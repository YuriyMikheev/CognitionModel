package cognitionmodel.models.inverted.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.index.BitInvertedIndex;
import cognitionmodel.models.inverted.index.InvertedIndex;
import cognitionmodel.models.inverted.index.Point;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IterativeTurboDecomposer implements Decomposer {

    private InvertedIndex invertedIndex;
    private String fields[];
    private String predicttingField;
    private int predictingFieldIndex, predictingFieldInvertedIndex;
    private HashMap<String, Agent> agentMap = new HashMap<>();

    private Function<Agent, Boolean> agentFilter;
    private int maxDepth = 300;
    private HashSet<Point> desabledPoints = new HashSet<>();
    private boolean mustHavePredictingField = false;

    public IterativeTurboDecomposer(InvertedIndex invertedIndex, String predictingField, boolean modelcashed) {
        this(invertedIndex, predictingField, modelcashed, 300, null, false);

    }

    public IterativeTurboDecomposer(InvertedIndex invertedIndex, String predictingField, boolean modelcashed, int maxDepth, Function<Agent, Boolean> agentFilter){
        this(invertedIndex, predictingField, modelcashed, maxDepth, agentFilter, false);
    }
    public IterativeTurboDecomposer(InvertedIndex invertedIndex, String predictingField, boolean modelcashed, int maxDepth, Function<Agent, Boolean> agentFilter, boolean mustHavePredictingField){

        this.maxDepth = maxDepth;
        this.invertedIndex = invertedIndex;
        this.predicttingField = predictingField;
        predictingFieldInvertedIndex = invertedIndex.getFieldIndex(predictingField);
        predictingFieldIndex = ((BitInvertedIndex)invertedIndex).invertedIndexToDatasetFieldIndex(predictingFieldInvertedIndex);

        this.agentFilter = agentFilter;
        this.mustHavePredictingField = mustHavePredictingField;
    }

    public boolean isMustHavePredictingField() {
        return mustHavePredictingField;
    }

    public void setMustHavePredictingField(boolean mustHavePredictingField) {
        this.mustHavePredictingField = mustHavePredictingField;
    }

    public Function<Agent, Boolean> getAgentFilter() {
        return agentFilter;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield, InvertedIndex intervaledBitInvertedIndex) {
        invertedIndex = intervaledBitInvertedIndex;
        return decompose(record, predictingfield);
    }
    @Override
    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield) {

        HashMap<Object, LinkedList<Agent>> r = new HashMap<>();
        TupleElement ote = predictingFieldIndex !=-1 ? record.set(predictingFieldIndex, null): null;

        LinkedList<Agent> al  = new LinkedList<>();

        al.add(new Agent((Point) null, invertedIndex));
        doDecompose(al, record, r);

        if (predictingFieldIndex != -1)
            record.set(predictingFieldIndex, ote);

        return r;
    }

    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, List<Point> pointList) {

        HashMap<Object, LinkedList<Agent>> r = new HashMap<>();

        List<Agent> al  = pointList.stream().map(p -> new Agent(p, invertedIndex)).collect(Collectors.toList());

        doDecompose(al, record, r);

        return r;
    }


    private void doDecompose(List<Agent> agents, Tuple record, HashMap<Object,  LinkedList<Agent>> resultMap){

        List<Agent> alist = agents;//, badAgents = new LinkedList<>();


        do {
            LinkedList<Agent> newlevel = new LinkedList<>(), badAgents = new LinkedList<>();//badAgents.clear();
            for (Agent a : alist) {
                for (PointIterator pointIterator = getAgentFieldsIterator(a, record); pointIterator.hasNext(); ) {
                    Point p = pointIterator.next();

                    Agent na = new Agent(p, invertedIndex);
                    if (p.getField().equals(predicttingField))
                        na.setPredictingValue(p.getValue());

                    Agent ca = a.getRelation().size() == 0 ? na : Agent.merge(a, na, invertedIndex);
                    if (ca.getFr() > 0 && (agentFilter == null || agentFilter.apply(ca) || ca.getRelation().size() == 1)) {
                            newlevel.add(ca);
                            Object co = ca.getPredictingValue();
                            if (co == null) co = "null";
                            if (!resultMap.containsKey(co)) resultMap.put(co, new LinkedList<>());
                            if (ca.relation.size() > 1 || co.equals("null")) resultMap.get(co).add(ca);

                    } else {
                        badAgents.add(ca);
                    }
                }
            }

            for (Agent ba : badAgents)
                checkAgents(ba, newlevel);

            if (newlevel.isEmpty()) break;
            if (newlevel.peek().relation.size() >= maxDepth) break;

            alist = newlevel;

        } while (true);
    }

    private void checkAgents(Agent agent, LinkedList<Agent> level){
//        long[] b = agent.getFields4view().toLongArray();
        long[] b = agent.getFields().toLongArray();
        for (Agent a: level){
            BitSet bt = BitSet.valueOf(b);
//            bt.andNot(a.getFields4view());
            bt.andNot(a.getFields());
            if (a.getPredictingValue() != null & agent.getPredictingValue() != null)
                bt.set(predictingFieldInvertedIndex, !a.getPredictingValue().toString().equals(agent.getPredictingValue().toString()));
            if (bt.cardinality() == 1) {
                a.getFields4view().set(bt.nextSetBit(0));
                if (predictingFieldInvertedIndex != -1)
                    if (bt.get(predictingFieldInvertedIndex))
                        a.setValues((int)a.getValues().nextValue(0));
            }
        }
    }

    private String sign(Agent a, Point point){
        BitSet b = (BitSet) a.getFields4view().clone();
        b.set(invertedIndex.getFieldIndex(point.getField()), true);
        return b.toString() + (point.getField().equals(predicttingField) ? point.getValue() : "");
    }


    private class PointIterator implements Iterator<Point>{
        Agent ag ;

        //int start = ag.getFields4view().isEmpty()? -1: ag.getFields4view().stream().max().getAsInt(), fi = start, vi = -1;
       int start = -1, fi = start, vi = -1;
        Tuple rec;// Tuple.copy(record, start, Integer.MAX_VALUE);

        private List<Object> values = null;
       // private BitSet bits;

        public PointIterator(Agent agent, Tuple record){
            ag = agent;
            rec = record;
            start =  ag.getFields4view().isEmpty()? -1: ag.getFields4view().stream().max().getAsInt();// using getFields4view() make the code much faster but that is mistaken
            fi = start; vi = -1;
/*            bits = BitSet.valueOf(ag.getFields4view().toLongArray());
            bits.andNot();*/
        }


        @Override
        public boolean hasNext() {
            int i = ag.getFields4view().nextClearBit(fi + 1);
            //int ifi = ((BitInvertedIndex)invertedIndex).invertedIndexToDatasetFieldIndex(i);
            if (i >= rec.size()) return false;

            if (values != null) {
                int j = (int)ag.getValues().nextAbsentValue(vi + 1);

                return (j < values.size()  | (i < invertedIndex.getFields().size()-1));
            }
            return i < invertedIndex.getFields().size();// | (valuesIterator != null ? valuesIterator.hasNext(): false);

        }

        @Override
        public Point next() {
            int ofi = fi;
            fi = ag.getFields4view().nextClearBit(fi + 1);
            if (fi >= invertedIndex.getFields().size()) return null;
            int ifi = ((BitInvertedIndex)invertedIndex).invertedIndexToDatasetFieldIndex(fi);

            if (ifi >= rec.size()) return null;

            String field = ((BitInvertedIndex) invertedIndex).getFieldsList().get(fi);
            if (rec.get(ifi) == null & values == null) {
                values =  invertedIndex.getAllValues(field);
            }

            if (values != null) {
                int j = (int)ag.getValues().nextAbsentValue(vi + 1);

                if (j < values.size()) {
                    fi = ofi;
                    vi = j;
                    return new Point(field, values.get(j));
                } else {
                    values = null;
                    vi = -1;
                    fi = ag.getFields4view().nextClearBit(fi + 1);
                    if (fi >= invertedIndex.getFields().size()) return null;
                    ifi = ((BitInvertedIndex) invertedIndex).invertedIndexToDatasetFieldIndex(fi);
                }
            }
            return new Point(field, rec.get(ifi).getValue());

        }

    }

    private PointIterator getAgentFieldsIterator(Agent agent, Tuple record){

        PointIterator iterator =  new PointIterator(agent, record);

        return iterator;
    }

}
