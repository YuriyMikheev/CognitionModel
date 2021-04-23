package cognitionmodel.models;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Light relation saves nothing.
 * Serialization returns zero byte array
 *
 */

public class LightRelation implements Relation {

    private static HashMap<String, Integer> terminalsMap = new HashMap<>();
    private static ArrayList<String> terminalsArray = new ArrayList<>();
    private static Integer aInteger = getAddTerminal(new TupleElement("").toString());

    /**
     * LightRelation do not save indices of tuples.
     * @return null
     */

    @Override
    public LinkedList<Integer>  getTuplesIndices() {
        return null;
    }


    /**
     *
     * @return zero size byte array
     */


    public byte[] serialize(){

        return new byte[0];
    }

    public void deserialize(byte[] serializedRelation){


    }


    /**
     * Light Relation do not save signature
     * @return - null
     */


    public int[] getSignature(){
        return null;
    }


    /**
     * Retrieves terminals from relation
     * @return - subset of terminals founded out in relation
     *
     *
     */

    public Tuple getTerminals(int[] signature){
        IntBuffer b = IntBuffer.allocate(signature.length).put(signature).position(0);
        LinkedList<TupleElement> t = new LinkedList<>();

        for (int i = 0; i < signature.length / Integer.BYTES; i++)
            t.add(new TupleElement(terminalsArray.get(b.get())));

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


    private static synchronized void addTerminal(String terminal){
        terminalsMap.put(terminal, (Integer) terminalsArray.size());
        terminalsArray.add(terminal);
    }

    private static Integer getAddTerminal(String terminal){
        if (!terminalsMap.containsKey(terminal))
            addTerminal(terminal);
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


    public int[] makeSignature(Tuple tuple) {

        IntBuffer intBuffer = IntBuffer.allocate(tuple.size());

        for (TupleElement t: tuple){
            intBuffer.put(getAddTerminal(t.getValue().toString()));
        }

        return intBuffer.array();
    }

    @Override
    public int addTuple(int tupleIndex) {
        return 0;
    }

    /**
     * Provides access to terminals
     * @return
     */

    public static ArrayList<String> getTerminalsArray() {
        return terminalsArray;
    }
}
