package cognitionmodel.models.upright;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class UprightTextComposer  {

    private int length;


 
    private class UrCompositionIndex {
        ArrayList<UrComposition> urCompositions = new ArrayList<>();
        BitSet[] index ;

        private boolean changed = true;

        public UrCompositionIndex(int length) {
            index = new BitSet[length];
            reindex();
        }

        private void reindex() {
            if (!changed) return;
            index = new BitSet[index.length];

            for (int i = 0; i < index.length; i++)
                index[i] = new BitSet();

            urCompositions.sort(Comparator.comparing(UrComposition::getMr).reversed());

            int idx = 0;
            for (UrComposition urComposition : urCompositions) {
                for (int i : urComposition.getFields().stream().toArray())
                    index[i].set(idx);
                idx++;
            }

            changed = false;
        }

        public void add(UrComposition urComposition) {
            urCompositions.add(urComposition);
            changed = true;
        }

        public List<UrComposition> get(BitSet fields) {
            reindex();
            LinkedList<UrComposition> result = new LinkedList<>();

            BitSet rset = new BitSet();
            rset.set(0, urCompositions.size());

            for (int nextbit = fields.nextSetBit(0); nextbit >= 0 & nextbit < index.length; nextbit = fields.nextSetBit(nextbit + 1))
                rset.andNot(index[nextbit]);

            for (int nextbit = rset.nextSetBit(0); nextbit >= 0 & nextbit < urCompositions.size(); nextbit = rset.nextSetBit(nextbit + 1))
                result.add(urCompositions.get(nextbit));

            return result;
        }

        public UrComposition getMax(BitSet fields) {
            reindex();

            BitSet rset = new BitSet();
            rset.set(0, urCompositions.size());

            for (int nextbit = fields.nextSetBit(0); nextbit >= 0 & nextbit < index.length; nextbit = fields.nextSetBit(nextbit + 1))
                rset.andNot(index[nextbit]);

            int nextbit = rset.nextSetBit(0);
            return nextbit < 0 ? null : urCompositions.get(nextbit);
        }


    }

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

    private UrComposition composeToBestUrComposition(LinkedList<UrAgent> UrAgentList, HashMap<String, UrAgent> zeroMap){
        List<UrComposition> cl = composeToSortedList(UrAgentList);
        return cl.isEmpty() ? null: cl.get(0);
    }

  

    public List<UrComposition> composeToSortedList(Collection<UrAgent> urAgents){
        PriorityQueue<UrComposition> bestUrCompositions = new PriorityQueue<>(Comparator.comparing(UrComposition::getMr).reversed());
        UrCompositionIndex urCompositionIndex = new UrCompositionIndex(length);

        for (UrAgent urAgent : urAgents)
            if (urAgent != null) {
                UrComposition urComposition = new UrComposition(urAgent);
                bestUrCompositions.add(urComposition);
                urCompositionIndex.add(urComposition);
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
                    List<UrComposition> UrCompositionL1 = urCompositionIndex.get(UrComposition.getFields());
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


    private boolean addToQ(UrComposition UrComposition, PriorityQueue<UrComposition> q, HashMap<String, UrComposition> maxUrCompositions){
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
