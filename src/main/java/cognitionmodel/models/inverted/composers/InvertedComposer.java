package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;

import java.util.*;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class InvertedComposer implements Composer {

    private int fieldsLength, predictingIndex;



    public InvertedComposer(int fieldsLength, int predictingIndex){
        this.fieldsLength = fieldsLength;
        this.predictingIndex = predictingIndex;
    }

    private class CompositionIndex {
        ArrayList<Composition> compositions = new ArrayList<>();
        BitSet[] index = new BitSet[fieldsLength];
      //  HashMap<String, Composition> maxcompositions = new HashMap<>();

        private boolean changed = true;

        public CompositionIndex(){
            reindex();
        }

        private void reindex(){
            if (!changed) return;
            index = new BitSet[fieldsLength];

            for (int i = 0; i < fieldsLength; i++)
                index[i] = new BitSet();

            compositions.sort(Comparator.comparing(Composition::getMr).reversed());

            int idx = 0;
            for (Composition composition: compositions) {
                for (int i : composition.getFields().stream().toArray())
                    index[i].set(idx);
                idx++;
            }

            changed = false;
        }

        public void add(Composition composition) {
            int idx = compositions.size();
            compositions.add(composition);

/*
            Composition tc = maxcompositions.get(composition.getFields().toString());
            if (tc == null) {
                maxcompositions.put(composition.getFields().toString(), composition);
                compositions.add(composition);
            }
                else
                    if (tc.getMr() < composition.getMr())
                        maxcompositions.put(composition.getFields().toString(), composition);
*/

            changed = true;
/*            if (idx < compositions.size())
                for (int i: composition.getFields().stream().toArray())
                    index[i].set(idx);*/
        }

        public List<Composition> get(BitSet fields){
            reindex();
            LinkedList<Composition> result = new LinkedList<>();

            BitSet rset = new BitSet(); rset.set(0, compositions.size());

            for (int nextbit = fields.nextClearBit(0); nextbit >= 0 & nextbit < fieldsLength; nextbit = fields.nextClearBit(nextbit + 1))
                rset.and(index[nextbit]);

            for (int nextbit = rset.nextSetBit(0); nextbit >= 0 & nextbit < compositions.size(); nextbit = rset.nextSetBit(nextbit + 1))
                result.add(compositions.get(nextbit));

            return result;
        }
        public Composition getMax(BitSet fields){
            reindex();

            Composition result = new Composition(); result.setMr(-100000000);
            HashSet<String> ca = new HashSet<>();
            LinkedList<Composition> resultList = new LinkedList<>();


            BitSet rset = new BitSet(); rset.set(0, compositions.size());

            for (int nextbit = fields.nextSetBit(0); nextbit >= 0 & nextbit < fieldsLength; nextbit = fields.nextSetBit(nextbit + 1))
                rset.andNot(index[nextbit]);

/*            for (int nextbit = rset.nextSetBit(0); nextbit >= 0 & nextbit < compositions.size(); nextbit = rset.nextSetBit(nextbit + 1)) {
                Composition nc = compositions.get(nextbit);

                String f = nc.getFields().toString();

                if (!ca.contains(f)) {
                    resultList.add(maxcompositions.get(f));
                    ca.add(f);

                    if (result.getMr() < nc.getMr())
                        result = nc;
                }
            }*/

            int nextbit = rset.nextSetBit(0);
            return  nextbit < 0? null:  compositions.get(nextbit);

            //return result.getAgents().isEmpty()? null: result;
        }


    }

    @Override
    public HashMap<Object, LinkedList<Agent>> compose(HashMap<Object, LinkedList<Agent>> decomposition){
        HashMap<Object, LinkedList<Agent>> result = new HashMap<Object, LinkedList<Agent>>();

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), compose(re.getValue()));

        return result;
    }

    private int maxN = 4000;

    public int getMaxN() {
        return maxN;
    }

    public void setMaxN(int maxN) {
        this.maxN = maxN;
    }

    private LinkedList<Agent> compose(LinkedList<Agent> agentList){
        PriorityQueue<Composition> bestCompositions = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());
        CompositionIndex compositionIndex = new CompositionIndex();

        for (Agent agent : agentList) {
            Composition composition = new Composition(agent, predictingIndex);
            bestCompositions.add(composition);
            compositionIndex.add(composition);
        }

        PriorityQueue<Composition> nbc = null;//new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());

        boolean f = false;
        do {
            nbc = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed()); f = false;
            while (!bestCompositions.isEmpty()) try {
                Composition composition = bestCompositions.poll(); //nbc.add(composition);
                if (composition.getFields().cardinality() < fieldsLength - 1) {
                   // List<Composition> compositionList = compositionIndex.getMax(composition.getFields());

                    boolean fl = false;
                    //for (Composition composition1: compositionList)
                    Composition composition1 = compositionIndex.getMax(composition.getFields());
                    if (composition1 != null)
                    {
                        Composition oc = composition.clone();
                        if (oc.add(composition1)) {
                            nbc.add(oc);
                          //  compositionIndex.add(oc);
                            fl = f = true;
                        }
                    }
                    if (!fl) {
                        nbc.add(composition);
                    }
                } else {
                    nbc.add(composition);
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }

            bestCompositions.clear();
            while (!nbc.isEmpty() & bestCompositions.size() < maxN)
                bestCompositions.add(nbc.poll());

        } while (f);

        return bestCompositions.isEmpty()? agentList: bestCompositions.peek().getAgents();
    }


}
