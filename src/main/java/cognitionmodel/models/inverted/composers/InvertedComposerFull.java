package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class InvertedComposerFull implements Composer {

    private int fieldsLength;
    private int predictingIndex;
    private Predictionfunction predictionfunction;


    public InvertedComposerFull(int fieldsLength, int predictingIndex, Predictionfunction predictionfunction) {
        this.fieldsLength = fieldsLength;
        this.predictingIndex = predictingIndex;
        this.predictionfunction = predictionfunction;
    }

    private class CompositionIndex {
        ArrayList<Composition> compositions = new ArrayList<>();
        BitSet[] index = new BitSet[fieldsLength];

        private boolean changed = true;

        public CompositionIndex() {
            reindex();
        }

        private void reindex() {
            if (!changed) return;
            index = new BitSet[fieldsLength];

/*            for (int i = 0; i < fieldsLength; i++)
                index[i] = new BitSet();*/

            //compositions.sort(Comparator.comparing(Composition::getMr).reversed());

/*            int idx = 0;
            for (Composition composition : compositions) {
                for (int i : composition.getFields().stream().toArray()) {
                    index[i].set(idx);
                }
                idx++;
            }*/


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

            for (int nextbit = fields.nextSetBit(0); nextbit >= 0 & nextbit < fieldsLength; nextbit = fields.nextSetBit(nextbit + 1))
                rset.andNot(index[nextbit]);

            for (int nextbit = rset.nextSetBit(0); nextbit >= 0 & nextbit < compositions.size(); nextbit = rset.nextSetBit(nextbit + 1))
                result.add(compositions.get(nextbit));

            return result;
        }

        public List<Composition> get(Composition composition) {
            reindex();
            LinkedList<Composition> result = new LinkedList<>();


            //for (int nextbit: composition.getFieldsIndex().stream().toArray())
            for (int nextbit = composition.getFieldsIndex().nextSetBit(0); nextbit >= 0 & nextbit < compositions.size(); nextbit = composition.getFieldsIndex().nextSetBit(nextbit + 1))
                    result.add(compositions.get(nextbit));

            return result;
        }

        public Composition getMax(BitSet fields) {
            reindex();

/*            Composition result = new Composition();
            result.setMr(-100000000);*/

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

/*
    private List<Composition> composeToSortedListo(LinkedList<Agent> agentList, HashMap<String, Agent> zeroMap){
        PriorityQueue<Composition> bestCompositions = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());
        CompositionIndex compositionIndex = new CompositionIndex();

        for (Agent agent : agentList) {
            Composition composition = new Composition(agent, predictingIndex, predictionfunction, zeroMap);
            bestCompositions.add(composition);
            compositionIndex.add(composition);
        }

        HashMap<String, Composition> maxCompositions = new HashMap<>();

        PriorityQueue<Composition> nbc = null;
        boolean f = false;
        do {
            nbc = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed()); f = false;
            while (!bestCompositions.isEmpty()) try {
                Composition composition = bestCompositions.poll(); //nbc.add(composition);
                if (composition.getFields().cardinality() < fieldsLength - 1) {

                    boolean fl = false;
                    Composition composition1 = compositionIndex.getMax(composition.getFields());
                    if (composition1 != null) {
                        Composition oc = composition.clone();
                        if (oc.add(composition1)) {

                            //nbc.add(oc);
                            fl = f = addToQ(oc, nbc, maxCompositions);//true;
                        }
                    }
                    if (!fl) {
                        addToQ(composition, nbc, maxCompositions);
                      //  nbc.add(composition);
                    }
                } else {
                    addToQ(composition, nbc, maxCompositions);
                    //nbc.add(composition);
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }

            bestCompositions.clear();
            while (!nbc.isEmpty() & bestCompositions.size() < maxN)
                bestCompositions.add(nbc.poll());

        } while (f);

        return  maxCompositions.values().stream().sorted(Comparator.comparing(Composition::getMr).reversed()).collect(Collectors.toList());//bestCompositions.stream().collect(Collectors.toList());
    }
*/


    private List<Composition> composeToSortedList(LinkedList<Agent> agentList, HashMap<String, Agent> zeroMap){
        PriorityQueue<Composition> bestCompositions = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());
        CompositionIndex compositionIndex = new CompositionIndex();

        for (Agent agent : agentList) {
            Composition composition = new Composition(agent, predictingIndex, predictionfunction, zeroMap);
            bestCompositions.add(composition);
            compositionIndex.add(composition);
        }

        compositionIndex.compositions.sort(Comparator.comparing(Composition::getMr).reversed());

        for (Composition c1 : compositionIndex.compositions) {
            int idx1 = 0;
            for (Composition c2 : compositionIndex.compositions) {
                if (!c1.getFields().intersects(c2.getFields()))
                    c1.getFieldsIndex().set(idx1);
                idx1++;
            }
        }

        PriorityQueue<Composition> nbc = null, onbc = null;
        boolean f = false;
        HashMap<String, Composition> maxCompositions = new HashMap<>();

        do {
            nbc = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed()); f = false;
            while (!bestCompositions.isEmpty())try {
                Composition composition = bestCompositions.poll(); //nbc.add(composition);
                if (composition.getFields().cardinality() < fieldsLength - 1) {
                    boolean fl = false; int i = 0;
                    List<Composition> compositionL1 = compositionIndex.get(composition);
                    if (!compositionL1.isEmpty()) {
                        for (Iterator<Composition> ci = compositionL1.iterator(); ci.hasNext() & nbc.size() < maxN; ) {
                            Composition oc = composition.clone();
                            if (oc.add(ci.next())) {
                                fl = fl || addToQ(oc, nbc, maxCompositions);
                            }
                        }
                        f = fl || f;
                    }
                    if (!fl) {
                        addToQ(composition, nbc, maxCompositions);
                    }
                } else {
                    addToQ(composition, nbc, maxCompositions);
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            bestCompositions = nbc;
        } while (f);

        return maxCompositions.values().stream().sorted(Comparator.comparing(Composition::getMr).reversed()).collect(Collectors.toList());
    }


    private boolean addToQ(Composition composition, PriorityQueue<Composition> q, HashMap<String, Composition> maxCompositions){
//        String cs = composition.getAgents().stream().sorted((a1, a2) -> a1.getFields().nextSetBit(0) < a2.getFields().nextSetBit(0) ? -1:1).map(Agent::getPoints).collect(Collectors.toList()).toString();
        String cs = composition.getFields().toString();
        //composition.getAgents().forEach();stream().map(Agent::getPoints).sorted.collect(Collectors.toList()).toString();
        if (maxCompositions.get(cs) == null) {
            maxCompositions.put(cs, composition);
            q.add(composition);
        } else
        if (maxCompositions.get(cs).getMr() < composition.getMr()) {
            q.add(composition);
            maxCompositions.put(cs, composition);
        } else {
            if (maxCompositions.get(cs).getMr() != composition.getMr()) q.add(maxCompositions.get(cs));
            return false;
        }

        return true;
    }




}
