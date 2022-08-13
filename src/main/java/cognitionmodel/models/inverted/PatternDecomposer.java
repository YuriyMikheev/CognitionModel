package cognitionmodel.models.inverted;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.LinkedPatternSet;
import cognitionmodel.patterns.Pattern;


import java.util.*;
import java.util.function.Function;

import static java.lang.Math.round;

public class PatternDecomposer implements Decomposer{

    private LinkedPatternSet patternSet;
    private InvertedTabularModel model;
    private String fields[];
    private String predicttingField;
    private int predictingFieldIndex;
    private HashMap<String, Agent> agentMap = new HashMap<>();
    private boolean modelcashed;
    private int maxCashed = 70;
    private Function<Agent, Boolean> agentFilter;

    public PatternDecomposer(List<Pattern> patterns, InvertedTabularModel model, String predictingField, boolean modelcashed) {
        this(patterns, model, predictingField, modelcashed, null);

    }

    public PatternDecomposer(List<Pattern> patterns, InvertedTabularModel model, String predictingField, boolean modelcashed, Function<Agent, Boolean> agentFilter){

        this.modelcashed = modelcashed;
        this.model = model;
        this.predicttingField = predictingField;
        predictingFieldIndex = model.getDataSet().getFieldIndex(predictingField);
        
        if (modelcashed) agentMap = model.agentsindex;

        LinkedList<Pattern> np = new LinkedList<>();
        for (Pattern p: patterns)
            if (p.getBitSet().get(predictingFieldIndex))
                np.add(p);



        this.patternSet = new LinkedPatternSet(patterns);
        this.agentFilter = agentFilter;
    }

    public int getMaxCashed() {
        return maxCashed;
    }

    public void setMaxCashed(int maxCashed) {
        this.maxCashed = maxCashed;
    }

    public List<Pattern> getPatterns() {
        return patternSet.getPatterns();
    }

    private double gamma = 000000000.0; //
    private double mrdelta = -Double.MAX_VALUE;
    private double epsilon = 0.00; //probability of confidential interval

    @Override
    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield) {

        HashMap<String, Point> pl = new HashMap<>();

        int j = 0;
        for (TupleElement tupleElement : record) {
            if (model.getEnabledFields()[j] == 1 & j != predictingFieldIndex) {
                String field = model.getDataSet().getHeader().get(j).getValue().toString();
                pl.put(field, new Point(field, tupleElement.getValue()));
            }
            j++;
        }

        for (Object pv: model.getInvertedIndex().getAllValues(predictingfield)) {
            Point p = new Point(predictingfield, pv);
            pl.put(p.toString(), p);
        }

        HashMap<Object, LinkedList<Agent>> r = new HashMap<>();

        List<Object> values  =  model.getInvertedIndex().getAllValues(predictingfield);
        values.add("null");

        for (Object pv: values) {
            patternSet.reactivate();
            if (!modelcashed) agentMap = new HashMap<>();
            LinkedList<Agent> agents = new LinkedList<>();

            boolean isNull = (pv.toString().equals("null"));

            Iterator<Pattern> it = patternSet.iterator();
            while (it.hasNext()) {
                Pattern pattern = it.next();

                j = 0;
                Point points[] = new Point[pattern.getSetAmount()];
                for (int i: pattern.getSet()){
                    if (i != predictingFieldIndex)
                        points[j++] = pl.get(model.getDataSet().getHeader().get(i).getValue().toString());
                }

                points[points.length - 1] = pl.get(predictingfield + ":" + pv);

                Agent a = null;

                if (points.length > 1) {
                    TreeSet<String> treeSet = new TreeSet<>();

                    treeSet.add(points[points.length - 1].toString());
                    for (int i = 0; i < points.length / 2; i++)
                        treeSet.add(points[i].toString());

                    Agent a1 = agentMap.get(treeSet.toString());

                    if (a1 != null) {
                        TreeSet<String> treeSet1 = new TreeSet<>();
                        treeSet1.add(points[points.length - 1].toString());

                        for (int i = points.length / 2; i < points.length - 1; i++) {
                            treeSet1.add(points[i].toString());
                            treeSet.add(points[i].toString());
                        }

                       if (agentMap.containsKey(treeSet.toString()))
                           a = agentMap.get(treeSet.toString());
                       else {
                            Agent a2 = agentMap.get(treeSet1.toString());
                            if (a2 != null) {
                                a = Agent.merge(a1, a2, model.getInvertedIndex());
                                a.setPerdictingField(true);
                            }
                       }
                    }
                } else {
                    TreeSet<String> treeSet = new TreeSet<>();

                    for (int i = 0; i < points.length; i++)
                        treeSet.add(points[i].toString());

                    a = agentMap.get(treeSet.toString());
                }


                if (a == null) {
                    a = new Agent(points, model.getInvertedIndex());
                    a.setPerdictingField(true);
                }


                if (a.records.isEmpty()){// | !(agentFilter == null ? true: agentFilter.apply(a))) {//agent checking
                    patternSet.setInActive(pattern, true);
                } else {
                    if (agentFilter == null ? true: agentFilter.apply(a)) {
                        agents.add(a);
                        if (a.relation.size() < maxCashed)
                            agentMap.put(a.getSignature(), a);
                    }
                }

            }
            r.put(pv, agents);
        }


        return r;
    }
}
