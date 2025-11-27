package cognitionmodel.models.upright.relations;

import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.patterns.FullGridIterativePatterns;
import cognitionmodel.patterns.Pattern;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrOrderRelation implements UrRelationInterface {


    private Function<UrAgent, List<UrAgent>> decompositionFunction;
    private Function<UrAgent, List<String>> generationFunction;

    private long datasetsize;
    private List<UrAgent>in;
    private int relationType = UrRelation.RELATION_ORDER;
    private String relationTypeStr;


    public UrOrderRelation(long datasetsize) {
        this.decompositionFunction = decompositionFunction;
        this.generationFunction = generationFunction;
        this.datasetsize = datasetsize;
        relationTypeStr = relationType+"";
    }

    @Override
    public List<UrAgent> makeDecomposition(UrAgent pointsAgent){
        List<UrPoint> points = pointsAgent.getPoints();

        return makeDecomposition(points, pointsAgent.getStartPos());

    }

    @Override
    public List<String> makeGeneration(UrAgent agent){
        return agent.getPoints().stream().map(o->((UrPoint)o).getToken().toString()).toList();
    }

    @Override
    public List<UrAgent> makeDecomposition(List<UrPoint> points, long startPos) {
        HashMap<String, UrAgent> result = new HashMap<>();

        int fp = points.getFirst().getPosition();

        for (Pattern p: new FullGridIterativePatterns(points.size(), points.size()).getPatterns()) {

            List<UrPoint> r = points.stream().filter(pt -> p.getBitSet().get(pt.getPosition() - fp)).toList();

            if (!r.isEmpty()) {

                UrAgent na = new UrAgent(r, 1, datasetsize, startPos);
                na.setRelation(relationType);
                na.setAgentHash(makeHash(na.getPoints()));
                result.compute(na.getAgentHash(), (k, v) -> v == null ? na : UrRelationInterface.addAgent(v, na));
            }
        }

        return result.values().stream().toList();
    }

    @Override
    public String makeHash(List<UrPoint> points) {
        return points.stream().map(p -> ((UrAgent) p.getToken()).getStringTokens().toString()).collect(Collectors.toList()).toString();
    }

    @Override
    public int getRelationType() {
        return relationType;
    }

    public String toString(){
        return relationTypeStr;
    }

}
