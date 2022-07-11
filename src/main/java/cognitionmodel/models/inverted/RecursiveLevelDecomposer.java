package cognitionmodel.models.inverted;

import cognitionmodel.datasets.Tuple;

import java.util.*;
import java.util.function.Function;

public class RecursiveLevelDecomposer implements Decomposer{

    private InvertedTabularModel model;
    private String fields[];
    private String predicttingField;
    private int predictingFieldIndex, predictingFieldInvertedIndex;
    private HashMap<String, Agent> agentMap = new HashMap<>();
    private boolean modelcashed;
    private Function<Agent, Boolean> agentFilter;
    private int maxDepth = 3;

    public RecursiveLevelDecomposer(InvertedTabularModel model, String predictingField, boolean modelcashed) {
        this( model, predictingField, modelcashed, 3, null);

    }

    public RecursiveLevelDecomposer(InvertedTabularModel model, String predictingField, boolean modelcashed, int maxDepth, Function<Agent, Boolean> agentFilter){

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

        LinkedList<Agent> al  = new LinkedList<>();

/*
        for (int i = 0; i < record.size(); i++){
            if (model.getEnabledFields()[i] != 0 & i != predictingFieldIndex){
                Agent a = new Agent(new Point(model.getDataSet().getHeader().get(i).getValue().toString(), record.get(i).getValue()), model);
                if (a.getRecords() != null)
                    al.add(a);
            }
        }
*/

        al.add(new Agent((Point) null, model)); //al.get(0).getFields().set(predictingFieldInvertedIndex, true);
        LinkedList<Agent> agents = doDecompose(al, record, agentMap, new int[model.getInvertedIndex().getFieldsAmount()]);
        agents.pollFirst();
        r.put("", agents);
/*
        for (Object pv: model.getInvertedIndex().getAllValues(predictingfield)) {
            if (!modelcashed) agentMap = new HashMap<>();
            LinkedList<Agent> al  = new LinkedList<>(); al.add(new Agent(new Point(predictingfield, pv), model)); al.get(0).getFields().set(predictingFieldInvertedIndex, false);
            LinkedList<Agent> agents = doDecompose(al, record, agentMap, new int[model.getInvertedIndex().getFieldsAmount()]);

            agents.pollFirst();
            r.put(pv, agents);
        }
*/

        return r;
    }

    private LinkedList<Agent> doDecompose(LinkedList<Agent> agents, Tuple record, HashMap<String, Agent> agentMap, int[] pf){

        LinkedList<Agent> newlevel  = new LinkedList<>(), badAgents = new LinkedList<>();

        for (Agent a: agents){
            int ifi = a.getFields4view().isEmpty()? -1: a.getFields4view().stream().max().getAsInt();
            while ((ifi = a.getFields4view().nextClearBit(ifi+1)) != -1 & ifi < model.getInvertedIndex().getFieldsAmount()) {
                if (ifi == predictingFieldInvertedIndex) continue;
                int fi = ((BitInvertedIndex)model.getInvertedIndex()).invertedIndexToDatasetFieldIndex(ifi);
                String field = model.getDataSet().getHeader().get(fi).getValue().toString();
                Point p = new Point(field, record.get(fi).getValue());

                String s = sign(a, p.getField());
                if (!agentMap.containsKey(s)){
                    Agent na = new Agent(p, model);

                    Agent ca = a.relation.size() == 0 ? na: Agent.merge(a, na, model);
                    if (!ca.records.isEmpty() ) {
                        if (agentFilter == null ? true: agentFilter.apply(ca)) {
                            newlevel.add(ca);
                            agentMap.put(s, ca);
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
                doDecompose(newlevel, record, agentMap, pf);

        agents.addAll(newlevel);
        return agents;
    }

    private void checkAgents(Agent agent, LinkedList<Agent> level){
        BitSet b = BitSet.valueOf(agent.getFields4view().toLongArray()); //b.set(model.getInvertedIndex().getFieldIndex(field));
        for (Agent a: level){
            BitSet bt = BitSet.valueOf(b.toLongArray());
            bt.andNot(a.getFields4view());
            if (bt.cardinality() == 1)
                a.getFields4view().set(bt.nextSetBit(0));
        }
    }

    private String sign(Agent a, String field){
        BitSet b = (BitSet) a.getFields4view().clone();
        b.set(model.getInvertedIndex().getFieldIndex(field), true);
        return b.toString();
    }

}
