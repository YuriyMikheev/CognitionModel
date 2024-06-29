package cognitionmodel.models.upright;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrRelation {


    private Function<List<UrPoint>, List<UrAgent>> transformFunction;

    public  static final int RELATION_UNDEFINED = 0;
    public  static final int RELATION_POINTED = 1;
    public  static final int RELATION_SIMILARITY = 2;
    public  static final int RELATION_ORDER = 3;
    public  static final int RELATION_ORDER_FULL = 4;
    private long datasetsize;
    private List<UrAgent>in;


    public UrRelation(Function<List<UrPoint>, List<UrAgent>> transformFunction, long datasetsize) {
        this.transformFunction = transformFunction;
        this.datasetsize = datasetsize;
    }

    public UrRelation(int transformationType, long datasetsize) {
        this(null, datasetsize);

        if (transformationType == RELATION_POINTED)
            this.transformFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();

                    while (!points.isEmpty()) {
                        Iterator<UrPoint> pointIterator = points.listIterator();
                        UrPoint p1 = pointIterator.next(); pointIterator.remove();
                        UrAgent na = new UrAgent(p1, 1, datasetsize); na.setRelation(transformationType);
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

        if (transformationType == RELATION_ORDER_FULL)
            this.transformFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();

                    while (!points.isEmpty()) {
                        Iterator<UrPoint> pointIterator = points.listIterator();
                        UrPoint p1 = pointIterator.next(); pointIterator.remove();
                        UrAgent na = new UrAgent(p1, 1, datasetsize); na.setRelation(transformationType);
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

        if (transformationType == RELATION_SIMILARITY)
            this.transformFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();
                    UrAgent na = new UrAgent(points, 1, datasetsize); na.setRelation(transformationType);
                    na.setAgentHash(points.stream().map(p -> ((UrAgent)p.getToken()).getTokens().toString()).sorted().collect(Collectors.toList()).toString());
                    result.compute(na.getAgentHash(), (k, v) -> v == null? na: addAgent(v, na));

                    return result.values().stream().toList();
                }
            };

        if (transformationType == RELATION_ORDER)
            this.transformFunction = new Function<List<UrPoint>, List<UrAgent>>() {
                @Override
                public List<UrAgent> apply(List<UrPoint> points) {

                    HashMap<String, UrAgent> result = new HashMap<>();
                    UrAgent na = new UrAgent(points, 1, datasetsize); na.setRelation(transformationType);
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


    public Function<List<UrPoint>, List<UrAgent>> getTransformFunction() {
        return transformFunction;
    }

    public List<UrAgent> apply(List<UrPoint> points){
        if (transformFunction != null)
            return transformFunction.apply(points);
        return null;
    }

}
