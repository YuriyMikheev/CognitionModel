package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.patterns.PatternSet;

import java.util.Map;
import java.util.Set;

/**
 * Abstract class represents model of data set. Consists map of relations produced from data set (D) and pattern set (P).
 * Model = {d*p -> relation}
 * Terminals are basic relations that represent minimal size relation. For example, letter of text or values range
 *
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
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public Map getRelationsMap() {
        return relationsMap;
    }

    public PatternSet getPatternSet() {
        return patternSet;
    }

    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param relation - relation from map
     * @return - Z value for the relation
     */

    public double getZ(Relation relation){
        double z = 0;




        return z;
    }

}
