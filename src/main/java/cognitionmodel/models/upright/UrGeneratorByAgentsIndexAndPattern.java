package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import cognitionmodel.models.inverted.index.TextIndex;
import cognitionmodel.patterns.FullGridIterativePatterns;
import cognitionmodel.patterns.Pattern;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/***
 * Generates new tokens basing on index and input list of UrAgents
 */



public class UrGeneratorByAgentsIndexAndPattern implements UrGeneratorInterface {

    private TextIndex textIndex;
    private UprightTextDataSet dataSet;

    private TreeMap<Object, RoaringBitmap> index;
    private UrRelation[] relations;

    private double minMrDelta = 1;
    private long batchSize = 1000000000;
    private long minF = 10;
    public static final int threadsCount = 20;
    public static final int maxContextSize = 100000;

    public UrGeneratorByAgentsIndexAndPattern(TextIndex textIndex, String tokensFile, int[] relationTypes) throws IOException {
        this(textIndex, new UprightTextDataSet(tokensFile),relationTypes);
    }

    public UrGeneratorByAgentsIndexAndPattern(@NotNull TextIndex textIndex, UprightTextDataSet dataSet, int[] relationTypes) throws IOException {
        this.textIndex = textIndex;
        this.dataSet = dataSet;
        index = textIndex.getIdx(textIndex.getTextField());
        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSet.getTextTokens().size())).collect(Collectors.toList()).toArray(new UrRelation[0]);
    }

    public TextIndex getTextIndex() {
        return textIndex;
    }

    public UprightTextDataSet getDataSet() {
        return dataSet;
    }

    @Override
    public void setBatchSize(int i) {
        batchSize = i;
    }

    public double getMinMrDelta() {
        return minMrDelta;
    }

    public void setMinMrDelta(double minMrDelta) {
        this.minMrDelta = minMrDelta;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }

    public long getMinF() {
        return minF;
    }

    public void setMinF(long minF) {
        this.minF = minF;
    }


    /**
     * Generates a new list of UrAgent instances, potentially creating new agents based on
     * the given inputs and specified parameters. This method processes the provided list
     * of agents, identifying relationships and generating new points based on their interactions.
     *
     * @param inAgents the initial list of UrAgent instances to be processed
     * @param attentionSize the size of the attention window during processing
     * @param variants the number of variants to be considered
     * @param newPointsPositions an array of positions for the new points to be considered
     * @return a list of new or modified UrAgent instances after processing
     */


    public List<UrAgent> newAgents(@NotNull List<UrAgent> inAgents, int attentionSize, int variants, int[] newPointsPositions){

        if (inAgents.isEmpty()) return new ArrayList<>();

        ConcurrentHashMap<String, UrAgent> agents = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Long> tagents = new ConcurrentHashMap<>();
        long dataSetSize = dataSet.getTextTokens().size();
        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();
        final long[] nn = {0};

        ArrayList<Pattern> patterns = new ArrayList<>(new FullGridIterativePatterns(newPointsPositions.length, attentionSize, 5).getPatterns().stream().filter(pattern -> pattern.getBitSet().cardinality()>1).toList());

        List<UrAgent> in = inAgents.stream()
                //.filter(a-> !a.getFields().intersects(atp)) // filter points are to be generated
                .sorted(Comparator.comparing(UrAgent::getFirstPos)) // sort by agent beginning point
                //.filter(a-> a.getPoints().getLast().getPosition() + 3*attentionSize > maxpoint && a.getFirstPos() - 3*attentionSize  < minpoint) //filter agents that can't influence generating
                .collect(Collectors.toList());

        for (Iterator<UrAgent> iterator = in.listIterator(); iterator.hasNext(); ) {

            UrAgent agent = iterator.next();
            long threadSize = agent.getIdx().getCardinality() / threadsCount;

            for (BatchedIterator gIterator = new BatchedIterator(agent.getIdx()); gIterator.hasNext(); ) {

                cfl.add(CompletableFuture.supplyAsync(() -> {
                    int ni = 0;
                    long idx;
                    try {
                        while (gIterator.hasNext() && (idx = gIterator.next()) != -1 && ni++ < threadSize) {
                            List<Integer> lr = getRangeByPoints(idx, newPointsPositions);

                            for (Pattern pattern : patterns) {
                                String s = "";
                                for (int i : pattern.getSet())
                                    s = s + i + ":" + lr.get(i) + ";";
                                tagents.compute(s, (k, v) -> v == null ? 1 : v + 1);
                            }

                            nn[0] += 1;
                        }
                    } catch (NoSuchElementException e) {

                    } catch (NullPointerException e) {

                    }

                    return null;
                }));
            }

            cfl.forEach(CompletableFuture::join);

            tagents.forEach(threadsCount, (k, v) -> { //переделать под разные типы отношений
                if (v > 2) {
                    LinkedList<UrPoint> points = new LinkedList<>();
                    for (String sp : k.split(";")) {
                        String[] spd = sp.split(":");
                        int i = newPointsPositions[Integer.parseInt(spd[0])];
                        points.add(new UrPoint(i, new UrAgent(new UrPoint(i, Integer.parseInt(spd[1])), 1, dataSetSize)));
                    }
                    points.addAll(agent.getPoints());
                    UrAgent cagent = new UrAgent(points, v, dataSetSize);
                    agents.put(cagent.getAgentHash(), cagent);
                }
            });
        }

        List<UrAgent> al = new LinkedList<>(agents.values().stream()
          //      .filter(a -> a.getMrToLength() > maxMR * 0.8)
                .sorted(Comparator.comparing(UrAgent::getMr).reversed())
                .limit(100)
                .toList());

        al.addAll(in);

        return al;
    }

    private @NotNull List<Integer> getRangeByPoints(long idx, int @NotNull [] points){
        ArrayList<Integer> r = new ArrayList<>();
        for (int i: points)
            r.add(dataSet.getTextTokens().get(idx + i));
        return r;
    }

}
