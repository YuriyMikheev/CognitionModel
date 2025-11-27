package cognitionmodel.models.upright.generators;

import cognitionmodel.models.inverted.index.TextIndex;
import cognitionmodel.models.upright.UrTextDataSet;
import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.models.upright.relations.UrRelation;
import cognitionmodel.models.upright.relations.UrRelationInterface;
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

public class UrGenerator implements UrGeneratorInterface {

    private TextIndex textIndex;
    private UrTextDataSet dataSet;

    private TreeMap<Object, RoaringBitmap> index;
    private UrRelationInterface[] relations;

    private double minMrDelta = 1;
    private long batchSize = 1000000000;
    private long minF = 10;
    public static final int threadsCount = 20;
    public static final int maxContextSize = 1000;

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

    public UrGenerator(TextIndex textIndex, String tokensFile, int[] relationTypes) throws IOException {
        this(textIndex, new UrTextDataSet(tokensFile),relationTypes);
    }

    public UrGenerator(@NotNull TextIndex textIndex, UrTextDataSet dataSet, int[] relationTypes) throws IOException {
        this.textIndex = textIndex;
        this.dataSet = dataSet;
        index = textIndex.getIdx(textIndex.getTextField());
        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSet.getTextTokens().size())).collect(Collectors.toList()).toArray(new UrRelationInterface[0]);
    }



    public TextIndex getTextIndex() {
        return textIndex;
    }

    public UrTextDataSet getDataSet() {
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

   /* public List<UrAgent> newAgents_old(List<UrAgent> inAgents, int attentionSize, int variants, int[] newPointsPositions){

        if (inAgents.isEmpty()) return new ArrayList<>();

        ConcurrentHashMap<String, UrAgent> agents = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Long> tagents = new ConcurrentHashMap<>();
        int step = attentionSize;//*2/3;
        long dataSetSize = dataSet.getTextTokens().size();
        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();
        final long[] nn = {0};
        //int minpoint =  Arrays.stream(newPointsPositions).min().getAsInt(), maxpoint = Arrays.stream(newPointsPositions).max().getAsInt();

        BitSet atp = new BitSet();


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

            LinkedList<UrAgent> nin = new LinkedList<>();

            for (Iterator<UrAgent> iterator = in.listIterator(); iterator.hasNext(); ) {
                UrAgent agent = iterator.next();
                if (atp.stream().anyMatch(p -> p < agent.getLastPos() + attentionSize && p > agent.getFirstPos() - attentionSize))
                    nin.add(agent);
                else
                    oin.add(agent);
            }
            in = nin;
        }

    //    List<Integer[]> combs = singlePoints(newPointsPositions, attentionSize);//allCombinations(newPointsPositions, attentionSize);
        List<Integer[]> combs = allCombinations(newPointsPositions, attentionSize);
    //    List<Integer[]> combs = longCombinations(newPointsPositions, attentionSize);


        for (int k = 0; k < in.size(); k += step) {
            int finalK = k;
            List<UrAgent> finalIn = in;
            cfl.add(CompletableFuture.supplyAsync(() -> {
                final double[] dmr = {0};

                PriorityQueue<IndexPoint> tokens = new PriorityQueue<>(Comparator.comparing(IndexPoint::getIdx));

                for (int i = finalK; i < min(finalK + attentionSize, finalIn.size()); i++) {
                    tokens.add(new IndexPoint(finalIn.get(i).getFirstPos(), finalIn.get(i), new BatchedIterator(finalIn.get(i).getIdx())));
                }

                LinkedList<UrAgent> nlist = new LinkedList<>();
                long  n = 0, ti = 0;

                for (; !tokens.isEmpty(); ) {
                    IndexPoint indexPoint = tokens.poll();
                    ti = indexPoint.getIdx();
                    indexPoint.nextIdx();
                    if (ti != -1) tokens.add(indexPoint);
                    else
                        continue;

                    n++;

                    boolean dontAddNew = false;
                    for (UrAgent agent : nlist)
                        if (ti - agent.getStartpos() < attentionSize)
 //                           if (indexPoint.getPosition() - agent.getFirstPos() < attentionSize && indexPoint.getPosition() > agent.getPoints().getLast().getPosition())
                            {
                                agent.addPoint(new UrPoint(indexPoint.getPosition(), indexPoint.getToken(), ti));
                                dontAddNew = true;
                            }

                    if (!dontAddNew)
                        nlist.add(new UrAgent(new UrPoint(indexPoint.getPosition(), indexPoint.getToken(), ti), 1, dataSetSize, ti));

                    while (!nlist.isEmpty() && ti - nlist.getFirst().getStartpos() >= attentionSize) {
                        UrAgent as = nlist.poll();

                        LinkedList<NewAgentData> ptl = new LinkedList<>();

                        //if (as.getPoints().getLast().getPosition() + attentionSize > maxpoint  && as.getFirstPos() - attentionSize < minpoint)
                        {
                            for (Integer[] ic : combs) {
                                NewAgentData nad = new NewAgentData(newPointsPositions.length);
                                for (int i : ic)
//                                    if (as.getPoints().getLast().getPosition() + attentionSize > i && as.getFirstPos() - attentionSize < i)
                                    {
                                        long idx = as.getStartpos() + i - as.getFirstPos();
                                        if (idx > 0 && idx < dataSetSize) {
                                            int t = dataSet.getTextTokens().get(idx);
                                            nad.setPoint(i, t);
                                        }
                                    }
                                if (nad.lastidx > 0) ptl.add(nad);
                            }
                        }

                        for (UrRelation relation: relations) {
                            for (UrAgent a : relation.applyDecomposition(as)) {
                                if (!agents.containsKey(a.getAgentHash()))
                                    agents.put(a.getAgentHash(), a);
                                else
                                    incAgentF(agents.get(a.getAgentHash()),  as.getStartpos() - as.getFirstPos() + a.getFirstPos());//as.getStartPos?? а если не сначала as начинается агент a?

                                for(NewAgentData nad: ptl)
                                 //   if (a.getFirstPos() - attentionSize < nad.getStartPoint() && a.getPoints().getLast().getPosition() + attentionSize > nad.getStartPoint())
                                        tagents.compute(a.getAgentHash() + nad.getSignature(a.getPoints().getLast().getPosition() + attentionSize), (key, v) -> v == null ? 1 : v + 1);
                            }
                        }
                    }

                    if (n % batchSize == 0) {
                        double ldmr = agents.values().stream().mapToDouble(a->a.getMr()>0? a.getMr():0).sum();
                        if (ldmr - dmr[0] < minMrDelta)
                            break;
                        dmr[0] = ldmr;
                    }
                }

                nn[0] +=n;
                return null;
            }));
        }
        cfl.forEach(CompletableFuture::join);

        ConcurrentHashMap<String, Double> mrt = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> t4a = new ConcurrentHashMap<>();
        //System.out.println(tagents.size()+" agents generated");

        tagents.forEach(threadsCount, (key, val) -> {
            if (val > minF) {
                String[] keys = key.split(";");
                UrAgent a = agents.get(keys[0]);
                double d = a.getMr() + log((double) val / a.getF());

                for (int i = 1; i < keys.length; i++) {
                    String[] ps = keys[i].split(":");
                    Integer t = Integer.parseInt(ps[1]);
                    d = d - log((double) dataSet.getFreqs()[t] / dataSetSize);
                }

                try {
                    String pt = key.substring(keys[0].length() + 1, key.length());

                    double finalD = d;
                    // Double pd = mrt.get(pt);
                    d = mrt.compute(pt, (k, v) -> v == null ? finalD : v + finalD);

                    ConcurrentHashMap<String, Double> as = t4a.get(pt);

                    if (as == null)
                        t4a.put(pt, as = new ConcurrentHashMap<>());
                    as.put(a.getAgentHash(), d);

                } catch (StringIndexOutOfBoundsException e){
                    System.err.println(e);
                }

            }
        });

        ConcurrentHashMap<String, Integer> ntl = new ConcurrentHashMap<>();
        mrt.entrySet().stream().parallel().sorted((a1,a2)-> a1.getValue() > a2.getValue() ?-1:a1.getValue() < a2.getValue()? 1: 0).limit(variants).map(Map.Entry::getKey).forEach(s-> ntl.put(s,1));
        LinkedList<UrAgent> nal = new LinkedList<>();

        ntl.forEach(threadsCount, (s,v1) -> {
            String[] keys = s.split(";");

            LinkedList<UrPoint> points = new LinkedList<>();
            for (int i = 0; i < keys.length; i++) {
                String[] ps = keys[i].split(":");
                Integer p = Integer.parseInt(ps[0]);
                Integer t = Integer.parseInt(ps[1]);
                points.add(new UrPoint(p, new UrAgent(new UrPoint(p, t), index.get(t), dataSetSize)));
            }

            AtomicLong ff = new AtomicLong();

            t4a.get(s).forEach((k, v) -> {
                LinkedList<UrPoint> apoints = new LinkedList<>(agents.get(k).getPoints());
                apoints.addAll(points);
                UrAgent na = new UrAgent(apoints, ff.addAndGet(tagents.get(k + ";" + s)), dataSetSize);
                na.getAgentHash();
                synchronized (this) {
                    nal.add(na);
                }
            });

            UrAgent naa = points.size() > 1 ? new UrAgent(points, ff.get(), dataSetSize) : (UrAgent)points.getFirst().getToken(); // if points more than 1 agent freq is in context of all others agents
            naa.getAgentHash();
            synchronized (this) {
                nal.add(naa);
            }

        });

        List<UrAgent> al = new LinkedList<>(agents.values());
        al.addAll(nal);
        al.addAll(oin);

        //System.out.println(nal.size()+" new agents amount");
        //  al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

        //System.out.println(nn[0] + " tokens analyzed");
        System.out.println(al.size() + " agents found");

        return al;
    }*/


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

            LinkedList<UrAgent> nin = new LinkedList<>();

            for (Iterator<UrAgent> iterator = in.listIterator(); iterator.hasNext(); ) {
                UrAgent agent = iterator.next();
                if (atp.stream().anyMatch(p -> p < agent.getLastPos() + attentionSize && p > agent.getFirstPos() - attentionSize))
                    nin.add(agent);
                else
                    oin.add(agent);
            }
            in = nin;
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

        if (soredIndexes.size() > 10000) soredIndexes = soredIndexes.subList(0, maxContextSize);

        HashSet<String> ftokens = new HashSet<>();

        long threadSize = soredIndexes.size()/threadsCount;

        int indexi = -1;

        if (idxWeights.size() > 0)
         for (Iterator<Long> gIterator = soredIndexes.listIterator(); gIterator.hasNext(); ) {
            //long idx = gIterator.next();
             AtomicInteger finalIndexi = new AtomicInteger(indexi);
             finalIndexi.getAndIncrement();
             List<Long> finalSoredIndexes = soredIndexes;
             cfl.add(CompletableFuture.supplyAsync(() -> {
                 int ni = 0;
                 long idx;
                 try {
                     while (gIterator.hasNext() && (idx = gIterator.next()) != -1 && ni++ < threadSize) {
                         List<Integer> lr = getRangeByPoints(idx, newPointsPositions);
                         Iterator<Long> gnIterator = finalSoredIndexes.listIterator(finalIndexi.getAndIncrement());

                         HashSet<String> fctokens = new HashSet<>();

                         while (gnIterator.hasNext()) {
                             Long idx1 = gnIterator.next();
                             ListIterator<Integer> range = lr.listIterator();
                             ListIterator<Integer> range1 = getRangeByPoints(idx1, newPointsPositions).listIterator();

                             LinkedList<UrPoint> tokens = new LinkedList<>();
                             int i = 0, t = 0;
                             while (range.hasNext() && range1.hasNext()) {
                                 if ((t = range.next()) == range1.next())
                                     tokens.add(new UrPoint(newPointsPositions[i], new UrAgent(new UrPoint(newPointsPositions[i], t, idx1 + newPointsPositions[i]), 1, dataSetSize), idx1 + newPointsPositions[i]));
                                 i++;
                             }

                             if (tokens.size() > 1 && !ftokens.contains(tokens.toString())) {
                                 UrAgent as = new UrAgent(tokens, 1, dataSetSize, idx);
                                 fctokens.add(tokens.toString());
                                 for (UrRelationInterface relation : relations) {
                                     for (UrAgent a : relation.makeDecomposition(as)) {
                                         if (!agents.containsKey(a.getAgentHash())) {
                                             agents.put(a.getAgentHash(), a);
                                             a.addIndex(idx1 + a.getFirstPos() - as.getFirstPos());
                                             a.addIndex(idx + a.getFirstPos() - as.getFirstPos());
                                             a.incF(2);
                                         } else
                                             incAgentF(agents.get(a.getAgentHash()), idx1 - as.getFirstPos() + a.getFirstPos());
                                     }
                                 }
                             }
                         }


                         ftokens.addAll(fctokens);

                         nn[0] += 1;
                    }
                 } catch (NoSuchElementException e){

                 }


                return null;

            }));
        }
        cfl.forEach(CompletableFuture::join);

        double maxMR = agents.size()>0 ? agents.values().stream().max(Comparator.comparing(UrAgent::getMr)).get().getMr() : 0;

        List<UrAgent> al = new LinkedList<>(agents.values().stream()
                /*.filter(a -> a.getMr()/a.getPointList().size() > maxMR * 0.8/a.getPointList().size())*/
                .sorted(Comparator.comparing(UrAgent::getMr).reversed())
                .toList());

        al.addAll(oin);
        al.addAll(in);

        return al;
    }


    private @NotNull List<Integer> getRangeByPoints(long idx, int @NotNull [] points){
        LinkedList<Integer> r = new LinkedList<>();
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

    public static @NotNull List<Integer[]> allCombinations(int @NotNull [] ints, int attentionSize){
        LinkedList<Integer[]> r = new LinkedList<>();

        if (ints.length == 0) return r;

        int j = 0;
        LinkedList<Integer> nints = new LinkedList<>();
        do {
            int si = ints[j];
            for (; j < ints.length? ints[j] - si <  attentionSize: false; j++)
                nints.add(ints[j]);
            r.add(nints.toArray(new Integer[]{}));
            nints.clear();
        } while (j < ints.length);

        LinkedList<Integer[]> nr = new LinkedList<>();
        for (Integer[] combo: r){
            LinkedList<Integer[]> cr = new LinkedList<>();
            for (int i: combo){
                LinkedList<Integer[]> crn = new LinkedList<>();
                for (Integer[] cc: cr){
                    Integer[] nce = Arrays.copyOf(cc, cc.length+1);
                    nce[cc.length] = i;
                    crn.add(nce);
                }
                cr.add(new Integer[]{i});
                cr.addAll(crn);
            }
            cr.removeLast();
            nr.addAll(cr);
        }
        r.addAll(nr);

        return r;
    }

    public static @NotNull List<Integer[]> singlePoints(int @NotNull [] ints, int attentionSize){
        LinkedList<Integer[]> r = new LinkedList<>();

        for (int i: ints)
            r.add(new Integer[]{i});

        return r;
    }

    public static @NotNull List<Integer[]> longCombinations(int @NotNull [] ints, int attentionSize){
        LinkedList<Integer[]> r = new LinkedList<>();
        int j = 0;
        LinkedList<Integer> nints = new LinkedList<>();
        do {
            int si = ints[j];
            for (; j < ints.length? ints[j] - si <  attentionSize: false; j++)
                nints.add(ints[j]);
            r.add(nints.toArray(new Integer[]{}));
            nints.clear();
        } while (j < ints.length);

        return r;
    }

}
