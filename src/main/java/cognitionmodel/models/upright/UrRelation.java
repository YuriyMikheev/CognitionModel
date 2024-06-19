package cognitionmodel.models.upright;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrRelation {


    private Function<List<UrPoint>, List<UrAgent>> transformFunction;


    public  static final int RELATION_POINTED = 1;
    public  static final int RELATION_SIMILARITY = 2;
    public  static final int RELATION_ORDER = 3;
    private long datasetsize;
    private List<UrAgent>in;


    public UrRelation(Function<List<UrPoint>, List<UrAgent>> transformFunction, List<UrAgent> in, long datasetsize) {
        this.transformFunction = transformFunction;
        this.datasetsize = datasetsize;
        this.in = in;
    }

    public UrRelation(int transformationType, List<UrAgent> in, long datasetsize) {
        this(null, in, datasetsize);

        if (transformationType == RELATION_POINTED)
            this.transformFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();

                    for (UrPoint p1 : points) {
                        UrAgent na = new UrAgent(p1, 1, datasetsize);
                        for (UrPoint p2 : points) {

/*
                        if (idxPoint.getPosition() - a.getPoints().getLast().getPosition() == ti - a.getIdx().last())
                            if (in.get((int) (idxPoint.getPosition() - ti + a.getIdx().last())) == a.getPoints().getLast().getToken())
*/
                            //if (p2.getPosition() - p1.getPosition() == )
                        }

                        result.compute(na.getAgentHash(), (k, v) -> v == null ? na : addAgent(v, na));
                    }


                    return result.values().stream().toList();
                }
            };




        if (transformationType == RELATION_SIMILARITY)
            this.transformFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();

                    UrAgent na = new UrAgent(points, 1, datasetsize);

                    na.setAgentHash(points.stream().map(p -> p.getToken()).sorted().collect(Collectors.toList()).toString());

                    result.compute(na.getAgentHash(), (k, v) -> v == null? na: addAgent(v, na));

                    return result.values().stream().toList();
                }
            };


        if (transformationType == RELATION_ORDER)
            this.transformFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();

                    UrAgent na = new UrAgent(points, 1, datasetsize);

                    na.setAgentHash(points.stream().map(p -> p.getToken()).collect(Collectors.toList()).toString());

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


    public Function<List<UrPoint>, List<UrAgent>> getTransformFunction() {
        return transformFunction;
    }

    public List<UrAgent> apply(List<UrPoint> points){
        if (transformFunction != null)
            return transformFunction.apply(points);
        return null;
    }

}
