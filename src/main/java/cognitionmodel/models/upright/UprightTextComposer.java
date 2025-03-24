package cognitionmodel.models.upright;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class UprightTextComposer  {

    private int length;


    public UprightTextComposer(int maxN, int length) {
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
