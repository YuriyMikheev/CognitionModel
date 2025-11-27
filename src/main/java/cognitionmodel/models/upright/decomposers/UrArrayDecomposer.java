package cognitionmodel.models.upright.decomposers;

import cognitionmodel.models.inverted.index.BatchedIterator;
import cognitionmodel.models.upright.UrTextDataSet;
import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.models.upright.relations.UrRelation;
import cognitionmodel.models.upright.relations.UrRelationInterface;
import cognitionmodel.patterns.FullGridIterativePatterns;
import cognitionmodel.patterns.Pattern;
import cognitionmodel.patterns.PatternSet;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.FastAggregation;
import org.roaringbitmap.ParallelAggregation;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import jdk.incubator.vector.*;

import static java.lang.Math.*;

public class UrArrayDecomposer {


    int threads = 20;
    double minMrDelta = 0.1;
    float overlap = 1;
    long batchSize = 1000000, dataSetSize = 0;
    private UrRelationInterface relations[];
    private int[] relationTypes;
    private UrTextDataSet dataSet;


    public UrArrayDecomposer(double minMrDelta, long batchSize, long dataSetSize) {
        this(minMrDelta, batchSize, dataSetSize, new UrRelationInterface[]{  new UrRelation(UrRelationInterface.RELATION_SIMILARITY, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_ORDER, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_POINTED, dataSetSize)});
    }

    public UrArrayDecomposer(UrTextDataSet dataSet, int ... relationTypes) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.dataSetSize = dataSet.getTextTokens().size();
        this.dataSet = dataSet;
        this.relationTypes = relationTypes;

        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSetSize)).collect(Collectors.toList()).toArray(new UrRelationInterface[0]);
    }



    public UrArrayDecomposer(double minMrDelta, long batchSize, long dataSetSize, UrRelationInterface... relations) {
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
        final long[] nn = {0};
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

            if (!inPointsLists.isEmpty()) {

                //RoaringBitmap idx = ParallelAggregation.or(in.stream().filter(agent -> agent.getFirstPos() >= finalL && agent.getPoints().getLast().getPosition() < finalL + attentionSize).map(a-> a.getIdx()).collect(Collectors.toList()));

                RoaringBitmap idx = ParallelAggregation.or(
                        in.stream()
                                .filter(agent -> agent.getFirstPos() >= finalL && agent.getPoints().getLast().getPosition() < finalL + attentionSize)
                                .map(a -> a.getIdx())
                                .collect(Collectors.toList()).toArray(new RoaringBitmap[]{})
                );



                //Map<Integer, int[]> lvm = inPointsLists.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst().getPoints().stream().mapToInt(v -> v.getPosition()).toArray()));

                Map<Integer, int[]> lvm = inPointsLists.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .flatMapToInt(el -> el.getPoints().stream().mapToInt(v -> v.getPosition()))
                                        .toArray()
                        ));

                long threadstep = idx.last() / threadsids.size();


                for (int k: threadsids) {
                    cfl.add(CompletableFuture.supplyAsync(() -> {
                        int n = 0;
                        ConcurrentHashMap<String, Integer> tagents = new ConcurrentHashMap<>();


                        RoaringBitmap range = idx.selectRange(k*threadstep, (k+1)*threadstep);
                        if (range.isEmpty()) return tagents;

                        BatchedIterator idxIterator = new BatchedIterator(range);

                        if (!idxIterator.hasNext()) return tagents;

                        BatchedIterator oiterator = idxIterator.clone();

                        long oidx = oiterator.next();
                        LinkedList<Long> ipoints = new LinkedList<>();

                        do {
                            long tidx = idxIterator.next();
                            if (tidx - oidx >= attentionSize) {
                                if (ipoints.size() > 1 && ipoints.getLast() - ipoints.getFirst() < attentionSize) {

                                    int li = 0; StringBuilder s = new StringBuilder();
                                    for (long ip: ipoints) {
                                        s.append(ip - oidx).append(":")
                                                .append(Arrays.stream(lvm.get(dataSet.get(ip)))
                                                        .mapToObj(String::valueOf)
                                                        .collect(Collectors.joining(",")));
                                        if (li++ <= ipoints.size()) s.append(";");
                                    }

                                    tagents.compute(s.toString(), (ks,v)-> (Integer) (v == null? 1: v+1));

                                }
                                while (tidx - oidx > attentionSize) {
                                    oidx = oiterator.next();
                                    ipoints.poll();
                                }
                            }
                            ipoints.add(tidx);
                        } while (idxIterator.hasNext() && n++ < threadstep + attentionSize);

                        nn[0] += n;

                        return tagents;
                    }));
                }
                cfl.forEach(c -> c.join().forEach(threads, (k, v) ->
                        sagents.compute(k, (ka, va) -> va == null ? v : va + v)
                        ));
            }
        }

        sagents.forEach(threads, (k,v)-> {
            if (v > 1)
                for (UrAgent a: pointString2Agents(k, in))
                    if (a.getLength() > 1)
                        agents.compute(a.getAgentHash(), (ka,va) -> {
                            if (va == null) va = a;
                            va.incF(v);
                            return va;
                        });
        });

        List<UrAgent> al = new ArrayList<>(agents.values());

        al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

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


    private List<UrAgent> pointString2Agents(String pointString, List<UrAgent> in) {

        LinkedList<UrAgent> result = new LinkedList<>();

        String[] points = pointString.split(";");
        ArrayList<UrPoint> relin = new ArrayList<>();
        for (String point: points){
            String[] ps = point.split(":");
            int tidx = Integer.parseInt(ps[0]);
            for (String pis: ps[1].split(",")) {
                int inidx = Integer.parseInt(pis);
                relin.add(new UrPoint(inidx, in.get(inidx), tidx));
            }
        }

        relin.sort(Comparator.comparing(UrPoint::getPosition));

        if  (relin.size() > 1)
            for (UrRelationInterface relation : relations)
                for (UrAgent a : relation.makeDecomposition(relin, 0))
                    if (a.getLength() > 1) {
                        result.add(a);
                    }

        return result;
    }


}
