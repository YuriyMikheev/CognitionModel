package cognitionmodel.models.upright.decomposers;


import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.models.upright.relations.UrRelation;
import cognitionmodel.models.upright.relations.UrRelationInterface;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.lang.Math.round;

public class UrCUDADecomposer {



    double minMrDelta = 0.1;
    float overlap = 1;
    long batchSize = 1000000, dataSetSize = 0;
    private UrRelationInterface relations[];

    public UrCUDADecomposer(double accuracy, long dataSetSize){
        this(accuracy*100, 10000000, dataSetSize);
        batchSize = round(dataSetSize * accuracy);
    }

    public UrCUDADecomposer(double minMrDelta, long batchSize, long dataSetSize) {
        this(minMrDelta, batchSize, dataSetSize, new UrRelationInterface[]{  new UrRelation(UrRelationInterface.RELATION_SIMILARITY, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_ORDER, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_POINTED, dataSetSize)});
    }
    public UrCUDADecomposer(double minMrDelta, long batchSize, long dataSetSize, int[] relationTypes) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.dataSetSize = dataSetSize;

        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSetSize)).collect(Collectors.toList()).toArray(new UrRelationInterface[0]);
    }



    public UrCUDADecomposer(double minMrDelta, long batchSize, long dataSetSize, UrRelationInterface[] relations) {
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

        for (int k = 0; k < in.size(); k += step) {
            int t[] = new int[step];
            int I[] = new int[step];
            int minI = min(step, in.size() - k) - 1;
            int nextMinI = Integer.MAX_VALUE;
            LinkedList<UrPoint> clist = new LinkedList();
//            long cx[] = new long[step];
            int X[][] = new int[step][];

            for (int i = k; i < min(k + attentionSize, in.size()); i++)
                X[i-k] = in.get(i).getIdx().toArray();

            for (int i = k; i < min(k + attentionSize, in.size()); i++) {
                t[i-k] = i;
                if (X[minI][I[minI]] > X[i-k][I[i-k]]) minI = i-k;
            }

            ConcurrentHashMap<String, UrAgent> tagents = new ConcurrentHashMap<>();

            nextMinI = min(step, in.size() - k) - minI - 1;
            clist.add(new UrPoint(minI, in.get(t[minI]),X[minI][I[minI]]));

            for (int ik = k; ik < min(k + attentionSize, in.size()); ik++) {
                int j = ik-k;
                int x = X[j][I[j]];
                if (x - X[minI][I[minI]] == j - minI) {
                    clist.add(new UrPoint(j,in.get(t[j]),x));
                }
                if (x < X[nextMinI][I[nextMinI]]) nextMinI = j;
                I[minI]++;
                if (X[minI][I[minI]] > X[nextMinI][I[nextMinI]] ){
                    int tmi = minI; minI = nextMinI; nextMinI = tmi;
                }
                if (X[minI][I[minI]] - clist.getFirst().getTag() > attentionSize) {
                    UrAgent a = new UrAgent(clist,1,dataSetSize);
                    tagents.compute(a.getAgentHash(), (key,val) -> {
                        if (val == null) return a;
                        val.incF(1);
                        return val;
                    });

                    clist.clear();
                    clist.add(new UrPoint(minI, in.get(t[minI]),X[minI][I[minI]]));
                }
            }

            for (UrAgent as: tagents.values())
                for (UrRelationInterface relation: relations)
                    for (UrAgent a: relation.makeDecomposition(as))
                        if (!agents.containsKey(a.getAgentHash())){
                            agents.put(a.getAgentHash(), a);
                            a.getIdx().or(as.getIdx());
                            a.setF(a.getIdx().getCardinality());
                        }
                        else {
                            UrAgent agent = agents.get(a.getAgentHash());
                            a.getIdx().or(as.getIdx());
                            agent.setF(agent.getIdx().getCardinality());
                        }

        }

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
