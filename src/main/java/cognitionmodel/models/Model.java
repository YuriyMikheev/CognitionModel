package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.PatternSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class represents model of data set. Consists map of relations produced from data set (D) and pattern set (P).
 * Model = {d*p -> relation}
 * Terminals are basic relations that represent minimal size relation. For example, letter of text or values range
 *
 * Realization should define
 * - setRelationsMap()
 */

public abstract class Model {
    private DataSet dataSet;
    private Map<byte[], Relation> relationsMap;
    private Set<Relation> terminals;
    private PatternSet patternSet;

    /**
     * Creates model object
     * @param dataSet - data for model
     * @param patternSet - set of patterns
     */

    public Model(DataSet dataSet, PatternSet patternSet) {
        this.dataSet = dataSet;
        this.patternSet = patternSet;
        setRelationMap();
    }

    public abstract void setRelationMap();

    public DataSet getDataSet() {
        return dataSet;
    }

/*    public Map getRelationsMap() {
        return relationsMap;
    }*/

    public Relation get(byte[] signature) {
        return relationsMap.get(signature);
    }

    public void putRelation(byte[] signature, Relation relation){
        relationsMap.put(signature, relation);
    }

    public PatternSet getPatternSet() {
        return patternSet;
    }

    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param relation - relation
     * @return - Z value for the relation
     */

    public double getZ(Relation relation){
        double z = relation.getTuples().size(), p = 1;

        for (TupleElement t: relation.getTerminals())
            p = p * dataSet.getFrequency(t);

        z = Math.log(z / p) - (relation.getLength() - 1) * Math.log(dataSet.size());

        return z;
    }

    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param signature - relation signature from map
     * @return - Z value for the relation
     */

    public double getZ(byte[] signature){
        try{
            return getZ(relationsMap.get(signature));
        } catch (NullPointerException e){
            throw new IllegalArgumentException();
        }
    }


}
