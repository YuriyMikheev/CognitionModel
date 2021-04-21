package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.patterns.Pattern;
import cognitionmodel.patterns.PatternSet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Abstract class represents model of data set. Consists map of relations produced from data set (D) and pattern set (P).
 * Model = {d*p -> relation}
 * Terminals are basic relations that represent minimal size relation. For example, letter of text or values range
 *
 * Realization should define to set up maps objects
 * - setMaps()
 */

public abstract class Model<R extends Relation> {
    private DataSet dataSet;
    protected Map<int[], R> relationsMap = null;
    protected Map<int[], Integer> frequencyMap;
   // private Map<int[], Long> terminalsFrequencies;
    protected PatternSet patternSet = null;
    protected R relationMethods;


    /**
     * Creates model object
     * @param dataSet - data for model
     */

    public Model(DataSet dataSet, R relationInstance) {
        try {
            relationMethods = (R) relationInstance.getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        setMaps();

        setDataSet(dataSet);

    }


    protected void setDataSet(DataSet dataSet){
        this.dataSet = dataSet;

        for (Tuple t: dataSet){
            int[] sign = relationMethods.makeSignature(t);
            for (int i = 0; i < sign.length; i++) {
                int[] s = new int[]{i,sign[i]};
                if (frequencyMap.containsKey(s))
                    frequencyMap.put(s, frequencyMap.get(s) + 1);
                else frequencyMap.put(s, 1);
            }
        }
    }

    /**
     * Sets RelationsMap object.
     */

    public abstract void setMaps();

    public DataSet getDataSet() {
        return dataSet;
    }


    public Relation get(byte[] signature) {
        return relationsMap.get(signature);
    }

    public void putRelation(int[] signature, R relation){
        relationsMap.put(signature, relation);
    }

    public void putRelation(Tuple tuple, R relation){
        relationsMap.put(relationMethods.makeSignature(tuple), relation);
    }

    /**
     * Gets the relation form the relation map by set of terminals and puts tuple to it.
     * @param terminals - set of terminals
     * @param tupleIndex - index of the added tuple in data set
     */

    public void addRecordToRelation(Tuple terminals, int tupleIndex){

        int[] s = relationMethods.makeSignature(terminals);
        addRecordToRelation(s,tupleIndex);

    }


    /**
     * Puts tuple to relation object in the process of making model.
     * @param signature - relation signature
     * @param tupleIndex - index of the added tuple in data set
     *
     *
     * The method should be multithread safe.
     *
     */

    public abstract void addRecordToRelation(int[] signature, int tupleIndex);

    /**
     * Increments frequency of the relation appearance
     * @param signature
     */

    public void incFrequency(int[] signature){
        if (frequencyMap.containsKey(signature))
            frequencyMap.put(signature, frequencyMap.get(signature) + 1);
        else
            frequencyMap.put(signature, 1);
    }


    public PatternSet getPatternSet() {
        return patternSet;
    }


    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param signature - relation signature from map
     * @return - Z value for the relation
     */

    public double getZ(int[] signature){

        Integer zf = frequencyMap.get(signature);
        if (zf == null) return 0;

        double z = zf, f = 1;
        int c = 1, i = 0;


        for (int t: signature) {
            f = f * frequencyMap.get(new int[]{i++,t});
            if (f > Double.MAX_VALUE/1000) { //prevents double value overloading
                f = f / getDataSet().size();
                c++;
            }
        }

        z = Math.log(z / f) - (signature.length - c) * Math.log(getDataSet().size());

        return z;

    }

    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param tuple - set of terminals in relation
     * @return - Z value for the relation
     */


    public double getZ(Tuple tuple){
        try {
            return getZ(relationMethods.makeSignature(tuple));
        } catch (IllegalArgumentException e){
            return 0;
        }
    }

    public void setPatternSet(PatternSet patternSet) {
        this.patternSet = patternSet;
    }

    /**
     * Generates the list of relations signatures from data in tuple using PatternSet
     * @param tuple
     * @return LinkedList object containing a new signatures
     */

    public LinkedList<int[]> generateRelations(Tuple tuple){
        int[] sign = relationMethods.makeSignature(tuple);

        return generateRelations(sign);
    }

    /**
     * Generates the list of relations signatures from data in signature using PatternSet
     * @param signature
     * @return LinkedList object containing a new signatures
     */


    public LinkedList<int[]> generateRelations(int[] signature){

        if (patternSet == null) {
            System.err.println("Pattern set is not defined");
        }

        LinkedList<int[]> r = new LinkedList<>();

        for (Pattern p: patternSet) {
            byte[] pb = p.get();
            int i = 0;
            int[] ns = new int[signature.length];
            for (byte b: pb) {
                ns[i] = (b == 1? signature[i]:0);
                i++;
            }
            r.add(ns);
        }

        return r;
    }

    /**
     * Produces the model from data set (D) and pattern set (P).
     * frequencyMap = {d*p -> F(relation)}
     *
     * if relationMap defined
     *    relationMap = {d*p -> relation}
     *
     */


    public void make(){
        int i = 0;

        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();

        for (Tuple tuple: dataSet){
            int finalI = i;
            cfl.add(CompletableFuture.supplyAsync(() -> {
                for (int[] signature: generateRelations(tuple)) {
                    if (relationsMap != null) addRecordToRelation(signature, finalI);
                    incFrequency(signature);
                }
                return null;
            }));
            if (i++ % (int) (dataSet.size() * 0.01 + 1) == 0) {
                cfl.stream().map(m -> m.join()).collect(Collectors.toList());
                cfl.clear();
            }
        }
    }

}
