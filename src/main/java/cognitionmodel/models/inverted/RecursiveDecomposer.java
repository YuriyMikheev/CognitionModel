package cognitionmodel.models.inverted;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.LinkedPatternSet;
import cognitionmodel.patterns.Pattern;

import java.util.*;
import java.util.function.Function;

public class RecursiveDecomposer implements Decomposer{

    private InvertedTabularModel model;
    private String fields[];
    private String predicttingField;
    private int predictingFieldIndex;
    private HashMap<String, Agent> agentMap = new HashMap<>();
    private boolean modelcashed;
    private Function<Agent, Boolean> agentFilter;
    private int maxDepth = 3;
    private HashMap<String, LinkedList<Pattern>> badPatterns = new HashMap<>();

    public RecursiveDecomposer( InvertedTabularModel model, String predictingField, boolean modelcashed) {
        this( model, predictingField, modelcashed, 3, null);

    }

    public RecursiveDecomposer( InvertedTabularModel model, String predictingField, boolean modelcashed, int maxDepth, Function<Agent, Boolean> agentFilter){

        this.maxDepth = maxDepth;
        this.modelcashed = modelcashed;
        this.model = model;
        this.predicttingField = predictingField;
        predictingFieldIndex = model.getDataSet().getFieldIndex(predictingField);

        if (modelcashed) agentMap = model.agentsindex;

        this.agentFilter = agentFilter;
    }

    private double gamma = 000000000.0; //
    private double mrdelta = -Double.MAX_VALUE;
    private double epsilon = 0.00; //probability of confidential interval


    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield) {

        HashMap<Object, LinkedList<Agent>> r = new HashMap<>();


        Point points[] = new Point[record.size()];
        int j = 0;
        for (int i = 0; i < record.size(); i++){
            if (model.getEnabledFields()[i] == 1 & i != predictingFieldIndex)
                points[j++] = new Point(model.getDataSet().getHeader().get(i).getValue().toString(), record.get(i).getValue());
        }

        points = Arrays.copyOf(points, j);

        for (Object pv: model.getInvertedIndex().getAllValues(predictingfield)) {
            if (!modelcashed) agentMap = new HashMap<>();
            //points[j] = new Point(predictingfield, pv);
            badPatterns.clear();
            LinkedList<Agent> agents = doDecompose(new LinkedList<>(), new Agent(new Point(predictingfield, pv), model), points, 0, new byte[points.length]);

            r.put(pv, agents);
        }

        return r;
    }

    private LinkedList<Agent> doDecompose(LinkedList<Agent> agents, Agent actualAgent, Point[] points, int start, byte[] actualfields){
        if (actualAgent != null)
            if (agentFilter == null ? true: agentFilter.apply(actualAgent))  agents.add(actualAgent);

        if (actualAgent.relation.size() < maxDepth)
            for (int i = start; i < points.length; i++) {
                actualfields[i] = 1;
               // if (check(points[i], actualfields))
                {
                    Agent agent = Agent.merge(new Agent(points[i], model), actualAgent, model);
                    if (!agent.records.isEmpty()) doDecompose(agents, agent, points, i + 1, actualfields.clone());
/*                        else
                            addBadPattern(agent, actualfields);*/
                }
                actualfields[i] = 0;
            }
        return agents;
    }

    private void addBadPattern(Agent agent, byte[] actualfields){
        for (String pointstring: agent.relation.keySet()) {
            LinkedList<Pattern> bp;
            if (!badPatterns.containsKey(pointstring)) badPatterns.put(pointstring, bp = new LinkedList<>());
                else bp = badPatterns.get(pointstring);
            bp.add(new Pattern(actualfields));
        }
    }

    private boolean check(Point point, byte[] actualfields){
        if (!badPatterns.containsKey(point.toString())) return true;

        Pattern cp = new Pattern(actualfields);
        for (Pattern p: badPatterns.get(point.toString())){
            BitSet bp = BitSet.valueOf(p.getBitSet().toLongArray());
            bp.and(cp.getBitSet());
            if (bp.cardinality() == p.getBitSet().cardinality())
                return false;
        }

        return true;
    }

}
