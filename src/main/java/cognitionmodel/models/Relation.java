package cognitionmodel.models;

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
    private LinkedList<Integer> tuples = new LinkedList<>();
    private byte[] signature;

    public Relation(int patternIndex, int[] tuplesIndex, byte[] signature) {
        this.patternIndex = patternIndex;
        for(int i: tuplesIndex)
            this.tuples.add(i);

        this.signature = signature;
    }

    public Relation(int patternIndex, LinkedList<Integer> tuples, byte[] signature) {
        this.patternIndex = patternIndex;
        this.tuples = tuples;
        this.signature = signature;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public LinkedList<Integer>  getTuples() {
        return tuples;
    }

    public byte[] serialize(){

        ByteBuffer r = ByteBuffer.allocate(Integer.BYTES + tuples.size() * Integer.BYTES).putInt(patternIndex);

        for (int t: tuples)
            r.putInt(t);

        return r.array();
    }

    public void deserialize(byte[] serializedRelation){

        ByteBuffer r = ByteBuffer.allocate(serializedRelation.length).put(serializedRelation);

        patternIndex = r.getInt();

        while (r.position() < serializedRelation.length)
            tuples.add(r.getInt());

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
     * @param terminals - set of all possible terminals
     * @return - subset of terminals founded out in relation
     *
     *
     */

    public Set<Relation> getTerminals(Set<Relation> terminals){ // TO-DO


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
