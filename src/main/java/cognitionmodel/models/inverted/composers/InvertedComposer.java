package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class InvertedComposer implements Composer {

    private int fieldsLength;
    private int predictingIndex;
    private Predictionfunction predictionfunction;


    public InvertedComposer(int fieldsLength, int predictingIndex, Predictionfunction predictionfunction) {
        this.fieldsLength = fieldsLength;
        this.predictingIndex = predictingIndex;
        this.predictionfunction = predictionfunction;
    }

    private class CompositionIndex {
        ArrayList<Composition> compositions = new ArrayList<>();
        BitSet[] index = new BitSet[fieldsLength];
        //  HashMap<String, Composition> maxcompositions = new HashMap<>();

        private boolean changed = true;

        public CompositionIndex() {
            reindex();
        }

        private void reindex() {
            if (!changed) return;
            index = new BitSet[fieldsLength];

            for (int i = 0; i < fieldsLength; i++)
                index[i] = new BitSet();

            compositions.sort(Comparator.comparing(Composition::getMr).reversed());

            int idx = 0;
            for (Composition composition : compositions) {
                for (int i : composition.getFields().stream().toArray())
                    index[i].set(idx);
                idx++;
            }

            changed = false;
        }

        public void add(Composition composition) {
            int idx = compositions.size();
            compositions.add(composition);
            changed = true;
        }

        public List<Composition> get(BitSet fields) {
            reindex();
            LinkedList<Composition> result = new LinkedList<>();

            BitSet rset = new BitSet();
            rset.set(0, compositions.size());

            for (int nextbit = fields.nextClearBit(0); nextbit >= 0 & nextbit < fieldsLength; nextbit = fields.nextClearBit(nextbit + 1))
                rset.and(index[nextbit]);

            for (int nextbit = rset.nextSetBit(0); nextbit >= 0 & nextbit < compositions.size(); nextbit = rset.nextSetBit(nextbit + 1))
                result.add(compositions.get(nextbit));

            return result;
        }

        public Composition getMax(BitSet fields) {
            reindex();

            Composition result = new Composition();
            result.setMr(-100000000);
            HashSet<String> ca = new HashSet<>();

            BitSet rset = new BitSet();
            rset.set(0, compositions.size());

            for (int nextbit = fields.nextSetBit(0); nextbit >= 0 & nextbit < fieldsLength; nextbit = fields.nextSetBit(nextbit + 1))
                rset.andNot(index[nextbit]);

            int nextbit = rset.nextSetBit(0);
            return nextbit < 0 ? null : compositions.get(nextbit);
        }


    }

    private HashMap<String, Agent> getZeroMap(HashMap<Object, LinkedList<Agent>> decomposition){
        HashMap<String, Agent> zeroMap = new HashMap<>();
        LinkedList<Agent> zl = decomposition.size() > 1? decomposition.remove("null"): decomposition.get("null");
        if (zl != null) {
            for (Agent a : zl)
                zeroMap.put(a.getFields().toString(), a);
        }
        return zeroMap;
    }

    @Override
    public HashMap<Object, LinkedList<Agent>> composeToAgentList(HashMap<Object, LinkedList<Agent>> decomposition) {
        HashMap<Object, LinkedList<Agent>> result = new HashMap<Object, LinkedList<Agent>>();
        HashMap<String, Agent> zeroMap = getZeroMap(decomposition);

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), composeToBestComposition(re.getValue(), zeroMap).getAgents());

        return result;
    }

    @Override
    public HashMap<Object, Composition> composeToBestCompositions(HashMap<Object, LinkedList<Agent>> decomposition) {
        HashMap<Object, Composition> result = new HashMap<Object, Composition>();
        HashMap<String, Agent> zeroMap = getZeroMap(decomposition);

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), composeToBestComposition(re.getValue(), zeroMap));

        return result;
    }

    @Override
    public HashMap<Object, List<Composition>> composeToSortedCompositions(HashMap<Object, LinkedList<Agent>> decomposition) {
        HashMap<Object, List<Composition>> result = new HashMap<Object, List<Composition>>();
        HashMap<String, Agent> zeroMap = getZeroMap(decomposition);

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), composeToSortedList(re.getValue(), zeroMap));

        return result;
    }

    public HashMap<Object, List<Composition>> composeToSortedCompositions(HashMap<Object, LinkedList<Agent>> decomposition, Function<Composition, Boolean> compositionFilter) {
        HashMap<Object, List<Composition>> result = new HashMap<Object, List<Composition>>();
        HashMap<String, Agent> zeroMap = getZeroMap(decomposition);

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), composeToSortedList(re.getValue(), zeroMap).stream().filter(c-> compositionFilter.apply(c)).collect(Collectors.toList()));

        return result;
    }

    private int maxN = Integer.MAX_VALUE;

    public int getMaxN() {
        return maxN;
    }

    public void setMaxN(int maxN) {
        this.maxN = maxN;
    }

    private LinkedList<Agent> composeToList(LinkedList<Agent> agentList, HashMap<String, Agent> zeroMap) {
        Composition c = composeToBestComposition(agentList, zeroMap);
        return c == null ? agentList : c.getAgents();
    }

    private Composition composeToBestComposition(LinkedList<Agent> agentList, HashMap<String, Agent> zeroMap){
        List<Composition> cl = composeToSortedList(agentList,  zeroMap);
        return cl.isEmpty() ? null: cl.get(0);
    }

    private List<Composition> composeToSortedList(LinkedList<Agent> agentList, HashMap<String, Agent> zeroMap){
        PriorityQueue<Composition> bestCompositions = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());
        CompositionIndex compositionIndex = new CompositionIndex();

        for (Agent agent : agentList) {
            Composition composition = new Composition(agent, predictingIndex, predictionfunction, zeroMap);
            bestCompositions.add(composition);
            compositionIndex.add(composition);
        }

        PriorityQueue<Composition> nbc = null;
        boolean f = false;
        do {
            nbc = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed()); f = false;
            while (!bestCompositions.isEmpty()) try {
                Composition composition = bestCompositions.poll(); //nbc.add(composition);
                if (composition.getFields().cardinality() < fieldsLength - 1) {

                    boolean fl = false;
                    Composition composition1 = compositionIndex.getMax(composition.getFields());
                    if (composition1 != null)
                    {
                        Composition oc = composition.clone();
                        if (oc.add(composition1)) {
                            nbc.add(oc);
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

        return bestCompositions.stream().collect(Collectors.toList());
    }


}
