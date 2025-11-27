package cognitionmodel.models.upright.composers;


import cognitionmodel.models.upright.agent.UrAgent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class UrTextComposer {

    private int length;
    private int threads = 20;


    public UrTextComposer(int maxN, int length) {
        this.maxN = maxN;
        this.length = length;
    }

    private int maxN = Integer.MAX_VALUE;

    public int getMaxN() {
        return maxN;
    }

    public void setMaxN(int maxN) {
        this.maxN = maxN;
    }

    private LinkedList<UrAgent> composeToList(LinkedList<UrAgent> UrAgentList, HashMap<String, UrAgent> zeroMap) {
        UrComposition c = composeToBestUrComposition(UrAgentList, zeroMap);
        return c == null ? UrAgentList : c.getUrAgents();
    }

    private @Nullable UrComposition composeToBestUrComposition(LinkedList<UrAgent> UrAgentList, HashMap<String, UrAgent> zeroMap){
        List<UrComposition> cl = composeToSortedList(UrAgentList);
        return cl.isEmpty() ? null: cl.get(0);
    }

    public List<UrComposition> composeToSortedList(@NotNull Collection<UrAgent> urAgents, int attentionSize, int amount){

        List<UrAgent> agents = urAgents.stream().sorted(Comparator.comparing(UrAgent::getFirstPos)).toList();

        ConcurrentHashMap<Integer, List<UrAgent>> compTasks = new ConcurrentHashMap<>();

        for (UrAgent a: agents){
            int i = a.getFirstPos()/attentionSize;
            if (!compTasks.containsKey(i)) compTasks.put(i, new LinkedList<>());
            compTasks.get(i).add(a);
        }

        List<UrComposition> results[] = new ArrayList[compTasks.size()];


        compTasks.forEach(threads, (k,v)->{
            results[k] = new ArrayList<>();
            results[k].addAll(composeToSortedList(v));
        });


        for (int i = 0; i < results.length; i++) {
            List<UrComposition> comps = results[i];

            // Найти максимальный getMr
            double maxMr = comps.stream()
                    .mapToDouble(UrComposition::getMr)
                    .max()
                    .orElse(Double.NEGATIVE_INFINITY);

            // Оставить только с максимальным getMr
            List<UrComposition> maxMrList = comps.stream()
                    .filter(c -> c.getMr() == maxMr)
                    .collect(Collectors.toList());

            // Найти максимальный getS среди максимальных getMr
            double maxS = maxMrList.stream()
                    .mapToDouble(UrComposition::getS)
                    .max()
                    .orElse(Double.NEGATIVE_INFINITY);

            // Оставить только с максимальным getS среди элементов с максимальным getMr
            List<UrComposition> filteredList = maxMrList.stream()
                    .filter(c -> c.getS() == maxS)
                    .collect(Collectors.toList());

            // Заменяем на отфильтрованный
            results[i] = filteredList;
        }

        //for (int i = 0; i < amount; i++)
            for (List<UrComposition> cl: results)
                if (cl != results[0].get(0))
                    results[0].get(0).add(cl.get(0));


        return results[0];




    }



    public List<UrComposition> composeToSortedList(@NotNull Collection<UrAgent> urAgents){
        PriorityQueue<UrComposition> bestUrCompositions = new PriorityQueue<>(Comparator.comparing(UrComposition::getMr).reversed());
        UrCompositionIndexInterface compositionDirectIndex = new UrCompositionDirectIndex(length);

        for (UrAgent urAgent : urAgents)
            if (urAgent != null) {
                UrComposition urComposition = new UrComposition(urAgent);
                bestUrCompositions.add(urComposition);
                compositionDirectIndex.add(urComposition);
            } else
                System.err.println("Composer: null agent found");


        PriorityQueue<UrComposition> nbc = null, onbc = null;
        boolean f = false;
        HashMap<String, UrComposition> maxUrCompositions = new HashMap<>();

        do {
            nbc = new PriorityQueue<>(Comparator.comparing(UrComposition::getMr).reversed()); f = false;
            while (!bestUrCompositions.isEmpty()) try {
                UrComposition UrComposition = bestUrCompositions.poll(); //nbc.add(UrComposition);
                if (UrComposition.getFields().cardinality() < length) {

                    boolean fl = false;
                    List<UrComposition> UrCompositionL1 = compositionDirectIndex.get(UrComposition.getFields());
                    if (!UrCompositionL1.isEmpty()) {
                        for (Iterator<UrComposition> ci = UrCompositionL1.iterator(); ci.hasNext(); ) {
                            UrComposition oc = UrComposition.clone();
                            if (oc.add(ci.next())) {
                                fl = fl || addToQ(oc, nbc, maxUrCompositions);//true;
                            }
                        }
                        f = fl || f;
                    }
                    if (!fl) {
                        addToQ(UrComposition, nbc, maxUrCompositions);
                    }
                } else {
                    addToQ(UrComposition, nbc, maxUrCompositions);
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }

            bestUrCompositions.clear();
            while (!nbc.isEmpty() && bestUrCompositions.size() < maxN)
                bestUrCompositions.add(nbc.poll());

        } while (f);

        return maxUrCompositions.values().stream().sorted(Comparator.comparing(UrComposition::getMr).reversed()).collect(Collectors.toList());
    }


    private boolean addToQ(@NotNull UrComposition UrComposition, PriorityQueue<UrComposition> q, @NotNull HashMap<String, UrComposition> maxUrCompositions){
//        String cs = UrComposition.getUrAgents().stream().sorted((a1, a2) -> a1.getFields().nextSetBit(0) < a2.getFields().nextSetBit(0) ? -1:1).map(UrAgent::getPoints).collect(Collectors.toList()).toString();
        String cs = UrComposition.getFields().toString();
        if (maxUrCompositions.get(cs) == null) {
            maxUrCompositions.put(cs, UrComposition);
            q.add(UrComposition);
        } else
        if (maxUrCompositions.get(cs).getMr() < UrComposition.getMr()) {
            q.add(UrComposition);
            maxUrCompositions.put(cs, UrComposition);
        } else {
            if (maxUrCompositions.get(cs).getMr() != UrComposition.getMr()) q.add(maxUrCompositions.get(cs));
            return false;
        }

        return true;
    }




}
