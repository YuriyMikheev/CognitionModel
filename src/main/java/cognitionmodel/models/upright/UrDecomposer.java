package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.lang.Math.round;

public class UrDecomposer {



    double minMrDelta = 0.1;
    float overlap = 1;
    long batchSize = 1000000, dataSetSize = 0;
    private UrRelation relations[];

    public UrDecomposer(double accuracy, long dataSetSize){
        this(accuracy*100, 10000000, dataSetSize);
        batchSize = round(dataSetSize * accuracy);
    }

    public UrDecomposer(double minMrDelta, long batchSize, long dataSetSize) {
        this(minMrDelta, batchSize, dataSetSize, new UrRelation[]{  new UrRelation(UrRelation.RELATION_SIMILARITY, dataSetSize),
                new UrRelation(UrRelation.RELATION_ORDER, dataSetSize),
                new UrRelation(UrRelation.RELATION_POINTED, dataSetSize)});
    }
    public UrDecomposer(double minMrDelta, long batchSize, long dataSetSize, int[] relationTypes) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.dataSetSize = dataSetSize;

        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSetSize)).collect(Collectors.toList()).toArray(new UrRelation[0]);
    }



    public UrDecomposer(double minMrDelta, long batchSize, long dataSetSize, UrRelation[] relations) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.dataSetSize = dataSetSize;

        this.relations = relations;
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


    public LinkedList<UrAgent> makeAgentList(@NotNull List<Integer> in, Map<Object, RoaringBitmap> index){
        int i = 0;
        LinkedList<UrAgent> list = new LinkedList<>();
        for (int t: in){
            if (index.containsKey(t))
                list.add(new UrAgent(new UrPoint(i, t), index.get(t), dataSetSize));
            else
                System.err.println(in.get(i) + " token unknown");
            i++;
        }

        return list;
    }


    public List<UrAgent> decompose(List<Integer> in, int attentionSize, Map<Object, RoaringBitmap> index){
        return decompose(makeAgentList(in, index), attentionSize);
    }

    public List<UrAgent> decompose(@NotNull List<UrAgent> in, int attentionSize){
        if (in.isEmpty()) return new ArrayList<>();
        ConcurrentHashMap<String, UrAgent> agents = new ConcurrentHashMap<>();

        in.sort(Comparator.comparing(UrAgent::getFirstPos));

        int step = round(attentionSize*overlap);

        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();
        final long[] nn = {0};

        for (int k = 0; k < in.size(); k += step) {
            int finalK = k;
            cfl.add(CompletableFuture.supplyAsync(() -> {
                final double[] dmr = {0};

                PriorityQueue<IndexPoint> tokens = new PriorityQueue<>(Comparator.comparing(IndexPoint::getIdx));

                for (int i = finalK; i < min(finalK + attentionSize, in.size()); i++) {
                    tokens.add(new IndexPoint(in.get(i).getPoints().getFirst().getPosition(), in.get(i), new BatchedIterator(in.get(i).getIdx())));
                }

                LinkedList<UrAgent> nlist = new LinkedList<>();
                long  n = 0, ti = 0;


                ConcurrentHashMap<String, UrAgent> tagents = new ConcurrentHashMap<>();


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
                        if (!tagents.containsKey(as.getAgentHash()))
                            tagents.put(as.getAgentHash(), as);
                        else
                            incAgentF(tagents.get(as.getAgentHash()), 1, as.getStartpos());
                    }

                    if (n % batchSize == 0) {
                        double ldmr = agents.values().stream().mapToDouble(a->a.getMr()>0? a.getMr():0).sum();
                        if (ldmr - dmr[0] < minMrDelta)
                            break;
                        dmr[0] = ldmr;
                    }
                }

                for (UrAgent as: tagents.values())
                    for (UrRelation relation: relations)
                        for (UrAgent a: relation.applyDecomposition(as))
                            if (!agents.containsKey(a.getAgentHash())){
                                agents.put(a.getAgentHash(), a);
                                a.getIdx().or(as.getIdx());
                                a.setF(a.getIdx().getCardinality());
                            }
                            else {
                                UrAgent agent = agents.get(a.getAgentHash());
                                a.getIdx().or(as.getIdx());
                                agent.setF(agent.getIdx().getCardinality());

                                //incAgentF(agents.get(a.getAgentHash()), as.getF(), as.getStartpos() - as.getFirstPos() + a.getFirstPos());
                            }
                nn[0] +=n;
                return null;
            }));
        }
        cfl.forEach(CompletableFuture::join);

        List<UrAgent> al = new ArrayList<>(agents.values());

        al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

        int i = 1;
        for (UrAgent a1: al) {
            if (a1.getPoints().size() > 1)
                for (Iterator<UrAgent> iterator = al.listIterator(i); iterator.hasNext(); ){
                    UrAgent a2 = iterator.next();
                    BitSet bs = BitSet.valueOf(a1.getFields().toLongArray());
                    bs.and(a2.getFields());
                    if (a1.getFields().cardinality() == bs.cardinality()) {
                        a1.incF(a2.getF());
                        a1.getIdx().or(a2.getIdx());
                    }
                }
            i++;
        }
        //System.out.println(nn[0] + " tokens analyzed");

        return al;
    }

    private void incAgentF(@NotNull UrAgent agent, long f, long index){
        if (agent.getPoints().size() > 0)
            if (!agent.getIdx().contains(index, index+1))
        {
            agent.incF(f);
            agent.addIndex(index);
        }
    }



}
