package cognitionmodel.models.upright.relations;

import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public interface UrRelationInterface {
    int RELATION_UNDEFINED = 0;
    int RELATION_POINTED = 1;
    int RELATION_SIMILARITY = 2;
    int RELATION_ORDER = 3;

    List<UrAgent> makeDecomposition(UrAgent pointsAgent);

    List<String> makeGeneration(UrAgent agent);

    List<UrAgent> makeDecomposition(List<UrPoint> points, long startPos);

    String makeHash(List<UrPoint> points);

    int getRelationType();

    @Contract("_, _ -> param1")
    public static @NotNull UrAgent addAgent(@NotNull UrAgent agent1, @NotNull UrAgent agent2){
        //agent1.incF(agent2.getF());
        agent1.getIdx().or(agent2.getIdx());
        agent1.setF(agent1.getIdx().getCardinality());
        return agent1;
    }
}
