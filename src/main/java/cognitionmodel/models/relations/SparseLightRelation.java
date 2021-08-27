package cognitionmodel.models.relations;

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


public class SparseLightRelation extends LightRelation {

    private static ConcurrentHashMap<String, Integer> terminalsMap = new ConcurrentHashMap<>();
    private static ArrayList<String> terminalsArray = new ArrayList<>();
    private static Integer aInteger = getAddTerminal(new TupleElement("").toString());

    private int labelindex;


    public SparseLightRelation(int labelindex) {
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

        LinkedList<Integer> sig = new LinkedList<>();

        int i = 0;
        for (TupleElement t: tuple) {
            if ((labelindex == i) | !(t.getValue().toString().equals("0") | (t.getValue().toString().equals("0.0")))) {
                sig.add(i);
                sig.add(getAddTerminal(t.getValue().toString()));
            }
            i++;
        }
        i = 0;
        int[] r = new int[sig.size()];
        for (int j: sig)
            r[i++] = j;

        return r;
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

        int j = 0, l = 0;
        for (int i = 0; j < signature.length & i < pattern.getSetAmount() & l < r.length; ){
            if (signature[j] < pattern.getSet()[i]) j += 2;
            else
            if ((signature[j] > pattern.getSet()[i])) i++;
            else {
                r[l++] = signature[j++];
                r[l++] = signature[j++];
            }

        }

        return Arrays.copyOf(r, l);
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
        int[] r = new int[pattern.getSetAmount()*2];

        int j = 0, l = 0;
        for (int i = 0; j < signature.length & i < pattern.getSetAmount() & l < r.length; ){
            if (signature[j] < pattern.getSet()[i]) j += 2;
                else
                    if ((signature[j] > pattern.getSet()[i])) i++;
                        else {
                            r[l++] = signature[j++];
                            r[l++] = signature[j++];
                    }

        }

        if (l > 2)
            return Arrays.copyOf(r, l);
        else
            return new int[]{};
    }

    /**
     * Add term to relation
     *
     * @param signature - relation signature
     * @param index - term index
     * @param term - term
     */

    public int[] addTermToRelation(int[] signature, int index, int term) {
        int[] s = new int[signature.length + 2];
        int i = 0, j = 0;

        for (i = 0; i < signature.length & index < signature[i];) {
            s[i] = signature[i++];
            s[i] = signature[i++];
        }

        if (index == signature[i]) j = i;
            else j = i + 2;

        s[i++] = index;
        s[i++] = term;

        for (;i < signature.length;) {
            s[j++] = signature[i++];
            s[j++] = signature[i++];
        }

        return Arrays.copyOf(s, j);
    }

    /**
     * Remove term from relation
     * @param signature - relation signature
     * @param index - term index
     */

    public int[] removeTermFromRelation(int[] signature, int index){
        int[] s = new int[signature.length];
        int j = 0;

        for (int i = 0; i < signature.length; )
            if (signature[i] != index) {
                s[j++] = s[i++];
                s[j++] = s[i++];
            } else
                i += 2;

        return Arrays.copyOf(s, j);
    }




}
