package cognitionmodel.models.inverted;

import org.roaringbitmap.RoaringBitmap;

import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.log;
import static java.lang.Math.round;

/**
 * Class Agent performs Z increasing by changing agent's relation
 */

public class Agent implements Cloneable {
    public TreeMap<String, Point> relation = new TreeMap<String, Point>();

    //HashMap<String, RoaringBitmap> recordsByField = new HashMap<>(); // records common for all points from relation
    RoaringBitmap records = null;//new RoaringBitmap(); // records common for all points from relation
    String signature = "";
    private double z = NaN, p = NaN, cp = NaN;
    private InvertedTabularModel model;
    private Object predictingValue = null;
    private boolean hasPredictingField = false;
    private HashMap<String, Double> condPcashe = new HashMap<>();
    private BitSet fields4view = new BitSet();
    private BitSet fields = new BitSet();
    private RoaringBitmap values = new RoaringBitmap();



    /**
     * Creates Agent for starting point
     * @param startPoint - {integer field index, object representing field value}
     */

    public Agent(Point startPoint, InvertedTabularModel model) {

        this.model = model;

        if (startPoint != null) {
            addPoint(startPoint);
        }
    }


    public Agent (Point points[], InvertedTabularModel model) {
        this.model = model;

        if (points.length > 0) {
            addPoint(points);
        }

    }

    public Agent (Collection<Point> points, InvertedTabularModel model) {
        this.model = model;

        if (points.size() > 0) {
            addPoint(points);
        }

    }

    public boolean hasPerdictingField() {
        return hasPredictingField;
    }

    public void setPerdictingField(boolean hasPredictingField) {
        this.hasPredictingField = hasPredictingField;
    }

    protected void resign(){
        signature = relation.keySet().toString();
        z = NaN; p = NaN; cp = NaN;
    }

    public Object getRelationValue(String field){
        for (Point p: relation.values())
            if (p.getField().equals(field)) return p.toString();

        return null;
    }



    public RoaringBitmap getRecords() {
        return records;
    }

    public Object getPerdictingValue() {
        return predictingValue;
    }

    public RoaringBitmap getValues() {
        return values;
    }

    public void setValues(int index) {
        values.add(index);
    }

    public void setPerdictingValue(Object predictingValue) {
        this.predictingValue = predictingValue;
    }

    public String toString(){
        return signature+"\t"+relation.size()+"; "+getMR();
    }

    /**
     * Gets confidential probability of the agents subspace that equal production of all confidential intervals probabilities included in agent
     * @return - confidential probability of the agent
     */

    public double getConfP(){
/*        double p = 1, f = 0 ;

        if(!Double.isNaN(cp)) return cp;

        for (Map.Entry<String, RoaringBitmap> e: recordsByField.entrySet()) {
            RoaringBitmap b = e.getValue();
            if (relationByField.get(e.getKey()).size() > 1) //& invertedIndex.get(e.getKey()).size()*epsilon > 1)
                p = p * (1+((double) b.getCardinality() / model.getDataSet().size()));
        }

        return cp = 1 - (p == 0?0: p - 1);*/
        return 0;
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

        for (Map.Entry<String, Point> e: relation.entrySet())
            if (!field.equals(e.getValue().getField())) {
                RoaringBitmap fv =  ((BitInvertedIndex)model.getInvertedIndex()).getValueIndex(e.getValue().getField(), e.getValue().getValue());//(RoaringBitmap) model.getInvertedIndex().getMap(e.getValue().getField()).get(e.getValue().getValue());
                if (fv != null)
                    rc.and(fv);
            }

        r = (double) records.getCardinality() / rc.getCardinality();

        condPcashe.put(field, r);

        return r;
    }


    /**
     * Adds new point to agent
     * @param point  - new point
     */

    public void addPoint(Point point){
        relation.put(point.toString(), point);
        fields4view.set(model.getInvertedIndex().getFieldIndex(point.getField()), true);
        fields.set(model.getInvertedIndex().getFieldIndex(point.getField()), true);
        records = new RoaringBitmap();

        RoaringBitmap rb;
        if ((rb = ((BitInvertedIndex)model.getInvertedIndex()).getValueIndex(point.getField(), point.getValue())) != null) {
            if (relation.size() == 1) {
                records.or(rb);
            } else
                records.and(rb);
            resign();
        }
    }

