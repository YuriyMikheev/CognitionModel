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
 */

public class BasicRelation implements Relation {

    private int patternIndex;
    private LinkedList<Integer> tupleIndices = new LinkedList<>();


    /**
     * Relation builder. Should be implemented in inheriting classes
     * @return the relation object
     */

    public static BasicRelation of(){
        return new BasicRelation();
    };


    public static BasicRelation fromTuplesIndecies(int patternIndex, int[] tuplesIndexes) {

        BasicRelation basicRelation = of();
        basicRelation.patternIndex = patternIndex;
        for(int i: tuplesIndexes)
            basicRelation.tupleIndices.add(i);

        return basicRelation;
    }

    public static BasicRelation fromTuplesList(int patternIndex, LinkedList<Integer> tupleIndices) {

        BasicRelation basicRelation = of();
        basicRelation.patternIndex = patternIndex;
        basicRelation.tupleIndices = tupleIndices;
        return basicRelation;
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
     * Retrieves terminals from relation
     *
     * @return - subset of terminals founded out in relation
     */

    public static Tuple getTerminals(byte[] signature) {
        return null;
    }


    /**
     *
     * @return the relation length calculated form signature
     *
     * Its recommended to redefine method to speed up Z calculations
     */


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

    @Override
    public int[] makeSignature(Tuple tuple) {

        return null;
    }

    @Override
    public Tuple getTerminals(int[] signature) {
        return null;
    }


}
