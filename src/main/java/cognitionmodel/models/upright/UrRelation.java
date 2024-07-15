package cognitionmodel.models.upright;

import cognitionmodel.models.relations.Relation;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrRelation {


    private Function<List<UrPoint>, List<UrAgent>> decompositionFunction;
    private BiFunction<Integer, UrAgent, List<Integer>> compositionFunction;

    public  static final int RELATION_UNDEFINED = 0;
    public  static final int RELATION_POINTED = 1;
    public  static final int RELATION_SIMILARITY = 2;
    public  static final int RELATION_ORDER = 3;
    public  static final int RELATION_ORDER_FULL = 4;
    private long datasetsize;
    private List<UrAgent>in;


    public UrRelation(Function<List<UrPoint>, List<UrAgent>> decompositionFunction,  BiFunction<Integer, UrAgent, List<Integer>> compositionFunction, long datasetsize) {
        this.decompositionFunction = decompositionFunction;
        this.compositionFunction = compositionFunction;
        this.datasetsize = datasetsize;
    }

    public UrRelation(int relationType, long datasetsize) {
        this(null, null, datasetsize);

        if (relationType == RELATION_POINTED) {
            this.decompositionFunction = points -> {

                HashMap<String, UrAgent> result = new HashMap<>();

                while (!points.isEmpty()) {
                    Iterator<UrPoint> pointIterator = points.listIterator();
                    UrPoint p1 = pointIterator.next();
                    pointIterator.remove();
                    UrAgent na = new UrAgent(p1, 1, datasetsize);
                    na.setRelation(relationType);
                    while (pointIterator.hasNext()) {
                        UrPoint p2 = pointIterator.next();
                        if (p2.getPosition() - p1.getPosition() == p2.getTag() - p1.getTag()) {
                            na.addPoint(p2);
                            pointIterator.remove();
                        }
                    }
                    result.compute(na.getAgentHash(), (k, v) -> v == null ? na : addAgent(v, na));
                }

                return result.values().stream().toList();
            };

            this.compositionFunction = (i, agent) -> {
                LinkedList<Integer> list = new LinkedList<>();
                list.add(i - agent.getFirstPos());
                return list;
            };
        }

        if (relationType == RELATION_ORDER_FULL) {
            this.decompositionFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();
                    points = new LinkedList<>(points);

                    while (!points.isEmpty()) {
                        Iterator<UrPoint> pointIterator = points.listIterator();
                        UrPoint p1 = pointIterator.next();
                        pointIterator.remove();
                        UrAgent na = new UrAgent(p1, 1, datasetsize);
                        na.setRelation(relationType);
                        while (pointIterator.hasNext()) {
                            UrPoint p2 = pointIterator.next();
                            if (p2.getPosition() - p1.getPosition() == p2.getTag() - p1.getTag()) {
                                na.addPoint(p2);
                                pointIterator.remove();
                            }
                        }
                        result.compute(na.getAgentHash(), (k, v) -> v == null ? na : addAgent(v, na));
                    }

                    return result.values().stream().toList();
                }
            };

/*            this.compositionFunction = (i, agent) -> {
                LinkedList<Integer> list = new LinkedList<>();
                list.add(i - agent.getFirstPos());
                return list;
            };*/
        }

        if (relationType == RELATION_SIMILARITY) {
            this.decompositionFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();
                    UrAgent na = new UrAgent(points, 1, datasetsize);
                    na.setRelation(relationType);
                    na.setAgentHash(points.stream().map(p -> ((UrAgent) p.getToken()).getTokens().toString()).sorted().collect(Collectors.toList()).toString());
                    result.compute(na.getAgentHash(), (k, v) -> v == null ? na : addAgent(v, na));

                    return result.values().stream().toList();
                }
            };

            this.compositionFunction = (i, agent) -> {
                LinkedList<Integer> list = new LinkedList<>();
                int l = agent.getPoints().getLast().getPosition() - agent.getFirstPos();
                for (int j = agent.getFirstPos(); j < agent.getPoints().getLast().getPosition(); j++) {
                    list.add(i + j - l/2);
                }
                return list;
            };
        }

        if (relationType == RELATION_ORDER)
            this.decompositionFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();
                    UrAgent na = new UrAgent(points, 1, datasetsize); na.setRelation(relationType);
                    na.setAgentHash(points.stream().map(p -> ((UrAgent)p.getToken()).getTokens().toString()).collect(Collectors.toList()).toString());
                    result.compute(na.getAgentHash(), (k, v) -> v == null? na: addAgent(v, na));

                    return result.values().stream().toList();
                }
            };
    }

    private UrAgent addAgent(UrAgent agent1, UrAgent agent2){
        agent1.incF(agent2.getF());
        agent1.getIdx().or(agent2.getIdx());
        return agent1;
    }


    public Function<List<UrPoint>, List<UrAgent>> getDecompositionFunction() {
        return decompositionFunction;
    }

    public List<UrAgent> applyDecomposition(List<UrPoint> points){
        if (decompositionFunction != null)
            return decompositionFunction.apply(points);
        return null;
    }

    public List<Integer> applyComposition(int i, UrAgent agent){
        if (compositionFunction != null)
            return compositionFunction.apply(i, agent);
        return null;
    }

    public static UrRelation[] allRelations(long datasetsize){
        UrRelation[] relations = new UrRelation[4];
        for (int i = 0; i < 4; i++) {
            relations[i] = new UrRelation(i, datasetsize);
        }
        return relations;
    }

}
