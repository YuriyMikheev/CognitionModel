package cognitionmodel.models.inverted;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.min;

public class RecursiveLevelValuesDecomposer implements Decomposer{

    private InvertedIndex invertedIndex;
    private String fields[];
    private String predicttingField;
    private int predictingFieldIndex, predictingFieldInvertedIndex;
    private HashMap<String, Agent> agentMap = new HashMap<>();

    private Function<Agent, Boolean> agentFilter;
    private int maxDepth = 3;
    private HashSet<Point> desabledPoints = new HashSet<>();

    public RecursiveLevelValuesDecomposer(InvertedIndex invertedIndex, String predictingField, boolean modelcashed) {
        this( invertedIndex, predictingField, modelcashed, 3, null);

    }

    public RecursiveLevelValuesDecomposer(InvertedIndex invertedIndex, String predictingField, boolean modelcashed, int maxDepth, Function<Agent, Boolean> agentFilter){

        this.maxDepth = maxDepth;
        this.invertedIndex = invertedIndex;
        this.predicttingField = predictingField;
        predictingFieldInvertedIndex = invertedIndex.getFieldIndex(predictingField);
        predictingFieldIndex = ((BitInvertedIndex)invertedIndex).invertedIndexToDatasetFieldIndex(predictingFieldInvertedIndex);

        this.agentFilter = agentFilter;
    }

    public HashSet<Point> getDesabledPoints() {
        return desabledPoints;
    }

    public void setDesabledPoints(HashSet<Point> desabledPoints) {
        this.desabledPoints = desabledPoints;
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
        HashMap<String, Agent> agentMap = new HashMap<>();
        TupleElement ote = record.set(predictingFieldIndex, null);

        LinkedList<Agent> al  = new LinkedList<>();

        al.add(new Agent((Point) null, invertedIndex));
        doDecompose(al, record, agentMap, r);
        HashMap<Object, LinkedList<Agent>> nr = new HashMap<>();

        record.set(predictingFieldIndex, ote);
        return r;
    }

    private void doDecompose(LinkedList<Agent> agents, Tuple record, HashMap<String, Agent> agentMap, HashMap<Object,  LinkedList<Agent>> resultMap){

        LinkedList<Agent> newlevel  = new LinkedList<>(), badAgents = new LinkedList<>();

        for (Agent a: agents){
            for (Iterator<Point> pointIterator = getAgentFieldsIterator(a, record); pointIterator.hasNext();) {
                Point p = pointIterator.next();

                Agent na = new Agent(p, invertedIndex);
                if (p.field.equals(predicttingField))
                    na.setPerdictingValue(p.getValue());

                Agent ca = a.relation.size() == 0 ? na: Agent.merge(a, na, invertedIndex);
             //   if (ca.records.getCardinality() > 5 ) {
                if (!ca.records.isEmpty() ) {
                    if (agentFilter == null || agentFilter.apply(ca) || ca.relation.size() == 1) {
                        newlevel.add(ca);
                        Object co = ca.getPerdictingValue();
                        if (co == null) co = "null";
                        if (!resultMap.containsKey(co)) resultMap.put(co, new LinkedList<>());
                        if ( ca.relation.size() > 1 || co.equals("null")) resultMap.get(co).add(ca);
                    }
                } else {
                    badAgents.add(ca);
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
        b.set(invertedIndex.getFieldIndex(point.getField()), true);
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

                String field = invertedIndex.getFields().get(fi);
                if (rec.get(ifi) == null & values == null) {
                    values =  invertedIndex.getAllValues(field);
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
                        if (fi >= invertedIndex.getFields().size()) return null;
                        ifi = ((BitInvertedIndex) invertedIndex).invertedIndexToDatasetFieldIndex(fi);
                    }
                }
                return new Point(field, rec.get(ifi).getValue());

            }
        };
    }

    private class Node implements Cloneable {
        LinkedList<Agent> agents = new LinkedList<>();
        double mr = 0;
        private BitSet fields = new BitSet();
        private BitSet havagents = new BitSet();

        public Node(){

        }
        public Node(Agent agent) {
            addAgent(agent);
        }

        private void addAgent(Agent agent){
            agents.add(agent);
            mr += agent.getMR();
            fields.or(agent.getFields());
            fields.clear(predictingFieldInvertedIndex);

        }

        public double getMr() {
            return mr;
        }

        public boolean canAdd(Agent agent){
            boolean y = agent.getFields().get(predictingFieldInvertedIndex) ? agents.size() > 0 ? agent.getPerdictingValue().equals(agents.get(0).getPerdictingValue()): true: true;
            return !fields.intersects(agent.getFields()) & y;
        }

        public Node clone(){
            Node node = new Node();
            for (Agent a: agents)
                node.addAgent(a);

            node.havagents = (BitSet) havagents.clone();
            return node;
        }

        public String toString(){
            return mr+"\t"+agents;
        }

    }

    private LinkedList<Agent> minimize(LinkedList<Agent> agents){

        agents.sort(Comparator.comparing(Agent::getMR, Comparator.reverseOrder()));

        LinkedList<Agent> nagents = new LinkedList<>();

        for (int i = 0; i < min(5, agents.size()); i++)
            nagents.add(agents.poll());


        return nagents;
    }

    private LinkedList<Agent> minimize1(LinkedList<Agent> agents){

        agents.sort(Comparator.comparing(Agent::getMR, Comparator.reverseOrder()));

        Node node = new Node();


        for (Iterator<Agent> agentIterator = agents.listIterator(); node.agents.size() <  agents.size() & agentIterator.hasNext();){
            Agent a = agentIterator.next();
            if (node.canAdd(a) & a.relation.size() > 1) node.addAgent(a);
        }

        return node.agents;
    }

}
