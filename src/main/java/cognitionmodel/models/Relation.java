package cognitionmodel.models;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.io.Serializable;
import java.util.LinkedList;

public interface Relation extends Serializable {

    /**
     * Relation builder. Should be implemented in inheriting classes
     * @return the relation object
     */

    public static Relation of(){
        return null;
    };

    /**
     *
     * @return list of tuples indices
     */

    public abstract LinkedList<Integer> getTuplesIndices();

    /**
     * Makes relation signature from tuple.
     * The method defines the way of content of relation to be transfered to signature in Model.RelationMap
     * Signature is identifier of the relation in the Map. It is used for fast searching relation in the map.
     *
     * The method should be redefined for relation realization.
     *
     * @param tuple - data
     * @return - signature
     */


    public abstract int[] makeSignature(Tuple tuple);


    /**
     * Retrieves terminals from relation
     * @return - subset of terminals founded out in relation
     *
     *
     */

    public abstract Tuple getTerminals(int[] signature);

    /**
     * Adds new tuple in tuplesIndices that contains relation
     * @param tupleIndex
     * @return frequency of the relation in data set
     */

    public abstract int addTuple(int tupleIndex);


}
