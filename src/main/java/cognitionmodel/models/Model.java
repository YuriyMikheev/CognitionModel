package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.Pattern;
import cognitionmodel.patterns.PatternSet;

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

public abstract class Model<R extends Relation> {
    private DataSet dataSet;
    private Map<byte[], R> relationsMap;
    private Set<R> terminals;
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

    /**
     * Sets RelationMap object.
     */

    public abstract void setRelationMap();

    public DataSet getDataSet() {
        return dataSet;
    }


    public Relation get(byte[] signature) {
        return relationsMap.get(signature);
    }

    public void putRelation(byte[] signature, R relation){
        relationsMap.put(signature, relation);
    }

    public void putRelation(Tuple tuple, R relation){
        relationsMap.put(R.makeSignature(tuple), relation);
    }

    /**
     * Gets the relation form the relation map by set of terminals and puts tuple to it.
     * @param terminals - set of terminals
     * @param tupleIndex - index of the added tuple in data set
     */

    public void addRecordToRelation(Tuple terminals, int tupleIndex){

        byte[] s = R.makeSignature(terminals);

        if (relationsMap.containsKey(s)) {
            relationsMap.get(s).addTuple(tupleIndex);
        }

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

    public double getZ(R relation){
        try{
            return getZ(relation.getSignature());
        } catch (NullPointerException e){
            throw new IllegalArgumentException();
        }
    }

    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param signature - relation signature from map
     * @return - Z value for the relation
     */

    public double getZ(byte[] signature){

        Relation relation;

        try{
             relation = relationsMap.get(signature);
        } catch (NullPointerException e){
            throw new IllegalArgumentException();
        }

        double z = relation.getFrequency(), f = 1;
        int c = 1;

        for (TupleElement t: R.getTerminals(signature)) {
            f = f * getDataSet().getFrequency(t);
            if (f > Double.MAX_VALUE/1000) { //prevents double value overloading
                f = f / getDataSet().size();
                c++;
            }
        }

        z = Math.log(z / f) - (relation.getLength() - c) * Math.log(getDataSet().size());

        return z;

    }

    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param tuple - set of terminals in relation
     * @return - Z value for the relation
     */


    public double getZ(Tuple tuple){
        try {
            return getZ(R.makeSignature(tuple));
        } catch (IllegalArgumentException e){
            return 0;
        }
    }


}
