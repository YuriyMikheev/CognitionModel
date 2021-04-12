package cognitionmodel.models;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Abstract class representing relation.
 * Relation is a pair of pattern and set of tuples from data set.
 *
 * Realizations should define methods:
 * - makeSignature(Tuple);
 * - getTerminals(signature)
 * - isConsists()
 */

public abstract class BasicRelation implements Relation {

    private int patternIndex;
    private LinkedList<Integer> tupleIndices = new LinkedList<>();
    private byte[] signature;

    public BasicRelation(int patternIndex, int[] tuplesIndexes) {
        this.patternIndex = patternIndex;
        for(int i: tuplesIndexes)
            this.tupleIndices.add(i);
    }

    public BasicRelation(int patternIndex, LinkedList<Integer> tupleIndices) {
        this.patternIndex = patternIndex;
        this.tupleIndices = tupleIndices;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    /**
     *
     * @return list of tuples indices
     */

    @Override
    public LinkedList<Integer>  getTuplesIndices() {
        return tupleIndices;
    }


    @Override
    public int getFrequency(){
        return tupleIndices.size();
    }

    public byte[] serialize(){

        ByteBuffer r = ByteBuffer.allocate(Integer.BYTES + tupleIndices.size() * Integer.BYTES).putInt(patternIndex);

        for (int t: tupleIndices)
            r.putInt(t);

        return r.array();
    }

    public void deserialize(byte[] serializedRelation){

        ByteBuffer r = ByteBuffer.allocate(serializedRelation.length).put(serializedRelation);

        patternIndex = r.getInt();

        while (r.position() < serializedRelation.length)
            tupleIndices.add(r.getInt());

    }


    /**
     * Signature identifies relation in Map
     * @return - the relation signature
     */


    public byte[] getSignature(){
        return signature;
    };


    /**
     * Retrieves terminals from relation
     * @return - subset of terminals founded out in relation
     *
     *
     */

    public abstract Tuple getTerminals(byte[] signature);

    /**
     * Override this method for relation realization
     * @param terminal - terminal for checking out
     * @return true if terminal is in relation
     */

    public abstract boolean isConsists(TupleElement terminal);

    /**
     *
     * @return the relation length calculated form signature
     *
     * Its recommended to redefine method to speed up Z calculations
     */


    @Override
    public int getLength(){
        return getTerminals(signature).getTupleElements().size();
    }

    @Override
    public int addTuple(int tupleIndex) {
        tupleIndices.add(tupleIndex);

        return tupleIndices.size();
    }


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


}