    /**
     * Adds new points to agent
     * @param points  - array of new point
     */

    public void addPoint(Point[] points){

        LinkedList<RoaringBitmap> roaringBitmaps = new LinkedList<>();

        for (Point point: points) {
            relation.put(point.toString(), point);
            fields4view.set(model.getInvertedIndex().getFieldIndex(point.getField()), true);
            fields.set(model.getInvertedIndex().getFieldIndex(point.getField()), true);

            RoaringBitmap rb =  ((BitInvertedIndex)model.getInvertedIndex()).getValueIndex(point.getField(), point.getValue());
            if (rb != null) {
                roaringBitmaps.add(rb);
            }/* else
                    System.err.println(point + " do not exist in model");*/

        }

        records = RoaringBitmap.and(roaringBitmaps.listIterator(), 0, round(model.getDataSet().size()));
        resign();
    }


    /**
     * Adds new points to agent
     * @param points  - array of new point
     */

    public void addPoint(Collection<Point> points){

        LinkedList<RoaringBitmap> roaringBitmaps = new LinkedList<>();

        for (Point point: points) {
            relation.put(point.toString(), point);
            fields4view.set(model.getInvertedIndex().getFieldIndex(point.getField()), true);
            fields.set(model.getInvertedIndex().getFieldIndex(point.getField()), true);

            RoaringBitmap rb =  ((BitInvertedIndex)model.getInvertedIndex()).getValueIndex(point.getField(), point.getValue());
            if (rb != null) {
                roaringBitmaps.add(rb);
            }/* else
                    System.err.println(point + " do not exist in model");*/

        }

        records = RoaringBitmap.and(roaringBitmaps.listIterator(), 0, round(model.getDataSet().size()));
        resign();
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
       // if (recordsByField.size() < 2) return 0;

        double z = records.getCardinality(), f = 1;
        int c = 1, l = 0;
        for (Point point: relation.values()) {
            RoaringBitmap fieldrecords = ((BitInvertedIndex)model.getInvertedIndex()).getValueIndex(point.getField(), point.getValue());
            if (fieldrecords != null) {
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE / 1000000) { //prevents double value overloading
                    f = f / model.getDataSet().size();
                    c++;
                }
            }
        }

        z = log(z / f) + (l - c) * log(model.getDataSet().size());

        return z;
    }

    public BitSet getFields4view() {
        return fields4view;
    }


    public BitSet getFields() {
        return fields;
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
        for (Point point: relation.values()) {
            RoaringBitmap fieldrecords = ((BitInvertedIndex)model.getInvertedIndex()).getValueIndex(point.getField(), point.getValue());
            if (!fieldrecords.isEmpty()) {
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE / 1000000) { //prevents double value overloading
                    f = f / model.getDataSet().size();
                    c++;
                }
            }
        }

        double ps = log(f) - (l - c) * log(model.getDataSet().size());
        return ps >= 0;
    }

    public static Agent merge(Agent a1, Agent a2, InvertedTabularModel model) {
        if (a1 == null & a2 == null) throw new IllegalArgumentException("Can't merge two null agents");

        if (a1 == null) return a2;
        if (a2 == null) return a1;

        Agent r = new Agent((Point) null, model);

        for (Point p: a1.relation.values()) {
            r.relation.put(p.toString(), p);
        }

        for (Point p: a2.relation.values()) {
            r.relation.put(p.toString(), p);
        }

        r.fields.or(a1.fields);
        r.fields.or(a2.fields);
        r.fields4view.or(a1.fields4view);
        r.fields4view.or(a2.fields4view);
        r.getValues().or(a1.getValues());
        r.getValues().or(a2.getValues());

        r.resign();
        r.predictingValue = a1.predictingValue != null ? a1.predictingValue: a2.predictingValue != null ? a2.predictingValue: null;

        r.records = new RoaringBitmap();
        if (a1.records != null) r.records.or(a1.records);
        if (a2.records != null) r.records.and(a2.records);

        return r;
    }

}