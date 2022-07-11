package cognitionmodel.models.inverted;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.util.*;
import java.util.function.Function;

public class RecursiveLevelValuesDecomposer implements Decomposer{

    private InvertedTabularModel model;
    private String fields[];
    private String predicttingField;
    private int predictingFieldIndex, predictingFieldInvertedIndex;
    private HashMap<String, Agent> agentMap = new HashMap<>();
    private boolean modelcashed;
    private Function<Agent, Boolean> agentFilter;
    private int maxDepth = 3;

    public RecursiveLevelValuesDecomposer(InvertedTabularModel model, String predictingField, boolean modelcashed) {
        this( model, predictingField, modelcashed, 3, null);

    }

    public RecursiveLevelValuesDecomposer(InvertedTabularModel model, String predictingField, boolean modelcashed, int maxDepth, Function<Agent, Boolean> agentFilter){

        this.maxDepth = maxDepth;
        this.modelcashed = modelcashed;
        this.model = model;
        this.predicttingField = predictingField;
        predictingFieldIndex = model.getDataSet().getFieldIndex(predictingField);
        predictingFieldInvertedIndex = ((BitInvertedIndex)model.getInvertedIndex()).dataSetFieldIndexToInvertedFieldIndex(predictingFieldIndex);

        if (modelcashed) agentMap = model.agentsindex;

        this.agentFilter = agentFilter;
    }

    public Function<Agent, Boolean> getAgentFilter() {
        return agentFilter;
    }

    private double gamma = 000000000.0; //
    private double mrdelta = -Double.MAX_VALUE;
    private double epsilon = 0.00; //probability of confidential interval
    private int sampleSize = 4000;

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield) {

        HashMap<Object, LinkedList<Agent>> r = new HashMap<>();
        HashMap<String, Agent> agentMap = new HashMap<>();
        TupleElement ote = record.set(predictingFieldIndex, null);

        LinkedList<Agent> al  = new LinkedList<>();

        al.add(new Agent((Point) null, model));
        doDecompose(al, record, agentMap, r);

        record.set(predictingFieldIndex, ote);
        return r;
    }

    private void doDecompose(LinkedList<Agent> agents, Tuple record, HashMap<String, Agent> agentMap, HashMap<Object,  LinkedList<Agent>> resultMap){

        LinkedList<Agent> newlevel  = new LinkedList<>(), badAgents = new LinkedList<>();

        for (Agent a: agents){
            for (Iterator<Point> pointIterator = getAgentFieldsIterator(a, record); pointIterator.hasNext();) {
                Point p = pointIterator.next();
              //  String s = sign(a, p);
              //  if (!agentMap.containsKey(s))
                {
                    Agent na = new Agent(p, model);
                    if (p.field.equals(predicttingField))
                        na.setPerdictingValue(p.getValue());

                    Agent ca = a.relation.size() == 0 ? na: Agent.merge(a, na, model);
                    if (!ca.records.isEmpty() ) {
                        if (agentFilter == null ? true: agentFilter.apply(ca)) {
                            newlevel.add(ca);
                        //    agentMap.put(s, ca);

                            Object co = ca.getPerdictingValue();
                            if (co == null) co = "null";

                            if (!resultMap.containsKey(co)) resultMap.put(co, new LinkedList<>());
                            resultMap.get(co).add(ca);
                        }
                    } else {
                        badAgents.add(ca);
                    }
                }
            }
        }

        for (Agent ba: badAgents)
            checkAgents(ba, newlevel);

        if (!newlevel.isEmpty())
            if (newlevel.peek().relation.size() < maxDepth)
                doDecompose(newlevel, record, agentMap, resultMap);

    }

    private void checkAgents(Agent agent, LinkedList<Agent> level){
        BitSet b = BitSet.valueOf(agent.getFields4view().toLongArray()); //b.set(model.getInvertedIndex().getFieldIndex(field));
        for (Agent a: level){
            BitSet bt = BitSet.valueOf(b.toLongArray());
            bt.andNot(a.getFields4view());
            if (a.getPerdictingValue() != null & agent.getPerdictingValue() != null)
                bt.set(predictingFieldInvertedIndex, !a.getPerdictingValue().toString().equals(agent.getPerdictingValue().toString()));
            if (bt.cardinality() == 1) {
                a.getFields4view().set(bt.nextSetBit(0));
                if (bt.get(predictingFieldInvertedIndex))
                    a.setValues((int)a.getValues().nextValue(0));
            }
        }
    }

    private String sign(Agent a, Point point){
        BitSet b = (BitSet) a.getFields4view().clone();
        b.set(model.getInvertedIndex().getFieldIndex(point.getField()), true);
        return b.toString() + (point.getField().equals(predicttingField) ? point.getValue() : "");
    }

    private Iterator<Point> getAgentFieldsIterator(Agent agent, Tuple record){

        return new Iterator<Point>() {
            Agent ag = agent;

            int start = ag.getFields4view().isEmpty()? -1: ag.getFields4view().stream().max().getAsInt(), fi = start, vi = -1;
            Tuple rec = record;// Tuple.copy(record, start, Integer.MAX_VALUE);

         //   private Iterator<Object> valuesIterator = null;
            private List<Object> values = null;
            @Override
            public boolean hasNext() {
                int i = ag.getFields4view().nextClearBit(fi + 1);

                if (values != null) {
                    int j = (int)ag.getValues().nextAbsentValue(vi + 1);

                    return (j < values.size()  | (i < model.getInvertedIndex().getFields().size()-1));
                }
                return i < model.getInvertedIndex().getFields().size();// | (valuesIterator != null ? valuesIterator.hasNext(): false);

            }

            @Override
            public Point next() {
                int ofi = fi;
                fi = ag.getFields4view().nextClearBit(fi + 1);
                if (fi >= model.getInvertedIndex().getFields().size()) return null;
                int ifi = ((BitInvertedIndex)model.getInvertedIndex()).invertedIndexToDatasetFieldIndex(fi);

                String field = model.getInvertedIndex().getFields().get(fi);
                if (rec.get(ifi) == null & values == null) {
                    values =  model.getInvertedIndex().getAllValues(field);
                    //valuesIterator = values.iterator();
                }

                if (values != null) {
                    int j = (int)ag.getValues().nextAbsentValue(vi + 1);

                    if (j < values.size()) {
                        fi = ofi;
                        vi = j;
                        return new Point(field, values.get(j));
                    } else {
                        //valuesIterator = null;
                        values = null;
                        vi = -1;
                        fi = ag.getFields4view().nextClearBit(fi + 1);
                        if (fi >= model.getInvertedIndex().getFields().size()) return null;
                        ifi = ((BitInvertedIndex) model.getInvertedIndex()).invertedIndexToDatasetFieldIndex(fi);
                    }
                }
                return new Point(field, rec.get(ifi).getValue());

            }
        };
    }


}
