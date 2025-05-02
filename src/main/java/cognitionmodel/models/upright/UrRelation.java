package cognitionmodel.models.upright;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrRelation {


    private Function<UrAgent, List<UrAgent>> decompositionFunction;
    private Function<UrAgent, List<String>> generationFunction;

    public  static final int RELATION_UNDEFINED = 0;
    public  static final int RELATION_POINTED = 1;
    public  static final int RELATION_SIMILARITY = 2;
    public  static final int RELATION_ORDER = 3;
    public  static final int RELATION_ORDER_FULL = 4;
    private long datasetsize;
    private List<UrAgent>in;
    private int relationType;
    private String relationTypeStr;


    public UrRelation(int relationType, long datasetsize) {
        this.decompositionFunction = decompositionFunction;
        this.generationFunction = generationFunction;
        this.datasetsize = datasetsize;
        this.relationType = relationType;
        relationTypeStr = relationType+"";

        if (relationType == RELATION_POINTED) {
            this.decompositionFunction = pointsAgent -> {

                HashMap<String, UrAgent> result = new HashMap<>();
                List<UrPoint> points = pointsAgent.getPoints();

                while (!points.isEmpty()) {
                    Iterator<UrPoint> pointIterator = points.listIterator();
                    UrPoint p1 = pointIterator.next();
                    pointIterator.remove();
                    UrAgent na = new UrAgent(p1, 1, datasetsize, pointsAgent.getStartpos()+p1.getPosition());
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

            this.generationFunction = (agent) -> {
                return agent.getPoints().stream().map(o->((UrPoint)o).toString()).toList();
            };
        }

        if (relationType == RELATION_ORDER_FULL) {
            this.decompositionFunction = pointsAgent -> {


                    HashMap<String, UrAgent> result = new HashMap<>();
                    List<UrPoint> points = pointsAgent.getPoints();

                    while (!points.isEmpty()) {
                        Iterator<UrPoint> pointIterator = points.listIterator();
                        UrPoint p1 = pointIterator.next();
                        pointIterator.remove();
                        UrAgent na = new UrAgent(p1, 1, datasetsize, pointsAgent.getStartpos()+p1.getPosition());
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

/*            this.compositionFunction = (i, agent) -> {
                LinkedList<Integer> list = new LinkedList<>();
                list.add(i - agent.getFirstPos());
                return list;
            };*/
        }

        if (relationType == RELATION_SIMILARITY) {
            this.decompositionFunction = pointsAgent -> {

                List<UrPoint> points = pointsAgent.getPoints();

                HashMap<String, UrAgent> result = new HashMap<>();
                UrAgent na = new UrAgent(points, 1, datasetsize, pointsAgent.getStartpos());
                na.setRelation(relationType);
                na.setAgentHash(points.stream().map(p -> ((UrAgent) p.getToken()).getTokens().toString()).sorted().collect(Collectors.toList()).toString());
                result.compute(na.getAgentHash(), (k, v) -> v == null ? na : addAgent(v, na));

                return result.values().stream().toList();

            };

            this.generationFunction = (agent) -> {
                return agent.getPoints().stream().map(o->((UrPoint)o).getToken().toString()).sorted().toList();
            };
        }

        if (relationType == RELATION_ORDER) {
            this.decompositionFunction = pointsAgent -> {
                List<UrPoint> points = pointsAgent.getPoints();

                HashMap<String, UrAgent> result = new HashMap<>();
                UrAgent na = new UrAgent(points, 1, datasetsize, pointsAgent.getStartpos());
                na.setRelation(relationType);
                na.setAgentHash(points.stream().map(p -> ((UrAgent) p.getToken()).getTokens().toString()).collect(Collectors.toList()).toString());
                result.compute(na.getAgentHash(), (k, v) -> v == null ? na : addAgent(v, na));

                return result.values().stream().toList();
            };

            this.generationFunction = (agent) -> {
                return agent.getPoints().stream().map(o->((UrPoint)o).getToken().toString()).toList();
            };
        }
    }

    @Contract("_, _ -> param1")
    private @NotNull UrAgent addAgent(@NotNull UrAgent agent1, @NotNull UrAgent agent2){
        agent1.incF(agent2.getF());
        agent1.getIdx().or(agent2.getIdx());
        return agent1;
    }


    public Function<UrAgent, List<UrAgent>> getDecompositionFunction() {
        return decompositionFunction;
    }

    public List<UrAgent> applyDecomposition(UrAgent pointsAgent){
        if (decompositionFunction != null)
            return decompositionFunction.apply(pointsAgent);
        return null;
    }

    public List<String> applyGeneration(UrAgent agent){
        if (generationFunction != null)
            return generationFunction.apply(agent);
        return null;
    }

    @Contract(pure = true)
    public static UrRelation @NotNull [] allRelations(long datasetsize){
        UrRelation[] relations = new UrRelation[4];
        for (int i = 0; i < 4; i++) {
            relations[i] = new UrRelation(i, datasetsize);
        }
        return relations;
    }

    public String toString(){
        return relationTypeStr;
    }

}
