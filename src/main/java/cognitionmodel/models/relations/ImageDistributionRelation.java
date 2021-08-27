package cognitionmodel.models.relations;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.ImageDistributionPattern;
import cognitionmodel.patterns.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.Math.round;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Class represents set of methods for processing signatures and new relations from table data about images.
 * ImageDistributionRelation uses ImageDistributionPattern
 * The relation consist {classindex, pattern.hashCode(), {distribution characteristics}}
 */

public class ImageDistributionRelation extends LightRelation {

    private static ConcurrentHashMap<String, Integer> terminalsMap = new ConcurrentHashMap<>();
    private static ArrayList<String> terminalsArray = new ArrayList<>();
    private static Integer aInteger = getAddTerminal(new TupleElement("").toString());

    private int labelindex;
    private Function<Tuple, int[]> characteristicfunction = null;


    public ImageDistributionRelation(int labelindex, Function<Tuple, int[]> characteristicfunction) {
        this.labelindex = labelindex;
        this.characteristicfunction = characteristicfunction;
    }



    /**
     * ImageDistributionRelation do not save indices of tuples.
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
        Tuple t = new Tuple();

        for (int i: signature)
            t.add(terminalsArray.get(i));

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

    private synchronized static Integer getAddTerminal(String terminal){
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

        int[] r = new int[tuple.size()];

        for (int i = 0; i < tuple.size(); i++)
            r[i] = getAddTerminal(tuple.get(i).getValue().toString());

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
        int[] signature = makeSignature(tuple);
        return makeRelation(signature, pattern);
    }


    /**
     * Generates new relation
     *
     * @param signature - signature of the input data
     * @param pattern - ImageDistributionPattern or its inheritor
     * @return
     */


    @Override
    public int[] makeRelation(int[] signature, Pattern pattern){

        if (!(pattern instanceof ImageDistributionPattern))
            throw new IllegalArgumentException("Pattern for ImageDistributionRelation should be ImageDistributionPattern or its inheritor");

        Tuple t = getTerminals(signature), tt = new Tuple();


        int start = pattern.getSet()[0];
        for (int i = 0; i < pattern.getSetAmount(); i++)
            if (pattern.getSet()[i] + start < t.size())
               tt.add(t.get(pattern.getSet()[i] + start));


        int[] dc = characteristicfunction.apply(tt);

        int[] rs = new int[dc.length + 2];
        rs[0] = signature[0];
        rs[1] = pattern.hashCode();

        for (int i = 0; i < dc.length; i++)
            rs[i+2] = dc[i];

        return rs;
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

    public static int[] average(Tuple tuple, int precision){
        return new int[]{(int)round(Arrays.stream(tuple.asIntArray()).average().getAsDouble()*precision)};
    }

    public static int[] sum(Tuple tuple){
        return new int[]{(int)round(Arrays.stream(tuple.asIntArray()).sum())};
    }

    public static int[] StandardDeviation(Tuple tuple, int precision){
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();

        for (TupleElement t: tuple)
            descriptiveStatistics.addValue(t.asDouble());

        return new int[]{(int)round(descriptiveStatistics.getStandardDeviation()*precision)};
    }

}
