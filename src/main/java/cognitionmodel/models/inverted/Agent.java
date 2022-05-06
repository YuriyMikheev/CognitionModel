package cognitionmodel.models.inverted;

import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.NaN;
import static java.lang.Math.log;
import static java.lang.Math.round;

/**
 * Class Agent performs Z increasing by changing agent's relation
 */

public class Agent implements Cloneable {
    public TreeMap<String, Point> relation = new TreeMap<String, Point>();
    public HashMap<String, HashSet<String>> relationByField = new HashMap<String, HashSet<String>>();

    HashMap<String, RoaringBitmap> recordsByField = new HashMap<>(); // records common for all points from relation
    RoaringBitmap records = null;//new RoaringBitmap(); // records common for all points from relation
    String signature = "";
    private double z = NaN, p = NaN, cp = NaN;
    public BitSet fields = new BitSet();
    public double dZ = NaN;
    private int length = 0;
    private InvertedTabularModel model;
    private ArrayList<Integer> index = null;
    private boolean hasPerdictingField = false;
    private HashMap<String, Double> condPcashe = new HashMap<>();



    /**
     * Creates Agent for starting point
     * @param startPoint - {integer field index, object representing field value}
     */

    public Agent(Point startPoint, InvertedTabularModel model) {

        this.model = model;

/*        for (Map.Entry<String, TreeMap<Object, RoaringBitmap>> entry: model.invertedIndex.getEntrySet()) {
           // recordsByField.put(entry.getKey(), new RoaringBitmap());
            relationByField.put(entry.getKey(), new HashSet<>());
        }*/
        if (startPoint != null) {
            addPoint(startPoint);
        }
    }

    protected void resign(){
//        signature = relation.keySet().stream().sorted().collect(Collectors.joining("\t"));
        for (String s: relation.keySet())
            signature = signature + "\t"+s;
        z = NaN; p = NaN; cp = NaN; index = null;
        // getMR(records);

    }

    public RoaringBitmap getRecords() {
        return records;
    }
/*
    public Agent clone(){
        Agent c = new Agent(null, model);
        c.mergewith(this);
        return c;
    }*/

    public boolean hasPerdictingField() {
        return hasPerdictingField;
    }

    public void setPerdictingField(boolean hasPerdictingField) {
        this.hasPerdictingField = hasPerdictingField;
    }

