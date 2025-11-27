package cognitionmodel.models.upright.relations;

import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrRelation implements UrRelationInterface {

    private UrRelationInterface relation;


    public UrRelation(int relationType, long datasetsize) {


        if (relationType == RELATION_POINTED) {
            relation = new UrPointedRelation(datasetsize);
        }

        if (relationType == RELATION_SIMILARITY) {
            relation = new UrSimilarityRelation(datasetsize);
        }

        if (relationType == RELATION_ORDER) {
            relation = new UrOrderRelation(datasetsize);
        }
    }

    @Override
    public List<UrAgent> makeDecomposition(UrAgent pointsAgent){
        return relation.makeDecomposition(pointsAgent);
    }

    @Override
    public List<String> makeGeneration(UrAgent agent){
        return relation.makeGeneration(agent);
    }

    @Override
    public List<UrAgent> makeDecomposition(List<UrPoint> points, long startPos) {
        return relation.makeDecomposition(points, startPos);
    }


    @Override
    public String makeHash(List<UrPoint> points) {
        return relation.makeHash(points);
    }

    @Override
    public int getRelationType() {
        return relation.getRelationType();
    }

    @Contract(pure = true)
    public static UrRelationInterface @NotNull [] allRelations(long datasetsize){
        UrRelationInterface[] relations = new UrRelationInterface[4];
        for (int i = 0; i < 4; i++) {
            relations[i] = new UrRelation(i, datasetsize);
        }
        return relations;
    }

    public String toString(){
        return relation.toString();
    }

}
