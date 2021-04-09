package cognitionmodel.models;

import cognitionmodel.datasets.Tuple;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Set;

/**
 * Abstract class representing relation.
 * Relation is a pair of pattern and set of tuples from data set.
 *
 *
 */

public abstract class Relation implements Serializable {

    private int patternIndex;
    private LinkedList<Integer> tupleIndices = new LinkedList<>();
    private byte[] signature;

    public Relation(int patternIndex, int[] tuplesIndexes, byte[] signature) {
        this.patternIndex = patternIndex;
        for(int i: tuplesIndexes)
            this.tupleIndices.add(i);

        this.signature = signature;
    }

    public Relation(int patternIndex, LinkedList<Integer> tupleIndices, byte[] signature) {
        this.patternIndex = patternIndex;
        this.tupleIndices = tupleIndices;
        this.signature = signature;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public LinkedList<Integer>  getTuples() {
        return tupleIndices;
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

    public byte[] getSignature() {
        return signature;
    }

    /**
     * Retrieves terminals from relation
     * @return - subset of terminals founded out in relation
     *
     *
     */

    public Tuple getTerminals(){ // TO-DO


        return null;
    }

    /**
     * Override this method for relation realization
     * @param terminal - terminal for checking out
     * @return true if terminal is in relation
     */

    public boolean isConsists(Relation terminal){
        return false;
    }

}
