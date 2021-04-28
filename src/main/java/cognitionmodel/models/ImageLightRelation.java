package cognitionmodel.models;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.Pattern;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class represents set of methods for processing signatures and new relations from table data about images.
 * Light relation saves nothing.
 *
 */


public class ImageLightRelation extends LightRelation {

    private static ConcurrentHashMap<String, Integer> terminalsMap = new ConcurrentHashMap<>();
    private static ArrayList<String> terminalsArray = new ArrayList<>();
    private static Integer aInteger = getAddTerminal(new TupleElement("").toString());

    private int labelindex;


    public ImageLightRelation(int labelindex) {
        this.labelindex = labelindex;
    }



    /**
     * LightRelation do not save indices of tuples.
     * @return null
     */

    @Override
    public LinkedList<Integer>  getTuplesIndices() {
        return null;
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
        Tuple t = new Tuple();

        int j = 0;
        while (b.hasRemaining()){
            int i = b.get();
            while (j++ != i)
                t.add("");

            t.add(terminalsArray.get(b.get()));
        }

        return t;
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

    public ArrayList<String> getTerminalsArray() {
        return terminalsArray;
    }

    /**
     * Gets index of terminal
     * @param terminal - terminal
     * @return - index
     */

    public int getTerminalIndex(String terminal){
        return terminalsMap.get(terminal);
    }


    /**
     * Generates new relation
     *
     * @param tuple - input data
     * @param pattern
     * @return
     */

    @Override
    public int[] makeRelation(Tuple tuple, Pattern pattern){
        int[] r = new int[tuple.size()];
        int[] signature = makeSignature(tuple);
        return makeRelation(signature, pattern);
    }


    /**
     * Generates new relation
     *
     * @param signature - signature of the input data
     * @param pattern
     * @return
     */


    @Override
    public int[] makeRelation(int[] signature, Pattern pattern){
        int[] r = new int[pattern.getSetAmount()];

        int j = 0;
        for (int i: pattern.getSet())
            if (i < signature.length)
                r[j++] = signature[i];
            else
                return new int[]{};

        return r;
    }

    /**
     * Add term to relation
     *
     * @param signature - relation signature
     * @param index - term index
     * @param term - term
     */

    public int[] addTermToRelation(int[] signature, int index, int term) {
        signature[index] = term;
        return signature;
    }

    /**
     * Remove term from relation
     * @param signature - relation signature
     * @param index - term index
     */

    public int[] removeTermFromRelation(int[] signature, int index){
        signature[index] = 0;
        return signature;
    }




}
