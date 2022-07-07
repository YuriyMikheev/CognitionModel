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
                String s = sign(a, p.getField());
                if (!agentMap.containsKey(s)){
                    Agent na = new Agent(p, model);

                    Agent ca = a.relation.size() == 0 ? na: Agent.merge(a, na, model);
                    if (!ca.records.isEmpty() ) {
                        if (agentFilter == null ? true: agentFilter.apply(ca)) {
                            newlevel.add(ca);
                            agentMap.put(s, ca);
                            //if (ca.getFields().get(p))
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
        BitSet b = BitSet.valueOf(agent.getFields().toLongArray()); //b.set(model.getInvertedIndex().getFieldIndex(field));
        for (Agent a: level){
            BitSet bt = BitSet.valueOf(b.toLongArray());
            bt.andNot(a.getFields());
            if (bt.cardinality() == 1)
                a.getFields().set(bt.nextSetBit(0));
        }
    }

    private String sign(Agent a, String field){
        BitSet b = (BitSet) a.getFields().clone();
        b.set(model.getInvertedIndex().getFieldIndex(field), true);
        return b.toString();
    }

    private Iterator<Point> getAgentFieldsIterator(Agent agent, Tuple record){

        return new Iterator<Point>() {

            Tuple rec = record;
            Agent ag = agent;
            int fi = ag.getFields().stream().max().getAsInt() , vi = -1;
            List<Object> values = null;

            @Override
            public boolean hasNext() {
                if (record.get(fi) != null) {
                    int n = ag.getFields().nextClearBit(fi + 1);
                    if (n == -1 || n >= rec.size()) return false;
                    return true;
                } else {
                    if (values == null) return false;
                        else
                            return vi < values.size() - 1;
                }
            }

            @Override
            public Point next() {
                int ifi = ((BitInvertedIndex)model.getInvertedIndex()).invertedIndexToDatasetFieldIndex(fi);

                if (record.get(ifi) != null) {
                    int n = ag.getFields().nextClearBit(fi + 1);
                    if (n == -1 || n >= rec.size()) return null;
                    fi = n;
                    ifi = ((BitInvertedIndex)model.getInvertedIndex()).invertedIndexToDatasetFieldIndex(fi);
                    if (rec.get(ifi) != null)
                        return new Point(model.getDataSet().getHeader().get(ifi).getValue().toString(), rec.get(ifi).getValue());
                }

                String field = model.getInvertedIndex().getFields().get(fi);

                if (values == null) {
                    values = model.getInvertedIndex().getAllValues(field);
                    vi = -1;
                }
                vi++;

                if (vi  <  values.size()){
                    return new Point(field, values.get(vi));
                }

                return null;
            }
        };
    }


}
