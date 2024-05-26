package cognitionmodel.models.inverted;

import cognitionmodel.models.inverted.index.InvertedIndex;
import cognitionmodel.models.inverted.index.Point;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.log;
import static java.lang.Math.round;

/**
 * Class Agent performs Z increasing by changing agent's relation
 */

public class Agent  {
    public TreeMap<String, Point> relation = new TreeMap<String, Point>();


    String signature = "";
    private double z = NaN, p = NaN, cp = NaN, prodP= NaN;
    private long fr = -1;
    private InvertedIndex invertedIndex;
    private Object predictingValue = null;
    private boolean hasPredictingField = false;
    private HashMap<String, Double> condPcashe = new HashMap<>();
    private BitSet fields4view = new BitSet();
    private BitSet fields = new BitSet();
    private RoaringBitmap values = new RoaringBitmap();

    private Object cachedRecords = null;



    /**
     * Creates Agent for starting point
     * @param startPoint - {integer field index, object representing field value}
     */

    public Agent(Point startPoint, InvertedIndex invertedIndex) {

        this.invertedIndex = invertedIndex;

        if (startPoint != null) {
            addPoint(startPoint);
        }
    }


    public Agent (Point points[], InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;

        if (points.length > 0) {
            addPoint(points);
        }

    }

    public Agent (Collection<Point> points, InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;

        if (points.size() > 0) {
            addPoint(points);
        }

    }

    public boolean hasPredictingField() {
        return hasPredictingField;
    }

    public void setPredictingField(boolean hasPredictingField) {
        this.hasPredictingField = hasPredictingField;
    }

    protected void resign(){
        signature = relation.keySet().toString();
        z = NaN; p = NaN; cp = NaN; prodP = NaN;
    }

    public Object getRelationValue(String field){
        for (Point p: relation.values())
            if (p.getField().equals(field)) return p.toString();

        return null;
    }



/*    public RoaringBitmap getRecords() {
        return records;
    }*/

    public Object getPredictingValue() {
        return predictingValue;
    }

    public RoaringBitmap getValues() {
        return values;
    }

    public void setValues(int index) {
        values.add(index);
    }

    public void setPredictingValue(Object predictingValue) {
        this.predictingValue = predictingValue;
        hasPredictingField = true;
    }

    public String toString(){
        return signature+"\t"+relation.size()+"; "+getMR();
    }

    /**
     * Gets confidential probability of the agents subspace that equal production of all confidential intervals probabilities included in agent
     * @return - confidential probability of the agent
     */

    public double getConfP(){
        double p = 1;
        if (invertedIndex.getConfidenceIntervals() == null) return 1.0;

        if(!Double.isNaN(cp)) return cp;

        for (Map.Entry<String, Point> e: relation.entrySet()) {
            int pi = invertedIndex.getFieldIndex(e.getValue().getField());
            p = p * invertedIndex.getConfidenceIntervals()[pi];
        }

        return cp = p;
    }

    /**
     * Gets probability of the relation
     * @return
     */
    public double getP(){
/*        if (records == null) return p;
        return (Double.isNaN(p)?(p=(double)records.getCardinality()/invertedIndex.getDataSetSize()): p);*/
        return (Double.isNaN(p)? p = invertedIndex.getP(this) : p);
    }

    /**
     * Gets probability of the relation as if relations are independent
     * That is production of the all relations probabilities
     * @return
     */
    public double getProductP(){
/*        if (records == null) return p;
        return (Double.isNaN(p)?(p=(double)records.getCardinality()/invertedIndex.getDataSetSize()): p);*/
        return (Double.isNaN(prodP)? p = invertedIndex.getProductP(this) : prodP);
    }


    /**
     * Gets frequency of the relation
     * @return
     */
    public double getFr(){
/*        if (records == null) return fr;
        return fr = records.getCardinality();*/
        return (fr < 0 ? fr = round(invertedIndex.getFr(this)) : fr);
    }


    /**
     * Gets conditional probability of field value if agent relation was appeared
     * @param field
     * @return
     */

    public double getCondP(String field){
/*
        Double r = condPcashe.get(field);
        if (r != null) return r;

        getP();

        RoaringBitmap rc  = new RoaringBitmap();

        rc.add(0, round(invertedIndex.getDataSetSize()));

        for (Map.Entry<String, Point> e: relation.entrySet())
            if (!field.equals(e.getValue().getField())) {
                RoaringBitmap fv =  invertedIndex.getRecords(e.getValue().getField(), e.getValue().getValue());//(RoaringBitmap) model.getInvertedIndex().getMap(e.getValue().getField()).get(e.getValue().getValue());
                if (fv != null)
                    rc.and(fv);
            }

        r = (double) records.getCardinality() / rc.getCardinality();

        condPcashe.put(field, r);*/

        return NaN;
    }


    /**
     * Adds new point to agent
     * @param point  - new point
     */

