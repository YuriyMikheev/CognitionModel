package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import cognitionmodel.models.inverted.index.TextIndex;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.Math.*;

/***
 * Generates new tokens basing on index and input list of UrAgents
 */

public class UrGenerator {

    private TextIndex textIndex;
    private UprightTextDataSet dataSet;

    private TreeMap<Object, RoaringBitmap> index;
    private UrRelation[] relations;

    private double minMrDelta = 1;
    private long batchSize = 1000000000;
    private long minF = 10;
    public static final int threadsCount = 20;

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
        this(textIndex, new UprightTextDataSet(tokensFile),relationTypes);
    }

    public UrGenerator(TextIndex textIndex, UprightTextDataSet dataSet, int[] relationTypes) throws IOException {
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

    public List<UrAgent> newAgents(List<UrAgent> inAgents, int attentionSize, int variants, int[] newPointsPositions){

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
                            for (UrAgent a : relation.applyDecomposition(as.getPoints())) {
                                if (!agents.containsKey(a.getAgentHash()))
                                    agents.put(a.getAgentHash(), a);
                                else
                                    incAgentF(agents.get(a.getAgentHash()), 1, as.getStartpos());

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
        System.out.println(tagents.size()+" agents generated");

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

        System.out.println(nal.size()+" new agents amount");
        //  al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

        System.out.println(nn[0] + " tokens analyzed");

        return al;
    }

    private void incAgentF(UrAgent agent, long f, long index){
        if (agent.getPoints().size() > 0)
        {
           agent.incF(f);
           agent.addIndex(index);
        }
    }

    public static List<Integer[]> allCombinations(int[] ints, int attentionSize){
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

    public static List<Integer[]> singlePoints(int[] ints, int attentionSize){
        LinkedList<Integer[]> r = new LinkedList<>();

        for (int i: ints)
            r.add(new Integer[]{i});

        return r;
    }

    public static List<Integer[]> longCombinations(int[] ints, int attentionSize){
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
