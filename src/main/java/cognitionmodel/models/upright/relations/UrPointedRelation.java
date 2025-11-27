package cognitionmodel.models.upright.relations;

import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.patterns.FullGridIterativePatterns;
import cognitionmodel.patterns.Pattern;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrPointedRelation implements UrRelationInterface {


    private Function<UrAgent, List<UrAgent>> decompositionFunction;
    private Function<UrAgent, List<String>> generationFunction;

    private long datasetsize;
    private List<UrAgent>in;
    private int relationType = UrRelation.RELATION_POINTED;
    private String relationTypeStr;


    public UrPointedRelation(long datasetsize) {
        this.decompositionFunction = decompositionFunction;
        this.generationFunction = generationFunction;
        this.datasetsize = datasetsize;
        relationTypeStr = relationType+"";
    }

    @Override
    public List<UrAgent> makeDecomposition(UrAgent agent){
        UrAgent pointsAgent = agent;//.clone();

        return makeDecomposition(agent.getPoints(), agent.getStartPos());
    }

    @Override
    public List<String> makeGeneration(UrAgent agent){
        return agent.getPoints().stream().map(o->((UrPoint)o).toString()).toList();
    }

    @Override
    public List<UrAgent> makeDecomposition(List<UrPoint> points, long startPos) { //переделать в режим итератора чтобы на повторять то, что уже делал для предыдущего набора, а только инкрементировать, и это жэе тсправит ошибку с повтореыми учетами уже анйденных агентов

        if (points.isEmpty()) return List.of();
        HashMap<String, UrAgent> result = new HashMap<>();

        List<UrPoint> pl = new LinkedList<>();
        pl.addAll(points);

        Iterator<UrPoint> pointIterator = pl.listIterator();
        UrPoint p1 = pointIterator.next();
        while (pointIterator.hasNext()) {
            UrPoint p2 = pointIterator.next();
            if (p2.getPosition() - p1.getPosition() != p2.getTag() - p1.getTag()) {
                pointIterator.remove();
            }
        }

        int fp = pl.getFirst().getPosition();

        if (pl.size() > 1)
         for (Pattern p: new FullGridIterativePatterns(points.size(), points.size()).getPatterns()) {

            List<UrPoint> r = pl.stream().filter(pt->p.getBitSet().get(pt.getPosition()-fp)).toList();

            if (r.size() > 1) {
                UrAgent na = new UrAgent(r, 1, datasetsize, startPos);
                na.setRelation(relationType);
                result.compute(na.getAgentHash(), (k, v) -> v == null ? na : UrRelationInterface.addAgent(v, na));
            }
        }
        return result.values().stream().toList();

    }


    @Override
    public String makeHash(List<UrPoint> points) {
        return points.toString();
    }

    @Override
    public int getRelationType() {
        return relationType;
    }


    public String toString(){
        return relationTypeStr;
    }

}
