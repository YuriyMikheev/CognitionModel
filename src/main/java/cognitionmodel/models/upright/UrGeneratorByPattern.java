package cognitionmodel.models.upright;

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

public class UrGeneratorByPattern implements UrGeneratorInterface {

    private TextIndex textIndex;
    private UprightTextDataSet dataSet;

    private TreeMap<Object, RoaringBitmap> index;
    private UrRelation[] relations;

    private double minMrDelta = 1;
    private long batchSize = 1000000000;
    private long minF = 10;
    public static final int threadsCount = 20;
    public static final int maxContextSize = 100000;

    private class NewAgentData{
        int[] points, tokens;
        int lastidx;

        public NewAgentData(int size) {
            points = new int[size];
            tokens = new int[size];
            lastidx = 0;
        }

        public String getSignature(int maxpoint) {
            String signature = "";

            for (int i = 0; i < lastidx; i++)
                if (points[i] <= maxpoint) signature = signature + ";" + points[i] + ":"+tokens[i];
                    else break;

            return signature;
        }

        public int getStartPoint() {
            return points[0];
        }

        public int getEndPoint() {
            return points[lastidx];
        }

        public void setPoint(int point, int token) {
            points[lastidx] = point;
            tokens[lastidx] = token;
            lastidx++;
        }
    }

    public UrGeneratorByPattern(TextIndex textIndex, String tokensFile, int[] relationTypes) throws IOException {
        this(textIndex, new UprightTextDataSet(tokensFile),relationTypes);
    }

    public UrGeneratorByPattern(@NotNull TextIndex textIndex, UprightTextDataSet dataSet, int[] relationTypes) throws IOException {
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
        int step = attentionSize*2/3;
        long dataSetSize = dataSet.getTextTokens().size();
        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();
        final long[] nn = {0};
        //int minpoint =  Arrays.stream(newPointsPositions).min().getAsInt(), maxpoint = Arrays.stream(newPointsPositions).max().getAsInt();

        BitSet atp = new BitSet();

        ArrayList<Pattern> patterns = new ArrayList<>(new FullGridIterativePatterns(newPointsPositions.length, attentionSize, 3).getPatterns().stream().filter(pattern -> pattern.getBitSet().cardinality()>1).toList());


        List<UrAgent> in = inAgents.stream()
                //.filter(a-> !a.getFields().intersects(atp)) // filter points are to be generated
                .sorted(Comparator.comparing(UrAgent::getFirstPos)) // sort by agent beginning point
                //.filter(a-> a.getPoints().getLast().getPosition() + 3*attentionSize > maxpoint && a.getFirstPos() - 3*attentionSize  < minpoint) //filter agents that can't influence generating
                .collect(Collectors.toList());

        LinkedList<UrAgent> oin = new LinkedList<>();


        if (newPointsPositions.length > 0) {
            for (int attentionPoint : newPointsPositions) {
                atp.set(attentionPoint);
            }

/*            LinkedList<UrAgent> nin = new LinkedList<>();

            for (Iterator<UrAgent> iterator = in.listIterator(); iterator.hasNext(); ) {
                UrAgent agent = iterator.next();
                if (atp.stream().anyMatch(p -> p < agent.getLastPos() + attentionSize && p > agent.getFirstPos() - attentionSize))
                    nin.add(agent);
                else
                    oin.add(agent);
            }
            in = nin;*/
        }

        //RoaringBitmap genidx = RoaringBitmap.or(in.stream().filter(agent -> agent.getPoints().size() > 1).map(UrAgent::getIdx).collect(Collectors.toList()).listIterator()).limit(10000);
        HashMap<Long, Double> idxWeights = new HashMap<>();


        for (Iterator<UrAgent> iterator = in.listIterator(); iterator.hasNext(); ) {
            UrAgent agent = iterator.next();
            if (agent.getPoints().size() > 1 & agent.getF() > 1) {
                RoaringBitmap idx = agent.getIdx();
                for (long i: idx)
                    if (i < dataSetSize)
                        idxWeights.compute(i, (k,v)-> v == null ? agent.getMr(): v + agent.getMr());
            }
        }

        List<Long> soredIndexes = new LinkedList<>(idxWeights.keySet());

        soredIndexes.sort((t1,t2) -> {
            Double f1 = idxWeights.get(t1);
            Double f2 = idxWeights.get(t2);

            return f1 > f2? -1: f1 < f2? 1: 0;
        });

        if (soredIndexes.size() > maxContextSize) soredIndexes = soredIndexes.subList(0, maxContextSize);


        long threadSize = soredIndexes.size()/threadsCount;

        int indexi = -1;

        if (idxWeights.size() > 0)
         for (Iterator<Long> gIterator = soredIndexes.listIterator(); gIterator.hasNext(); ) {
             AtomicInteger finalIndexi = new AtomicInteger(indexi);
             finalIndexi.getAndIncrement();
             cfl.add(CompletableFuture.supplyAsync(() -> {
                 int ni = 0;
                 long idx;
                 try {
                     while (gIterator.hasNext() && (idx = gIterator.next()) != -1 && ni++ < threadSize) {
                         List<Integer> lr = getRangeByPoints(idx, newPointsPositions);

                         for (Pattern pattern: patterns){
                             String s = "";
                             for (int i: pattern.getSet())
                                s = s + i + ":" + lr.get(i) + ";";
                             tagents.compute(s, (k,v) -> v == null? 1: v+1);
                         }

                         nn[0] += 1;
                    }
                 } catch (NoSuchElementException e){

                 }


                return null;

            }));
        }
        cfl.forEach(CompletableFuture::join);

        tagents.forEach(threadsCount, (k,v) -> {
            if (v > 2) {
                LinkedList<UrPoint> points = new LinkedList<>();
                for (String sp : k.split(";")) {
                    String[] spd = sp.split(":");
                    int i = newPointsPositions[Integer.parseInt(spd[0])];
                    points.add(new UrPoint(i, new UrAgent(new UrPoint(i, Integer.parseInt(spd[1])), 1, dataSetSize)));
                }
                UrAgent agent = new UrAgent(points, v, dataSetSize);
                agents.put(agent.getAgentHash(), agent);
            }
        });


        //double maxMR = agents.size()>0 ? agents.values().stream().max(Comparator.comparing(UrAgent::getMrToLength)).get().getMrToLength() : 0;

        List<UrAgent> al = new LinkedList<>(agents.values().stream()
          //      .filter(a -> a.getMrToLength() > maxMR * 0.8)
                .sorted(Comparator.comparing(UrAgent::getMr).reversed())
                .limit(100)
                .toList());

        al.addAll(oin);
        al.addAll(in);

        return al;
    }




    private @NotNull List<Integer> getRangeByPoints(long idx, int @NotNull [] points){
        ArrayList<Integer> r = new ArrayList<>();
        for (int i: points)
            r.add(dataSet.getTextTokens().get(idx + i));
        return r;
    }


    private void incAgentF(@NotNull UrAgent agent, long index){
        if (agent.getPoints().size() > 0)
            if (!agent.getIdx().contains(index, index+1))
        {
           agent.incF(1);
           agent.addIndex(index);
        }
    }


}
