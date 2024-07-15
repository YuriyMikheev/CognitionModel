package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import cognitionmodel.models.inverted.index.TextIndex;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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

    double minMrDelta = 1;
    long batchSize = 10000000;
    public static final int threadsCount = 20;

    public UrGenerator(TextIndex textIndex, String tokensFile, int[] relationTypes) throws IOException {
        this.textIndex = textIndex;
        dataSet = new UprightTextDataSet(tokensFile);
        index = textIndex.getIdx(textIndex.getTextField());
        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSet.getTextTokens().size())).collect(Collectors.toList()).toArray(new UrRelation[0]);
    }

    public TextIndex getTextIndex() {
        return textIndex;
    }

    public UprightTextDataSet getDataSet() {
        return dataSet;
    }
    public List<UrPoint> newTokens(List<UrAgent> agents, int attentionSize, int variants) {
        int[] ap = new int[attentionSize];
        int lastPoint = agents.stream().mapToInt(a-> a.getPoints().getLast().getPosition()).max().getAsInt();

        for (int i = 0; i < attentionSize; i++) {
            ap[i] = i + lastPoint + 1;
        }

        return newTokens(agents, ap, variants);
    }
    public List<UrPoint> newTokens(List<UrAgent> agents, int[] attentionPoints, int variants){
        //Integer maxToken = (Integer) textIndex.getIdx(textIndex.getTextField()).lastKey();
        List<Integer>[] maxIdx = new List[attentionPoints.length];

        LinkedList<UrPoint> r = new LinkedList<>();
        BitSet atp = new BitSet();
        for (int attentionPoint : attentionPoints) {
            atp.set(attentionPoint);
        }


        //long firstPoint = agents.stream().mapToLong(a-> a.getFirstPos()).min().getAsLong();
        agents = agents.stream().filter(a-> !a.getFields().intersects(atp)).collect(Collectors.toList());

        double s = dataSet.getTextTokens().size(); int j = 0;

        for (int i: attentionPoints) {
            HashMap<Integer, Double> newIdx = new HashMap<>();
            for (UrAgent agent : agents) {

                HashMap<Integer, Double> newAIdx = new HashMap<>();
                BatchedIterator iterator = new BatchedIterator(agent.getIdx());
                if (agent.getPoints().size() == 1)
                    if (agent.getPoints().getFirst().getToken() instanceof UrAgent)
                        iterator = new BatchedIterator(((UrAgent) agent.getPoints().getFirst().getToken()).getIdx());

                while (iterator.hasNext()) {
                    for (int shift: relations[agent.getRelation()].applyComposition(i, agent)) {
                        long idx = iterator.next() + shift;
                        if (idx < dataSet.getTextTokens().size() && idx >= 0) {
                            int newToken = dataSet.getTextTokens().get(idx);
                            newAIdx.compute(newToken, (k, v) -> (v == null ? 1 : v + 1));
                        }
                    }
                }
                //long dist = firstPoint - agentFirstPos + i + 1;
                newAIdx.entrySet().forEach(e -> newIdx.compute(e.getKey(), (k, v) -> {
                      double d = agent.getMr() - log(agent.getP()) + log(e.getValue()/dataSet.getFreqs()[k]);
                   // double d = log(e.getValue() / (agent.getF())) - log (dataSet.getFreqs()[k] / s) - log(agent.getP());
                    //double d = log(e.getValue()/dataSet.getFreqs()[k]) - 2*log(agent.getP());
                    return v == null ? d : v + d;
                }));

            }
            maxIdx[j] = newIdx.entrySet().stream().sorted((e1, e2) -> e1.getValue() < e2.getValue()? 1: e1.getValue() > e2.getValue()? -1:0).limit(variants).map(e->e.getKey()).collect(Collectors.toList());
            //r.add(new UrPoint(i+(int)lastIdx, maxIdx[i].get(0)));
            int finalI = i;
            r.addAll(maxIdx[j++].stream().map(e-> new UrPoint(finalI, e)).collect(Collectors.toList()));
        }

        return r;
    }


    public List<UrAgent> newAgents(List<UrAgent> in, int attentionSize, int variants, int[] newPointsPositions){

        if (in.isEmpty()) return new ArrayList<>();
        ConcurrentHashMap<String, UrAgent> agents = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Long> tagents = new ConcurrentHashMap<>();

        in.sort(Comparator.comparing(UrAgent::getFirstPos));

        BitSet atp = new BitSet();

        for (int attentionPoint : newPointsPositions) {
            atp.set(attentionPoint);
        }

        in = in.stream().filter(a-> !a.getFields().intersects(atp)).collect(Collectors.toList());
        int step = attentionSize*2/3;
        long dataSetSize = dataSet.getTextTokens().size();

        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();
        final long[] nn = {0};

        for (int k = 0; k < in.size(); k += step) {
            int finalK = k;
            List<UrAgent> finalIn = in;
            cfl.add(CompletableFuture.supplyAsync(() -> {
                final double[] dmr = {0};

                PriorityQueue<IndexPoint> tokens = new PriorityQueue<>(Comparator.comparing(IndexPoint::getIdx));

                for (int i = finalK; i < min(finalK + attentionSize, finalIn.size()); i++) {
                    tokens.add(new IndexPoint(finalIn.get(i).getPoints().getFirst().getPosition(), finalIn.get(i), new BatchedIterator(finalIn.get(i).getIdx())));
                }


                LinkedList<UrAgent> nlist = new LinkedList<>();
                long  n = 0, sti = 0, ti = 0;

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
                            if (indexPoint.getPosition() - agent.getFirstPos() < attentionSize && indexPoint.getPosition() > agent.getPoints().getLast().getPosition()) {
                                agent.addPoint(new UrPoint(indexPoint.getPosition(), indexPoint.getToken(), ti));
                                dontAddNew = true;
                            }

                    if (!dontAddNew)
                        nlist.add(new UrAgent(new UrPoint(indexPoint.getPosition(), indexPoint.getToken(), ti), 1, dataSetSize, ti));

                    while (!nlist.isEmpty() && ti - nlist.getFirst().getStartpos() >= attentionSize) {
                        UrAgent as = nlist.poll();
                        for (UrRelation relation: relations) {
                            for (UrAgent a : relation.applyDecomposition(as.getPoints())) {
                                if (!agents.containsKey(a.getAgentHash()))
                                    agents.put(a.getAgentHash(), a);
                                else
                                    incAgentF(agents.get(a.getAgentHash()), 1, as.getStartpos());


                                for (int[] ic: allCombinations(newPointsPositions))
                                    for (int i:ic) {
                                        if (a.getFirstPos() - attentionSize < i && a.getPoints().getLast().getPosition() + attentionSize > i)
                                            if (as.getStartpos() + i - a.getFirstPos() > 0 && as.getStartpos() + i - a.getFirstPos() < dataSetSize) {
                                                int t = dataSet.getTextTokens().get(as.getStartpos() + i - a.getFirstPos());
                                                String pt = a.getAgentHash() + ";" + i + ";" + t;
                                                tagents.compute(pt, (key, v) -> v == null ? 1 : v + 1);
                                            }
                                    }
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
            String[] keys = key.split(";");
            //Integer p = Integer.parseInt(keys[1]);
            Integer t = Integer.parseInt(keys[2]);
            UrAgent a = agents.get(keys[0]);
            double d = a.getMr() + log((double)val/a.getF()) - log((double) dataSet.getFreqs()[t]/dataSetSize);
            String pt = keys[1]+";"+keys[2];
            mrt.compute(pt, (k,v)-> v == null? d: v + d);
            ConcurrentHashMap<String, Double> as =  t4a.get(pt);
            if (as == null)
                t4a.put(pt, as = new ConcurrentHashMap<>());
            as.put(a.getAgentHash(), d);
        });

        ConcurrentHashMap<String, Integer> ntl = new ConcurrentHashMap<>();
        mrt.entrySet().stream().sorted((a1,a2)-> a1.getValue() > a2.getValue() ?-1:a1.getValue() < a2.getValue()? 1: 0).limit(variants).map(Map.Entry::getKey).forEach(s-> ntl.put(s,1));
        LinkedList<UrAgent> nal = new LinkedList<>();


        ntl.forEach(threadsCount, (s,v1) -> {
            String[] keys = s.split(";");
            Integer p = Integer.parseInt(keys[0]);
            Integer t = Integer.parseInt(keys[1]);
            UrPoint point = new UrPoint(p, new UrAgent(new UrPoint(p,t), dataSet.getFreqs()[t], dataSetSize));

            t4a.get(s).forEach((k,v) -> {
                UrAgent na = new UrAgent(agents.get(k).getPoints(), tagents.get(k+";"+s), dataSetSize);
                na.addPoint(point);
                na.getAgentHash();
                synchronized (this) {
                    nal.add(na);
                }
            });
        });

        List<UrAgent> al = new LinkedList<>(agents.values());
        al.addAll(nal);
      //  al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

        System.out.println(nn[0] + " tokens analyzed");

        return al;
    }

    //сила влияния агента на это месте
    private double dp(long distance, UrAgent agent, double lp){
        return (agent.getMr())/(agent.getF()*distance) + lp;
    }

    private void incAgentF(UrAgent agent, long f, long index){
        if (agent.getPoints().size() > 0)
        {
            agent.incF(f);
         //  agent.addIndex(index);
        }
    }

    private int[][] allCombinations(int[] ints){
        return new int[][]{ints};
    }


}