    public ArrayList<Integer> getIndex() {
        if (index == null){
            index = new ArrayList<>();

            for (int i = records.first(); i >= 0; i = (int) records.nextValue(i+1)) {
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

        for (Map.Entry<String, RoaringBitmap> e: recordsByField.entrySet()) {
            RoaringBitmap b = e.getValue();
            if (relationByField.get(e.getKey()).size() > 1) //& invertedIndex.get(e.getKey()).size()*epsilon > 1)
                p = p * (1+((double) b.getCardinality() / model.getDataSet().size()));
        }

        return cp = 1 - (p == 0?0: p - 1);
    }

    /**
     * Gets probability of the relation
     * @return
     */
    public double getP(){
        if (records == null) return 0;
        return (Double.isNaN(p)?(p=(double)records.getCardinality()/model.getDataSet().size()): p);
    }

    /**
     * Gets conditional probability of field value if agent relation was appeared
     * @param field
     * @return
     */

    public double getCondP(String field){

        Double r = condPcashe.get(field);
        if (r != null) return r;

        getP();

        RoaringBitmap rc  = new RoaringBitmap();

        rc.add(0, round(model.getDataSet().size()));

        for (Map.Entry<String, RoaringBitmap> e: recordsByField.entrySet())
            if (!field.equals(e.getKey()) & !e.getValue().isEmpty())
                rc.and(e.getValue());

        r = (double) records.getCardinality() / rc.getCardinality();

        condPcashe.put(field, r);

        return r;
    }


    /**
     * Adds new point to agent
     * @param point  - new point
     */

    public void addPoint(Point point){
       // records.add(0, round(model.getDataSet().size()));
        relation.put(point.toString(), point);
        if (!relationByField.containsKey(point.getField())) relationByField.put(point.getField(), new HashSet<>());
        relationByField.get(point.field).add(point.toString());

        if (model.getIndexes().getMap(point.getField()).get(point.getValue()) != null) {
         //   recordsByField.get(point.field).or((RoaringBitmap) model.getIndexes().getMap(point.getField()).get(point.getValue()));
            recordsByField.put(point.field, ((RoaringBitmap) model.getIndexes().getMap(point.getField()).get(point.getValue())));

            if (records == null) {
                records = new RoaringBitmap();
                records.or(((RoaringBitmap) model.getIndexes().getMap(point.getField()).get(point.getValue())));
            } else
                records.and(((RoaringBitmap) model.getIndexes().getMap(point.getField()).get(point.getValue())));


            int i = 0;
            for (Map.Entry<String, TreeMap<Object, RoaringBitmap>> entry: model.invertedIndex.getEntrySet()) {
                if (entry.getKey().equals(point.getField())) {
                    fields.set(i);
                    break;
                }
                i++;
            }
/*            int i = 0;
            for (RoaringBitmap b: recordsByField.values()) {
                if (!b.isEmpty()) {
                    if (records != b)
                        records.and(b);
                    fields.set(i, true);
                }
                i++;
            }*/
            resign();
        }
    }

    public String getSignature() {
        return signature;
    }

    /**
     * Calculates regularity measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     * @return - regularity measure for the relation
     */

    public double getMR(){
        if (Double.isNaN(z))
            return z = getMR(this.records);
        else return z;
    }

    public double getMR(RoaringBitmap records){
        if (relation.size() < 1) return 0;
        if (records == null) return 0;
        if (records.isEmpty()) return 0;
        if (recordsByField.size() < 2) return 0;

        double z = records.getCardinality(), f = 1;
        int c = 1, l = 0;
        for (RoaringBitmap fieldrecords: recordsByField.values())
            if (!fieldrecords.isEmpty()){
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / model.getDataSet().size();
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
        for (RoaringBitmap fieldrecords: recordsByField.values())
            if (!fieldrecords.isEmpty()){
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / model.getDataSet().size();
                    c++;
                }
            }

        double ps = log(f) - (l - c) * log(model.getDataSet().size());
        return ps >= 0;
    }

    public RoaringBitmap mergewith(Agent agent){

        records.add(0, round(model.getDataSet().size()));
        fields.or(agent.fields);
        for (Point point: agent.relation.values())
            if (!relation.containsKey(point.field))
            {

                relation.put(point.toString(), point);
                relationByField.get(point.field).add(point.toString());

                if (model.getIndexes().getMap(point.getField()).get(point.getValue()) != null) {
                    recordsByField.put(point.field, (RoaringBitmap) model.getIndexes().getMap(point.getField()).get(point.getValue()));
/*
                    for (RoaringBitmap b: recordsByField.values())
                        if (!b.isEmpty() & records != b)
                            records.and(b);*/
                }
            }

        resign();
        return records;
    }


    public static Agent merge(Agent a1, Agent a2, InvertedTabularModel model) {
        Agent r = new Agent(null, model);

        for (Point p: a1.relation.values()) {
            r.relation.put(p.toString(), p);
            if (!r.relationByField.containsKey(p.getField())) r.relationByField.put(p.getField(), new HashSet<>());
            r.relationByField.get(p.field).add(p.toString());
        }

        for (Point p: a2.relation.values()) {
            r.relation.put(p.toString(), p);
            if (!r.relationByField.containsKey(p.getField())) r.relationByField.put(p.getField(), new HashSet<>());
            r.relationByField.get(p.field).add(p.toString());
        }

        r.resign();

/*        if (model.agentsindex.containsKey(r.signature))
            return (Agent) model.agentsindex.get(r.signature);*/

        for (String f: model.invertedIndex.getFields()) {
/*            RoaringBitmap bs = r.recordsByField.get(f);

            bs.or(a1.recordsByField.get(f));
            bs.or(a2.recordsByField.get(f));
*/
            if (a1.recordsByField.containsKey(f)) r.recordsByField.put(f,a1.recordsByField.get(f));
            if (a2.recordsByField.containsKey(f)) r.recordsByField.put(f,a2.recordsByField.get(f));
        }

        r.fields.or(a1.fields);
        r.fields.or(a2.fields);


     //   List<RoaringBitmap> fm = new LinkedList<>(); fm.addAll(r.recordsByField.values());
    //    Collections.sort(fm,Comparator.comparing(RoaringBitmap::getCardinality).reversed());

/*        int i=0;
        for (RoaringBitmap bs: fm)
            if (!bs.isEmpty())
                if (i++ == 0) r.records.or(bs);
                    else
                        r.records.and(bs);*/

        r.records = new RoaringBitmap();
        r.records.or(a1.records);
        r.records.and(a2.records);

//        model.agentsindex.put(r.getSignature(), r);

        r.dZ = r.getMR() - a1.getMR() - a2.getMR();

        return r;
    }

    public InvertedIndex initIndex(InvertedTabularModel model){
        return new BitInvertedIndex(model);
    }

}