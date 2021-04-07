package cognitionmodel.models;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Relation is a pair of pattern and set of tuples.
 *
 *
 */

public class Relation implements Serializable {

    private int patternIndex;
    private LinkedList<Integer> tuples = new LinkedList<>();

    public Relation(int patternIndex, int[] tuplesIndex) {
        this.patternIndex = patternIndex;
        for(int i: tuplesIndex)
            this.tuples.add(i);
    }

    public Relation(int patternIndex, LinkedList<Integer> tuples) {
        this.patternIndex = patternIndex;
        this.tuples = tuples;
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

}
