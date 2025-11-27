package cognitionmodel.models.upright.decomposers;

import cognitionmodel.models.inverted.index.BatchedIterator;
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

public class UrDirectDecomposer {


    int threads = 20;
    double minMrDelta = 0.1;
    float overlap = 1;
    long batchSize = 1000000, dataSetSize = 0;
    private UrRelationInterface relations[];
    private UrTextDataSet dataSet;

    public UrDirectDecomposer(double minMrDelta, long batchSize, long dataSetSize) {
        this(minMrDelta, batchSize, dataSetSize, new UrRelationInterface[]{  new UrRelation(UrRelationInterface.RELATION_SIMILARITY, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_ORDER, dataSetSize),
                new UrRelation(UrRelationInterface.RELATION_POINTED, dataSetSize)});
    }

    public UrDirectDecomposer(UrTextDataSet dataSet, int ... relationTypes) {
        this.minMrDelta = minMrDelta;
        this.batchSize = batchSize;
        this.dataSetSize = dataSet.getTextTokens().size();
        this.dataSet = dataSet;

        relations = Arrays.stream(relationTypes).mapToObj(t-> new UrRelation(t, dataSetSize)).collect(Collectors.toList()).toArray(new UrRelationInterface[0]);
    }



    public UrDirectDecomposer(double minMrDelta, long batchSize, long dataSetSize, UrRelationInterface... relations) {
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
        ConcurrentHashMap<Long, List<UrAgent>> indexmap = new ConcurrentHashMap<>();

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
            Map<Integer, List<UrAgent>> inPointsLists = in.stream()
                    .filter(agent -> agent.getFirstPos() >= finalL && agent.getPoints().getLast().getPosition() < finalL + attentionSize)
                    .collect(Collectors.groupingBy(agent -> (Integer) agent.getPoints().getFirst().getToken()));

///            RoaringBitmap idx = RoaringBitmap.or(inPointsLists.values().stream().map(ls -> ls.getFirst().getIdx()).iterator());
            RoaringBitmap idx = RoaringBitmap.or(in.stream().filter(agent -> agent.getFirstPos() >= finalL && agent.getPoints().getLast().getPosition() < finalL + attentionSize).map(a-> a.getIdx()).iterator());

            LinkedList<CompletableFuture<ConcurrentHashMap<String, UrAgent> >> cfl = new LinkedList<>();
            final long[] nn = {0};

            if (!inPointsLists.isEmpty()) {

                long stepsize = Integer.toUnsignedLong(idx.last()) / thcn;

                for (int k: threadsids) {
                    long finalK = k;
                    cfl.add(CompletableFuture.supplyAsync(() -> {
                        int n = 0;
                        LinkedList<UrPoint> points = new LinkedList<>();

                        RoaringBitmap indexsubmap = idx.selectRange(finalK * stepsize, (finalK + 1) * stepsize);

                        if (indexsubmap.isEmpty()) return new ConcurrentHashMap<>();

                        Iterator<Long> iterator = new BatchedIterator(indexsubmap);
                        ConcurrentHashMap<String, UrAgent> tagents = new ConcurrentHashMap<>();

                        while (iterator.hasNext()){
                            long i = iterator.next();
                            List<UrAgent> p = inPointsLists.get(dataSet.get(i));
                            points.add(new UrPoint(-1, p, i));

                            while (points.getLast().getTag() - points.getFirst().getTag() >= attentionSize) {
                                LinkedList<List<UrPoint>> nps = new LinkedList<>();
                                for (UrAgent fal : (List<UrAgent>) points.getFirst().getToken()) {
                                    LinkedList<UrPoint> npsl = new LinkedList<>();
                                    npsl.add(new UrPoint(fal.getFirstPos(), fal, points.getFirst().getTag()));
                                    nps.add(npsl);
                                }

                                points.poll();

                                int j = 0;
                                for (UrPoint pl:points)
                                  if (j++ < points.size() - 1) { // не брать последний point он точнго за пределами
                                    for (UrAgent al: (List<UrAgent>) pl.getToken()) {
                                        for (List<UrPoint> npsl: nps)
                                            npsl.add(new UrPoint(al.getFirstPos(), al, pl.getTag()));
                                    }
                                }

                                for (List<UrPoint> npsl: nps){
                                    List<UrPoint> np = npsl.stream().filter(pt -> sign(npsl.getFirst().getPosition(), pt.getPosition()) == sign(npsl.getFirst().getTag(), pt.getTag())).toList();

                                    if (np.size() > 1) {
                                        long ti = np.getFirst().getTag();
                                        for (UrRelationInterface relation : relations)
                                            for (UrAgent a : relation.makeDecomposition(np, ti))
                                                if (a.getLength() > 1) {
                                                    long finalTi = ti;
                                                    tagents.compute(a.getAgentHash(), (kh, v) -> {
                                                        if (v == null) return v = a;
                                                        v.getIdx().add(finalTi, finalTi + 1);
                                                        return v;
                                                    });

                                                }
                                    }
                                }
                            }
                            n++;
                        }

                        nn[0] += n;
                        return tagents;
                    }));
                }
                cfl.forEach(c -> c.join().forEach(threads, (k, v) ->
                        agents.compute(k, (ka, va) -> va == null ? v : UrRelationInterface.addAgent(va, v)
                        )));
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
