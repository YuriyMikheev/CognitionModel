package cognitionmodel.models;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Represents lightest realization of relation. Saves just frequency of the relation in data set.
 * Related terminals are in signature.
 *
 *
 */

public class LightRelation implements Relation {

    private static HashMap<String, Integer> terminalsMap = new HashMap<>();
    private static ArrayList<String> terminalsArray = new ArrayList<>();

    private int frequency;

    public LightRelation(){
        getAddTerminal(new TupleElement("").toString());
    }


    /**
     * LightRelation do not save indices of tuples.
     * @return null
     */

    @Override
    public LinkedList<Integer>  getTuplesIndices() {
        return null;
    }



    @Override
    public int getFrequency(){
        return frequency;
    }

    public byte[] serialize(){

        ByteBuffer r = ByteBuffer.allocate(Integer.BYTES ).putInt(frequency);

        return r.array();
    }

    public void deserialize(byte[] serializedRelation){

        ByteBuffer r = ByteBuffer.allocate(serializedRelation.length).put(serializedRelation);

        frequency = r.getInt();
    }


    /**
     * Light Relation do not save signature
     * @return - null
     */


    public byte[] getSignature(){
        return null;
    }


    /**
     * Retrieves terminals from relation
     * @return - subset of terminals founded out in relation
     *
     *
     */

    public static Tuple getTerminals(byte[] signature){
        ByteBuffer b = ByteBuffer.allocate(signature.length).put(signature).position(0);
        LinkedList<TupleElement> t = new LinkedList<>();

        for (int i = 0; i < signature.length / Integer.BYTES; i++)
            t.add(new TupleElement(terminalsArray.get(b.getInt())));

        return new Tuple(t);
    };

    /**
     * Light relation do not save terminal list
     */

    public boolean isConsists(TupleElement terminal) {
        return false;
    };

    /**
     * Light relation do not save signature. So this length is undefined here.
     * The length is in the Model.relationMap Key
     */

    @Override
    public int getLength(){
        return 0;
    }

    @Override
    public int addTuple(int tupleIndex) {
        return frequency++;
    }


    private static Integer getAddTerminal(String terminal){
        if (!terminalsMap.containsKey(terminal)){
            synchronized (terminalsMap) {
                synchronized (terminalsArray) {
                    terminalsMap.put(terminal, terminalsArray.size());
                    terminalsArray.add(terminal);
                }
            }
        }
        return terminalsMap.get(terminal);
    }

    /**
     * Makes relation signature from tuple.
     * The method defines the way of content of relation to be transferred to signature in Model.RelationMap
     * Signature is identifier of the relation in the Map. It is used for fast searching relation in the map.
     *
     *
     * @param tuple - data
     * @return - signature
     */


    public static byte[] makeSignature(Tuple tuple) {

        ByteBuffer b = ByteBuffer.allocate(tuple.size() * Integer.BYTES);

        for (TupleElement t: tuple){
            b.putInt(getAddTerminal(t.get().toString()));
        }

        return b.array();
    };


}
