package cognitionmodel.models.inverted;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.NaN;
import static java.lang.Math.log;
import static java.lang.Math.round;

/**
 * Class Agent performs Z increasing by changing agent's relation
 */

public class BitAgent implements Cloneable {
    public HashMap<String, Point> relation = new HashMap<String, Point>();
    public HashMap<String, HashSet<String>> relationByField = new HashMap<String, HashSet<String>>();
    HashMap<String, BitSet> recordsByField = new HashMap<String, BitSet>(); // records common for all points from relation
    BitSet records = new BitSet(); // records common for all points from relation
    String signature = "";
    private double z = NaN, p = NaN, cp = NaN;
    public BitSet fields = new BitSet();
    public double dZ = NaN;
    private int length = 0;
    private InvertedBitTabularModel model;
    private ArrayList<Integer> index = null;

    /**
     * Creates Agent for starting point
     * @param startPoint - {integer field index, object representing field value}
     */

    public BitAgent(Point startPoint, InvertedBitTabularModel model){

        this.model = model;

        for (Map.Entry<String, TreeMap<Object, BitSet>> entry: model.invertedIndex.entrySet()) {
            recordsByField.put(entry.getKey(), new BitSet());
            relationByField.put(entry.getKey(), new HashSet<>());
        }
        if (startPoint != null) {
            addPoint(startPoint);
        }
    }

    protected void resign(){
        signature = relation.keySet().stream().sorted().collect(Collectors.joining("\t"));
/*            for (String s: relation.keySet().stream().sorted().collect(Collectors.toList()))
                signature = signature + "\t"+s;*/
        z = NaN; p = NaN; cp = NaN;
        // getMR(records);

    }

    public BitSet getRecords() {
        return records;
    }

    public BitAgent clone(){
        BitAgent c = new BitAgent(null, model);
        c.mergewith(this);
        return c;
    }


    public ArrayList<Integer> getIndex() {
        if (index == null){
            index = new ArrayList<>();

            for (int i = records.nextSetBit(0); i >= 0; i = records.nextSetBit(i+1)) {
                // operate on index i here
                if (i == Integer.MAX_VALUE) {
                    break; // or (i+1) would overflow
                }
                index.add(i);
            }
        }

        return index;
    }

    public String toString(){
        return signature+"\t"+relation.size()+"; "+recordsByField.values().stream().filter(b -> !b.isEmpty()).count()+"; "+getMR();
    }

    public int getLength(){
        return (length == 0? length = fields.cardinality(): length);
    }

    /**
     * Gets confidential probability of the agents subspace that equal production of all confidential intervals probabilities included in agent
     * @return - confidential probability of the agent
     */

    public double getConfP(){
        double p = 1, f = 0 ;

        if(!Double.isNaN(cp)) return cp;

        for (Map.Entry<String, BitSet> e: recordsByField.entrySet()) {
            BitSet b = e.getValue();
            if (relationByField.get(e.getKey()).size() > 1) //& invertedIndex.get(e.getKey()).size()*epsilon > 1)
                p = p * (1+((double) b.cardinality() / b.size()));
        }

        return cp = 1 - (p == 0?0: p - 1);
    }

    /**
     * Gets probability of the relation
     * @return
     */
    public double getP(){
        return (Double.isNaN(p)?(p=(double)records.cardinality()/model.getDataSet().size()): p);
    }

    /**
     * Gets conditional probability of field value if agent relation was appeared
     * @param field
     * @return
     */

    public double getCondP(String field){

        getP();

        BitSet rc  = new BitSet();

        rc.set(0, (int) round(model.getDataSet().size()), true);

        for (Map.Entry<String, BitSet> e: recordsByField.entrySet()) {
            if (!field.equals(e.getKey()) & !e.getValue().isEmpty()) {
                rc.and(e.getValue());
            }
        }

        return (double) records.cardinality() / rc.cardinality();
    }


    /**
     * Adds new point to agent
     * @param point  - new point
     * @return - set of records actual for agent
     */

    public BitSet addPoint(Point point){
        records.set(0, (int) round(model.getDataSet().size()), true);
        relation.put(point.toString(), point);
        relationByField.get(point.field).add(point.toString());

        if (model.getIndexes(point) != null) {
            recordsByField.get(point.field).or(model.getIndexes(point));

            int i = 0;
            for (BitSet b: recordsByField.values()) {
                if (!b.isEmpty()) {
                    if (records != b)
                        records.and(b);
                    fields.set(i, true);
                }
                i++;
            }
            resign();
        }
        return records;
    }

    public String getSignature() {
        return signature;
    }

    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     * @return - Z value for the relation
     */

    public double getMR(){
        if (Double.isNaN(z))
            return z = getMR(this.records);
        else return z;
    }

    public double getMR(BitSet records){
        if (relation.size() < 1) return 0;
        if (records.isEmpty()) return 0;
        if (recordsByField.size() < 2) return 0;

        double z = records.cardinality(), f = 1;
        int c = 1, l = 0;
        for (BitSet fieldrecords: recordsByField.values())
            if (!fieldrecords.isEmpty()){
                f = f * fieldrecords.cardinality();
                l++;
                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / records.size();
                    c++;
                }
            }

        z = log(z / f) + (l - c) * log(model.getDataSet().size());

        return z;
    }

    /**
     * Checks if the relation represented by agent is possible
     * Relation is impossibli if sum(log(p(cj)) <= log(1/N), N is number of records in dataset
     * @return true if relation is possible
     */

    public boolean isPossible(){
        if (relation.size() < 1) return false;
        if (records.isEmpty()) return false;

        double f = 1;
        int c = 1, l = 0;
        for (BitSet fieldrecords: recordsByField.values())
            if (!fieldrecords.isEmpty()){
                f = f * fieldrecords.cardinality();
                l++;
                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / records.size();
                    c++;
                }
            }

        double ps = log(f) - (l - c) * log(model.getDataSet().size());
        return ps >= 0;
    }

    public BitSet mergewith(BitAgent agent){

        records.set(0, (int) round(model.getDataSet().size()), true);
        fields.or(agent.fields);
        for (Point point: agent.relation.values())
            if (!relation.containsKey(point.field))
            {

                relation.put(point.toString(), point);
                relationByField.get(point.field).add(point.toString());

                if (model.getIndexes(point) != null) {
                    recordsByField.get(point.field).or(model.getIndexes(point));

                    for (BitSet b: recordsByField.values())
                        if (!b.isEmpty() & records != b)
                            records.and(b);
                }
            }

        resign();
        return records;
    }

}