    public void addPoint(Point point){
        relation.put(point.toString(), point);
        fields4view.set(invertedIndex.getFieldIndex(point.getField()), true);
        fields.set(invertedIndex.getFieldIndex(point.getField()), true);
        resign();

/*        records = new RoaringBitmap();

        RoaringBitmap rb;
        if ((rb = invertedIndex.getRecords(point.getField(), point.getValue())) != null) {
            if (relation.size() == 1) {
                records.or(rb);
            } else
                records.and(rb);
        }*/
    }

    /**
     * Adds new points to agent
     * @param points  - array of new point
     */

    public void addPoint(Point[] points){

     //   LinkedList<RoaringBitmap> roaringBitmaps = new LinkedList<>();

        for (Point point: points) {
            relation.put(point.toString(), point);
            fields4view.set(invertedIndex.getFieldIndex(point.getField()), true);
            fields.set(invertedIndex.getFieldIndex(point.getField()), true);

/*            RoaringBitmap rb =  invertedIndex.getRecords(point.getField(), point.getValue());
            if (rb != null) {
                roaringBitmaps.add(rb);
            }*//* else
                    System.err.println(point + " do not exist in model");*/

        }

      //  records = RoaringBitmap.and(roaringBitmaps.listIterator(), 0, round(invertedIndex.getDataSetSize()));
        resign();
    }


    /**
     * Adds new points to agent
     * @param points  - array of new point
     */

    public void addPoint(Collection<Point> points){

    //    LinkedList<RoaringBitmap> roaringBitmaps = new LinkedList<>();

        for (Point point: points) {
            relation.put(point.toString(), point);
            fields4view.set(invertedIndex.getFieldIndex(point.getField()), true);
            fields.set(invertedIndex.getFieldIndex(point.getField()), true);
/*
            RoaringBitmap rb =  invertedIndex.getRecords(point.getField(), point.getValue());
            if (rb != null) {
                roaringBitmaps.add(rb);
            }*//* else
                    System.err.println(point + " do not exist in model");*/

        }

     //   records = RoaringBitmap.and(roaringBitmaps.listIterator(), 0, round(invertedIndex.getDataSetSize()));
        resign();
    }

    public String getSignature() {
        return signature;
    }

    public void setMR(double mr) {
        this.z = mr;
    }

    /**
     * Calculates regularity measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     * @return - regularity measure for the relation
     */


    public double getMR(){
        if (!Double.isNaN(z)) return z;
        if (relation.size() < 1) return 0;

        z = invertedIndex.getMR(this);

/*       // if (recordsByField.size() < 2) return 0;

        z = getFr();// records.getCardinality();
        double f = getProductP();
    //    return z == 0 ? -relation.values().size()*log(invertedIndex.getDataSetSize()) :log(z);
   //     int c = 1, l = 0;
*//*        for (Point point: relation.values()) {
            RoaringBitmap fieldrecords = invertedIndex.getRecords(point.getField(), point.getValue());
            if (fieldrecords != null) {
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE / 1000000) { //prevents double value overloading
                    f = f / invertedIndex.getDataSetSize();
                    c++;
                }
            }
        }*//*


        z = log(z / f) + (relation.size() - 1) * log(invertedIndex.getDataSetSize());// + (l - c) * log(invertedIndex.getDataSetSize());*/

        return z;
    }

    public TreeMap<String, Point> getRelation() {
        return relation;
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

/*    public boolean isPossible(){
        if (relation.size() < 1) return false;
        if (records.isEmpty()) return false;

        double f = 1;
        int c = 1, l = 0;
        for (Point point: relation.values()) {
            RoaringBitmap fieldrecords = invertedIndex.getRecords(point.getField(), point.getValue());
            if (!fieldrecords.isEmpty()) {
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE / 1000000) { //prevents double value overloading
                    f = f / invertedIndex.getDataSetSize();
                    c++;
                }
            }
        }

        double ps = log(f) - (l - c) * log(invertedIndex.getDataSetSize());
        return ps >= 0;
    }*/

    public InvertedIndex getInvertedIndex() {
        return invertedIndex;
    }

    public void freeze(){
        getP();
        getMR();
       // records = null;
    }

    public static Agent merge(Agent a1, Agent a2, InvertedIndex invertedIndex) {
        if (a1 == null & a2 == null) throw new IllegalArgumentException("Can't merge two null agents");

        if (a1 == null) return a2;
        if (a2 == null) return a1;

        Agent r = new Agent((Point) null, invertedIndex);

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
        r.hasPredictingField = r.predictingValue != null;
        r.prodP = a1.prodP * a2.prodP;

/*        r.records = new RoaringBitmap();


        if (a1.records != null) {
            r.records.or(a1.records);
            if (a2.records != null)
                r.records.and(a2.records);
        } else
        if (a2.records != null)
            r.records.or(a2.records);*/
        invertedIndex.mergeAgents(r, a1, a2);

        return r;
    }

    public Collection<Point> getPoints(){
        return relation.values();
    }

    public Object getCachedRecords() {
        return cachedRecords;
    }

    public void setCachedRecords(Object cachedRecords) {
        this.cachedRecords = cachedRecords;
    }
}