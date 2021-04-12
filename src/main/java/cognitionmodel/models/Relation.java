package cognitionmodel.models;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.io.Serializable;
import java.util.LinkedList;

public interface Relation extends Serializable {

    /**
     *
     * @return list of tuples indices
     */

    public LinkedList<Integer> getTuplesIndices();

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


    public static byte[] makeSignature(Tuple tuple) {
        return null;
    };

    /**
     *
     * @return the frequency of the relation in data set
     */

    public int getFrequency();

    /**
     * Signature identifies relation in Map
     * @return - the relation signature
     */
    public byte[] getSignature();


    /**
     * Retrieves terminals from relation
     * @return - subset of terminals founded out in relation
     *
     *
     */

    public static Tuple getTerminals(byte[] signature) {
        return null;
    };

    /**
     * Override this method for relation realization
     * @param terminal - terminal for checking out
     * @return true if terminal is in relation
     */

    public abstract boolean isConsists(TupleElement terminal);

    /**
     *
     * @return the number of terminals that are in relation
     */

    public int getLength();

    /**
     * Adds new tuple in tuplesIndices that contains relation
     * @param tupleIndex
     * @return frequency of the relation in data set
     */

    public int addTuple(int tupleIndex);


}
