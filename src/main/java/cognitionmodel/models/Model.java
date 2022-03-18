package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.relations.Relation;
import cognitionmodel.patterns.Pattern;
import cognitionmodel.patterns.PatternSet;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.log;

/**
 * Abstract class represents model of data set. Consists map of relations produced from data set (D) and pattern set (P).
 * Model = {d*p -> relation}
 * Terminals are basic relations that represent minimal size relation. For example, letter of text or values range
 *
 * Realization should define to set up maps objects
 * - setMaps()
 */

public abstract class Model<R extends Relation> {
    protected DataSet dataSet;
    protected Map<int[], R> relationsMap = null;
    protected Map<int[], Integer> frequencyMap;
    protected PatternSet patternSet = null;
    protected R relationMethods;
    protected ConcurrentHashMap<String, Integer> termsfrequencies = new ConcurrentHashMap<>();


    /**
     * Creates model object
     */

    public Model() {
        setMaps();
    }


    protected void setDataSet(DataSet dataSet){
        this.dataSet = dataSet;

        for (Tuple t: dataSet){
            int[] sign = relationMethods.makeSignature(t);
            for (int i = 0; i < sign.length; i++)
                termsfrequencies.compute(i + ":" + sign[i], (k, v)  -> (v == null ? 1: v + 1));
        }
    }

    /**
     * Sets RelationsMap object.
     */

    protected abstract void setMaps();

    public R getRelationMethods() {
        return relationMethods;
    }

    public void setRelationMethods(R relationMethods) {
        this.relationMethods = relationMethods;
    }

    public DataSet getDataSet() {
        return dataSet;
    }


    public Relation get(byte[] signature) {
        return relationsMap.get(signature);
    }

    public long getReltionsAmount(){
        return  frequencyMap.size();
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
        try {
            frequencyMap.compute(signature, (k, v) -> (v == null ? 1 : v + 1));
        } catch (IllegalStateException e){

        }
    }

    /**
     * Increments frequency of the relation appearance
     * @param signature
     */

    public int getFrequency(int[] signature){
        Integer f = frequencyMap.get(signature);
        return (f != null? f : 0);
    }


    public PatternSet getPatternSet() {
        return patternSet;
    }


    /**
     * Calculates RM measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param signature - relation signature from map
     * @return - Z value for the relation
     */

    public double getMR(int[] signature){

        Integer zf = frequencyMap.get(signature);
        if (zf == null) return 0;

        double z = zf, f = 1;
        int c = 1, l = 0;


        for (int i = 0; i < signature.length; i++)
            if (signature[i] != 0){
                l++;
                f = f * termsfrequencies.get(i + ":" + signature[i]);
                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / getDataSet().size();
                    c++;
            }
        }

        z = Math.log(z / f) + (l - c) * Math.log(getDataSet().size());

        return z;

    }

    /**
     * Calculates RM measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param tuple - set of terminals in relation
     * @return - RM value for the relation
     */


    public double getMR(Tuple tuple){
        try {
            return getMR(relationMethods.makeSignature(tuple));
        } catch (IllegalArgumentException e){
            return 0;
        }
    }


    /**
     * Faster calculation RM measure = ln(P(relation) + const, P(relation) - probability of relation
     * Is correct if and only if of equal relations length and elements. in this case on prediction stage denominators could be eliminated
     *
     * @param signature - relation signature from map
     * @return - RM value for the relation
     */

    public double getMRfast(int[] signature){

        Integer zf = frequencyMap.get(signature);
        if (zf == null) return 0;

        return log((double)zf) + (signature.length - 1)*log(dataSet.size());

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
        if (patternSet == null) {
            throw new IllegalStateException("Pattern set is not defined");
        }

        LinkedList<int[]> r = new LinkedList<>();
        int[] signature = relationMethods.makeSignature(tuple);

        for (Pattern p: patternSet) {
            int[] nr = relationMethods.makeRelation(signature, p);

            if (nr.length > 0)
                r.add(nr);
        }

        return r;
    }

    /**
     * Generates the list of relations signatures from data in signature using PatternSet
     * @param signature
     * @return LinkedList object containing a new signatures
     */


    public LinkedList<int[]> generateRelations(int[] signature){

        if (patternSet == null) {
            throw new IllegalStateException("Pattern set is not defined");
        }

        LinkedList<int[]> r = new LinkedList<>();

        for (Pattern p: patternSet) {
            r.add(relationMethods.makeRelation(signature, p));
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
            cfl.add(CompletableFuture.supplyAsync(() ->
            {
                for (int[] signature: generateRelations(tuple)) {
                    if (relationsMap != null) addRecordToRelation(signature, finalI);
                    incFrequency(signature);
                }
                return null;
            }));
            if (i++ % (int) (dataSet.size() * 0.01 + 1) == 0 | i == dataSet.size()) {
                cfl.stream().map(m -> m.join()).collect(Collectors.toList());
                cfl.clear();
            }
        }
    }

    /**
     * Gets iterator for relation map
     * @return - iterator for the set of <Entry<int[], Integer>>
     */


    public Iterator<Map.Entry<int[], Integer>> relationIterator(){
        return frequencyMap.entrySet().iterator();
    }

}
