package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.lang.Math.round;

public class UrDecomposer {

    private class IdxPoint{
        int position;
        Object token;
        long idx;
        Iterator<Long> iterator;

        public IdxPoint(int position, Object token, Iterator<Long> iterator) {
            this.position = position;
            this.token = token;
            this.iterator = iterator;
            idx = nextIdx();
        }

        public long nextIdx(){
            if (iterator.hasNext()) return idx = iterator.next();
            else
                return idx = -1;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }

        public long getIdx() {
            return idx;
        }

        public Object getToken() {
            return token;
        }

        public void setToken(int token) {
            this.token = token;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public Iterator<Long> getIterator() {
            return iterator;
        }

        public void setIterator(Iterator<Long> iterator) {
            this.iterator = iterator;
        }
    }

    double minMrDelta = 0.1;
    long batchSize = 1000000, datSetSize = 0;
    private UrRelation relations[];

    public UrDecomposer(double accuracy, long datSetSize){
        this(accuracy*100, 10000000, datSetSize);
        batchSize = round(datSetSize * accuracy);
    }

    public UrDecomposer(double minMrDelta, long batchSize, long datSetSize) {
        this(minMrDelta, batchSize, datSetSize, new UrRelation[]{  new UrRelation(UrRelation.RELATION_SIMILARITY, datSetSize),
                new UrRelation(UrRelation.RELATION_ORDER, datSetSize),
                new UrRelation(UrRelation.RELATION_POINTED, datSetSize)});
    }
    public UrDecomposer(double minMrDelta, long batchSize, long datSetSize, int[] relationTypes) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.datSetSize = datSetSize;

        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, datSetSize)).collect(Collectors.toList()).toArray(new UrRelation[0]);
    }



    public UrDecomposer(double minMrDelta, long batchSize, long datSetSize, UrRelation[] relations) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.datSetSize = datSetSize;

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


    public LinkedList<UrAgent> makeAgentList(List<Integer> in, Map<Object, RoaringBitmap> index){
        int i = 0;
        LinkedList<UrAgent> list = new LinkedList<>();
        for (int t: in){
            if (index.containsKey(t))
                list.add(new UrAgent(new UrPoint(i, t), index.get(t), datSetSize));
            else
                System.err.println(in.get(i) + " token unknown");
            i++;
        }

        return list;
    }


    public List<UrAgent> decompose(List<Integer> in, int attentionSize, Map<Object, RoaringBitmap> index){
        return decompose(makeAgentList(in, index), attentionSize);
    }

    public List<UrAgent> decompose(LinkedList<UrAgent> in, int attentionSize){
        if (in.isEmpty()) return new ArrayList<>();
        ConcurrentHashMap<String, UrAgent> agents = new ConcurrentHashMap<>();

        in.sort(Comparator.comparing(UrAgent::getFirstPos));

        int step = attentionSize*2/3;

        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();
        final long[] nn = {0};

        for (int k = 0; k < in.size(); k += step) {
            int finalK = k;
            cfl.add(CompletableFuture.supplyAsync(() -> {
                final double[] dmr = {0};

                PriorityQueue<IdxPoint> tokens = new PriorityQueue<>(Comparator.comparing(IdxPoint::getIdx));

                for (int i = finalK; i < min(finalK + attentionSize, in.size()); i++) {
                    tokens.add(new IdxPoint(in.get(i).getPoints().getFirst().getPosition(), in.get(i), new BatchedIterator(in.get(i).getIdx())));
                }

                LinkedList<UrAgent> nlist = new LinkedList<>();
                HashSet<String> cset = new HashSet<>();
                long  n = 0, sti = 0, ti = 0;

                for (; !tokens.isEmpty(); ) {
                    IdxPoint idxPoint = tokens.poll();
                    ti = idxPoint.getIdx();
                    idxPoint.nextIdx();
                    if (ti != -1) tokens.add(idxPoint);
                        else
                            continue;

                    n++;

                    boolean dontAddNew = false;
                    for (UrAgent agent : nlist)
                        if (ti - agent.getStartpos() < attentionSize)
                            if (idxPoint.getPosition() - agent.getFirstPos() < attentionSize && idxPoint.getPosition() > agent.getPoints().getLast().getPosition()) {
                                agent.addPoint(new UrPoint(idxPoint.getPosition(), idxPoint.getToken(), ti));
                                dontAddNew = true;
                            }

                    if (!dontAddNew)
                        nlist.add(new UrAgent(new UrPoint(idxPoint.getPosition(), idxPoint.getToken(), ti), 1, datSetSize, ti));

                    while (!nlist.isEmpty() && ti - nlist.getFirst().getStartpos() >= attentionSize) {
                        UrAgent as = nlist.poll();
                        for (UrRelation relation: relations)
                            for (UrAgent a: relation.apply(as.getPoints()))
                                if (!agents.containsKey(a.getAgentHash()))
                                    agents.put(a.getAgentHash(), a);
                                else
                                    incAgentF(agents.get(a.getAgentHash()), 1, a.getStartpos());
                    }

                    if (n % batchSize == 0) {
                        double ldmr = cset.stream().mapToDouble(a->agents.get(a).getMr()>0? agents.get(a).getMr():0).sum();
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

        List<UrAgent> al = new LinkedList<>(agents.values());

        al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

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



}
