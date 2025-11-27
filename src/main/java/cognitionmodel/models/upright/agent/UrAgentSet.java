package cognitionmodel.models.upright.agent;

import cognitionmodel.models.upright.relations.UrRelationInterface;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/** UrAgentSet - supports saving and retrieving UrAgents from Set
 *
 */

public class UrAgentSet {

    private HashMap<String, UrAgent> agentHashMap = new HashMap<>();
    private TreeMap<String, UrAgent> agentTree = new TreeMap<>();
    private TreeMap<String, UrAgent> agentLeftTree = new TreeMap<>();

    private HashSet<UrRelationInterface> relations = new HashSet<>();

    public UrAgentSet(UrRelationInterface @NotNull ... relations){
        for (UrRelationInterface r: relations)
            this.relations.add(r);
    }

    public UrAgent get(UrRelationInterface relation, String @NotNull ... agentHashes){
        String ah  = relation+": " + (agentHashes.length > 1 ? Arrays.stream(agentHashes).toList().toString(): agentHashes[0]);
        return agentHashMap.get(ah);
    }

    public UrAgent get(UrRelationInterface relation, UrAgent agent){
        String ah = getHash(relation, agent);
        UrAgent ao = agentHashMap.get(ah);
        return ao;
    }

    public List<UrAgent> get(UrAgent agent){
        return relations.stream().map(r -> get(r,agent)).toList();
    }

    public void put(@NotNull UrAgent agent){

        for (UrRelationInterface relation: relations) {
            String ah = getHash(relation, agent);
            UrAgent ao = agentHashMap.get(ah);

            if (ao != null) {
                ao.getIdx().or(agent.getIdx());
                ao.forceRecalcMr();
            } else {
                ao = agent;
                agentHashMap.put(ah, ao);
                agentTree.put(ah, ao);
                agentTree.put(getLeftHash(relation,agent), ao);
            }
        }
    }

    private @NotNull String getHash(@NotNull UrRelationInterface relation, UrAgent agent){
        return relation + ": " + relation.makeGeneration(agent);
    }

    private @NotNull String getLeftHash(@NotNull UrRelationInterface relation, UrAgent agent){
        return relation + ": " + relation.makeGeneration(agent).stream().sorted(Comparator.reverseOrder()).toList();
    }

    public void save(OutputStream stream){

    }

    public void load(InputStream stream){

    }

    public long size(){
        return agentHashMap.size();
    }

}
