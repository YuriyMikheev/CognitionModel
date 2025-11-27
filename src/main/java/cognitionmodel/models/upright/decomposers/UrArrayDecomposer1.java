package cognitionmodel.models.upright.decomposers;

import cognitionmodel.models.upright.UrTextDataSet;
import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.models.upright.relations.UrRelation;
import cognitionmodel.models.upright.relations.UrRelationInterface;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrArrayDecomposer1 {


    int threads = 2;
    double minMrDelta = 0.1;
    float overlap = 1;
    long batchSize = 1000000, dataSetSize = 0;
    private UrRelationInterface relations[];
    private int[] relationTypes;
    private UrTextDataSet dataSet;

    public UrArrayDecomposer1(double minMrDelta, long batchSize, long dataSetSize) {
        this(minMrDelta, batchSize, dataSetSize, new UrRelationInterface[]{  new UrRelation(UrRelationInterface.RELATION_SIMILARITY, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_ORDER, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_POINTED, dataSetSize)});
    }

    public UrArrayDecomposer1(UrTextDataSet dataSet, int ... relationTypes) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.dataSetSize = dataSet.getTextTokens().size();
        this.dataSet = dataSet;
        this.relationTypes = relationTypes;

        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSetSize)).collect(Collectors.toList()).toArray(new UrRelationInterface[0]);
    }



    public UrArrayDecomposer1(double minMrDelta, long batchSize, long dataSetSize, UrRelationInterface... relations) {
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


    public List<UrAgent> decompose(List<Integer> in, int attentionSize, Map<Object, RoaringBitmap> index, double density){
        return decompose(makeAgentList(in, index), attentionSize, density);
    }

    public List<UrAgent> decompose(@NotNull List<UrAgent> in, int attentionSize){
        return decompose(in, attentionSize, 1);
    }

    public List<UrAgent> decompose(@NotNull List<UrAgent> in, int attentionSize, double density){
        if (in.isEmpty()) return new ArrayList<>();
        ConcurrentHashMap<String, UrAgent> agents = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> sagents = new ConcurrentHashMap<>();

        in.sort(Comparator.comparing(UrAgent::getFirstPos));

        Random random = new Random(System.currentTimeMillis());

        int thcn = density != 1? threads * 10 : threads;

        HashSet<Integer> threadsids = new HashSet<>();

        if (density != 1) {
            while (threadsids.size() <= (density <= 0.5 ? thcn * density - 1 : thcn * (1 - density) - 1))
                threadsids.add(random.nextInt(thcn-1));
            if (density > 0.5)
                for (int k = 0; k < thcn*density - 1; k++)
                    if (threadsids.contains(k)) threadsids.remove(k);
                        else threadsids.add(k);
        } else
            for  (int k = 0; k < thcn*density - 1; k++)
                threadsids.add(k);


        for (int l = 0; l < in.size(); l += attentionSize) {

            int finalL = l;
            List<UrAgent> inpl = in.stream()
                    .filter(agent -> agent.getFirstPos() >= finalL && agent.getPoints().getLast().getPosition() < finalL + attentionSize).toList();

            Map<Integer, List<UrAgent>> inPointsLists = inpl.stream().collect(Collectors.groupingBy(agent -> (Integer) agent.getPoints().getFirst().getToken()));


            LinkedList<CompletableFuture<ConcurrentHashMap<String, Integer> >> cfl = new LinkedList<>();
            final long[] nn = {0};
            int[][] tokenPoints = new int[inpl.size()][];
            int tokenPointsOffset = inpl.getFirst().getFirstPos();
            for (UrAgent la: inpl)
                tokenPoints[inPointsLists.get(la.getPoints().getFirst().getToken()).getFirst().getFirstPos() - tokenPointsOffset]
                        = inPointsLists.get(la.getPoints().getFirst().getToken()).stream().mapToInt(UrAgent::getFirstPos).toArray();

            if (!inPointsLists.isEmpty()) {

                long idxset[][] = new long[attentionSize][];
                int inPoints[] = new int[idxset.length];

                Map<Integer, long[]> lvm = inPointsLists.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().getFirst().getIdx().stream().mapToLong(v-> Integer.toUnsignedLong(v)).toArray()));

                int jv = 0;
                for (UrAgent ina: inpl){
                    int t = inPoints[jv] = (Integer) ina.getPoints().getFirst().getToken();
                    idxset[jv] = lvm.get(t);
                    jv++;
                }

                for (int k: threadsids) {
                    cfl.add(CompletableFuture.supplyAsync(() -> {
                        int n = 0;

                        int idxOffsets[] = new int[idxset.length];

                        for (int i = 0; i < idxset.length; i++)
                            idxOffsets[i] = Math.toIntExact(k * idxset[i].length / threadsids.size());

                        ConcurrentHashMap<String, Integer> tagents = new ConcurrentHashMap<>();
                        int pointsPos[][] = new int[attentionSize][];
                        long tokenPos[] = new long[attentionSize];

                        int isFinished = 0;
                        int pmx  =  0, pmy = 0; pointsPos[0] = tokenPoints[0];
                        while (isFinished < idxOffsets.length){

                            int ix = 0; long i = Long.MAX_VALUE;

                            for (int ii = 0; ii < attentionSize; ii++)
                                if (idxOffsets[ii] >= 0) {
                                    if (i > idxset[ii][idxOffsets[ii]]) {
                                        i = idxset[ii][idxOffsets[ii]];
                                        ix = ii;
                                    }
                                }

                            tokenPos[pmx] = i; pointsPos[pmx] = tokenPoints[ix];
                            idxOffsets[ix]++;

                            if (i - tokenPos[pmy] < attentionSize) {
                                pmx = (pmx + 1) % attentionSize;
                                continue;
                            }

                            if (pmy < pmx ? pmx - pmy > 1 : attentionSize - pmx - pmy > 1)
                             for (int r: relationTypes){
                                if (r == UrRelationInterface.RELATION_POINTED){
                                    String sig = "[" + pointsPos[0];
                                    for (int x = 1; x < pmx; x++ )
                                        if (pointsPos[x][0] - pointsPos[0][0] == tokenPos[x] - tokenPos[0])
                                            sig = sig +" ,"+ pointsPos[x];

                                    sig = sig+"]";
                                    tagents.compute(sig, (ks,v)-> v == null? 1: v+1);
                                } else
                                if (r == UrRelationInterface.RELATION_ORDER) {
                                    String sig = "[" + pointsPos[0];
                                    for (int x = 1; x < pmx; x++ )
                                       //if (sign(pointsPos[x], pointsPos[0]) == sign(tokenPos[x], tokenPos[0]))
                                            sig = sig +" ,"+ pointsPos[x];

                                    sig = sig+"]";
                                    tagents.compute(sig, (ks,v)-> v == null? 1: v+1);
                                }
                            }

                            if (idxOffsets[ix] > Math.toIntExact((k+1) * idxset[ix].length / threadsids.size())) {
                                isFinished++;
                                idxOffsets[ix] = -1;
                            }

                            n++;
                            pmy = pmx;
                        }

                        nn[0] += n;
                        return tagents;
                    }));
                }
                cfl.forEach(c -> c.join().forEach(threads, (k, v) ->
                        sagents.compute(k, (ka, va) -> va == null ? v : va + v)
                        ));
            }
        }

        List<UrAgent> al = new ArrayList<>(agents.values());

        al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

        // увеличиваем частоту и индекс более коротких агентов за счет более длинных включающих в себя короткие
        int i = 1;
        for (UrAgent a1: al) {
            if (a1.getPoints().size() > 1)
                for (Iterator<UrAgent> iterator = al.listIterator(i); iterator.hasNext(); ){
                    UrAgent a2 = iterator.next();
                    if (a1 != a2)
                     if (a1.getRelation() == a2.getRelation()) {
                        BitSet bs = BitSet.valueOf(a1.getFields().toLongArray());
                        bs.and(a2.getFields());
                        if (a1.getFields().cardinality() == bs.cardinality()) {
                            a1.getIdx().or(a2.getIdx());
                            a1.setF(a1.getIdx().getCardinality());
                        }
                    }
                }
            i++;
        }

        List<UrRelationInterface> rels = Arrays.stream(relations).filter(r-> r.getRelationType() != UrRelationInterface.RELATION_POINTED).toList();

        for (UrAgent a1: al)
         if (a1.getPoints().size() > 1 && a1.getRelation() == UrRelationInterface.RELATION_POINTED){
             for (UrRelationInterface relation: rels) {
                 UrAgent a2 = agents.get(relation.makeHash(a1.getPoints()));
                 if (a2 != null) a1.addMr(a2.getMr());
             }
         }

        al.addAll(in.stream().map(a->{
            UrAgent an = a.clone();
            an.setRelation(UrRelation.RELATION_POINTED);
            return an;
        }).toList());


        return al.parallelStream().filter(a->a.getRelation() == UrRelationInterface.RELATION_POINTED).toList();
    }

    private void incAgentF(@NotNull UrAgent agent, long index){
        if (agent.getPoints().size() > 0)
            if (!agent.getIdx().contains(index, index+1))
        {
            agent.incF(1);
            agent.addIndex(index);
        }
    }

    public static <T, K> List<T> uniqueByProperty(List<T> list, Function<T, K> keyExtractor) {
        return new ArrayList<>(
                list.stream()
                        .collect(Collectors.toMap(
                                keyExtractor,  // функция для извлечения ключа
                                obj -> obj,    // значение - сам объект
                                (existing, replacement) -> existing // при коллизиях выбрать первый
                        ))
                        .values()
        );
    }

    private static int sign(long x, long y){
        return x > y ? 1: x < y? -1: 0;
    }


}
