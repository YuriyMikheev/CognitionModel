package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;

import java.util.*;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class IndependentComposer3 implements Composer {

    private int fieldsLength, predictingIndex;


    public IndependentComposer3(int fieldsLength, int predictingIndex){
        this.fieldsLength = fieldsLength;
        this.predictingIndex = predictingIndex;
    }

    @Override
    public HashMap<Object, LinkedList<Agent>> compose(HashMap<Object, LinkedList<Agent>> decomposition){
        HashMap<Object, LinkedList<Agent>> result = new HashMap<Object, LinkedList<Agent>>();

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), compose(re.getValue()));

        return result;
    }

    int maxN = 10;
    private LinkedList<Agent> compose(LinkedList<Agent> agentList){
       /// LinkedList<Composition> compositions = new LinkedList<>();
        PriorityQueue<Composition> bestCompositions = new PriorityQueue<>(Comparator.comparing(Composition::getAbsMr).reversed());
        HashMap<String, Composition> fieldsMap = new HashMap<>();

        for (Agent agent : agentList) {
            bestCompositions.add(new Composition(agent, predictingIndex));
        }

        PriorityQueue<Composition> nbc = null;//new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());

        boolean f = false;
        do {
            nbc = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed()); f = false;
            while (!bestCompositions.isEmpty()) try {
                Composition composition = bestCompositions.poll(); //nbc.add(composition);
                if (composition.getFields().cardinality() < fieldsLength - 1) {
                    Iterator<Composition> iterator = bestCompositions.iterator();
                    boolean fl = false;
                    while (iterator.hasNext()) {
                        Composition composition1 = iterator.next();
                        if (Composition.check(composition, composition1, fieldsLength)){
                            Composition oc = composition.clone();
                            if (oc.add(composition1)) {
                                Composition tc = fieldsMap.get(oc.getFields().toString());
                                if (tc == null) {
                                    fieldsMap.put(oc.getFields().toString(), oc);
                                    nbc.add(oc);
                                }
                                else
                                   if (tc.getMr() >= oc.getMr()) oc = tc;
                                   else {
                                       fieldsMap.put(oc.getFields().toString(), oc);
                                       nbc.add(oc);
                                   }
                                fl = f = true;
                            }
                        }
                    }
                    if (!fl) {
                        nbc.add(composition);
                        fieldsMap.put(composition.getFields().toString(), composition);
                    }
                } else {
                    nbc.add(composition);
                    fieldsMap.put(composition.getFields().toString(), composition);
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }

            bestCompositions.clear();
            while (!nbc.isEmpty() & bestCompositions.size() < maxN)
                bestCompositions.add(nbc.poll());

        } while (f);

/*        PriorityQueue<Composition> compositions = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());
        compositions.addAll(bestCompositions);*/


        return bestCompositions.isEmpty()? agentList: bestCompositions.peek().getAgents();
    }


}
