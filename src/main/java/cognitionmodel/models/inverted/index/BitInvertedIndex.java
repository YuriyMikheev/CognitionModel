package cognitionmodel.models.inverted.index;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.log;

public class BitInvertedIndex implements InvertedIndex{

    protected InvertedTabularModel model;
    protected TableDataSet dataSet;
    protected HashMap<String, TreeMap<Object, RoaringBitmap>> invertedIndex = new HashMap();
    protected TreeMap<String, Integer> fields = new TreeMap<>();
    protected ArrayList<String> fieldsList;
    protected double[] confidenceLevels = null;
    protected int[] di2i, i2di;

    protected BitInvertedIndex(){

    }

    public BitInvertedIndex(InvertedTabularModel model){
        this.dataSet = model.getDataSet();
        this.model = model;

        fieldsList = new ArrayList<>(); //fieldsList.addAll(fields.keySet().stream().collect(Collectors.toList()));

        di2i = new int[dataSet.getHeader().size()];
        i2di = new int[dataSet.getHeader().size()];
        for (int i = 0; i < dataSet.getHeader().size(); i++)
            if (model.getEnabledFields()[i] == 1) {
                invertedIndex.put(dataSet.getHeader().get(i).getValue().toString(), new TreeMap<Object, RoaringBitmap>());
                di2i[i] = fields.size(); i2di[fields.size()] = i;
                fields.put(dataSet.getHeader().get(i).getValue().toString(), fields.size());
                fieldsList.add(dataSet.getHeader().get(i).getValue().toString());
            }

        i2di = Arrays.copyOf(i2di, fields.size());
        int i = 0;
        for (Tuple tuple: dataSet) {
            int j = 0;
            for (TupleElement tupleElement: tuple){
                if (model.getEnabledFields()[j] == 1) {
                    String fieldName = dataSet.getHeader().get(j).getValue().toString();
                    RoaringBitmap idx;
                    Object val;

                    //if (tupleElement.getType() == TupleElement.Type.Int) val = (int)tupleElement.getValue() * 1.0;
                        //else
                    val = tupleElement.getValue();
                    if (tupleElement.getType() != TupleElement.Type.Empty)
                        try {
                            if (invertedIndex.get(fieldName).containsKey(val))
                                idx = invertedIndex.get(fieldName).get(val);
                            else {
                                idx = RoaringBitmap.bitmapOf(i);
                                invertedIndex.get(fieldName).put(val, idx);
                            }
                            idx.add(i);
                        } catch (ClassCastException e){
                            System.err.println("line "+i+" field "+fieldName+" has inconsistent data "+tupleElement+" "+e.getMessage());
                        }
                }
                j++;
            }
            i++;
        }

    }



    /**
     * Returns field index in Data Set object corresponded with filed index in Inverted Index object
     * @param index
     * @return
     */

    public int dataSetFieldIndexToInvertedFieldIndex(int index){
        return di2i[index];
    }

    /**
     * Returns field index in Inverted Index object corresponded with filed index in Data Set object
     * @param index
     * @return
     */

    public int invertedIndexToDatasetFieldIndex(int index){
        if (index == -1) return -1;
        return i2di[index];
    }

    /**
     * Gets number of the field in index. The index does not include not enabled fields. Order of fields corresponds the order in data set excluding not enabled fields.
     * @param field - field in index
     * @return
     */

    @Override
    public Integer getFieldIndex(String field){
        return (fields.containsKey(field)?fields.get(field):-1);
    }


    /**
     * Gets the list of inverted index fields. Inverted index have all indexed fields and does not include fields that not enabled in data set
     *
     * @return
     */

    @Override
    public TreeMap<String, Integer> getFields() {
        return fields;
    }

    /**
     * Gets all values of the field from index
     * @param field - field in index
     * @return - list object contains all possible values
     */

    @Override
    public List<Object> getAllValues(String field) {
        if (!invertedIndex.containsKey(field))
            throw new IllegalArgumentException("Field" + field + " do not contained in index") ;
        return invertedIndex.get(field).keySet().stream().collect(Collectors.toList());
    }

    /**
     * Returns Tree map object that contains all values and indexes of the field
     * @param field - field in index
     * @return
     */

    @Override
    public TreeMap getMap(String field) {
        if (!invertedIndex.containsKey(field))
            throw new IllegalArgumentException("Field " + field + " is not contained in index") ;
        return invertedIndex.get(field);
    }

    @Override
    public int getFieldsAmount() {
        return invertedIndex.entrySet().size();
    }

    /**
     * Sets level of confidence for all fields
     * @param confidenceLevel - level of confidence (90%, 95%, 99% or any other) for fields in data set order. if equal NaN it throw off the interval
     */
    @Override
    public void setConfidenceIntervals(double confidenceLevel) {
        if (Double.isNaN(confidenceLevel)) confidenceLevels = null;
            else {
                    confidenceLevels = new double[fields.size()];
                    for (int i = 0; i < confidenceLevels.length; i++)
                        confidenceLevels[i] = confidenceLevel;
                }
    }

    @Override
    public double[] getConfidenceIntervals() {
        return confidenceLevels;
    }

    /**
     * Sets levels of confidence for fields according to their number
     * @param confidenceLevels - levels of confidence (90%, 95%, 99% or any other) for fields in data set order. if equal NaN it throw off the interval
     */

    @Override
    public void setConfidenceIntervals(double[] confidenceLevels) {
        this.confidenceLevels = confidenceLevels;
    }


    /**
     * Gets set of records from index. if field level of confidence is not NaN calculates record set according it.
     * @param field - enabled field from data set
     * @param value - value of the field
     * @return
     */
    @Override
    public RoaringBitmap getRecords(String field, Object value){
       // if (value.getClass().getSuperclass() == Number.class) value = Double.parseDouble(value.toString());
        try {
            return (RoaringBitmap) getMap(field).get(value);
        } catch (ClassCastException e){
            return null;
        }
    }


    private RoaringBitmap[] getBitmapArray(Collection<Point> points){
        RoaringBitmap[] r = new RoaringBitmap[points.size()];
        int i = 0;
        for (Point point : points) {
            RoaringBitmap rb = getRecords(point.field, point.value);
            if (rb != null)
                r[i++] = rb;
        }
        if (i < points.size())
            r = Arrays.copyOf(r,i);

        return r;
    }

    private List<RoaringBitmap> getBitmapList(Collection<Point> points){
        LinkedList<RoaringBitmap> r = new LinkedList<>();
        for (Point point : points) {
            RoaringBitmap rb = getRecords(point.field, point.value);
            if (rb != null)
                r.add(rb);
        }
        return r;
    }


    @Override
    public double getDataSetSize() {
        return dataSet.size();
    }

    @Override
    public double getP(Agent agent) {
        return getFr(agent)/getDataSetSize();
    }

    @Override
    public double getFr(Agent agent) {
        if (agent.getPoints().isEmpty()) return -1;

        RoaringBitmap rb = (RoaringBitmap) getCash(agent);//agent.getCachedRecords();
        if (rb == null) {
            rb = RoaringBitmap.and(getBitmapList(agent.getPoints()).listIterator(), 0L, (long)Integer.MAX_VALUE*2-1);
            putCash(agent, rb);
            //cash.put(agent.getSignature(), rb);
        }
        return rb.getCardinality();
    }

    @Override
    public double getProductP(Agent agent) {
        int c = 1, l = 0;
        double f = 1;
        for (Point point: agent.getRelation().values()) {
            RoaringBitmap fieldrecords = getRecords(point.getField(), point.getValue());
            if (fieldrecords != null) {
                f = f * fieldrecords.getCardinality();
/*                l++;
                if (f > Double.MAX_VALUE / 1000000) { //prevents double value overloading
                    f = f / getDataSetSize();
                    c++;
                }*/
            }
        }
        return f;
    }

    @Override
    public double getMR(Agent agent){
        if (agent.getRelation().size() <= 1) return 0;

        double z = getFr(agent), f = 1;// records.getCardinality();
        int c = 1, l = 0;
        for (Point point: agent.getRelation().values()) {
            RoaringBitmap fieldrecords = getRecords(point.getField(), point.getValue());
            if (fieldrecords != null) {
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE / 1000000) { //prevents double value overflowing
                    f = f / getDataSetSize();
                    c++;
                }
            }
        }
        z = log(z / f) + (l - c) * log(getDataSetSize());
        agent.setMR(z);

        return z;
    }

    @Override
    public void mergeAgents(Agent agent, Agent a1, Agent a2) {
        if (agent.getCachedRecords() != null) return;

        RoaringBitmap r1, r2;
        r1 = (RoaringBitmap) getCash(a1);//cash.get(a1.getSignature());
        if (r1 == null)
            if (a1.getPoints().size() > 1)
                putCash(a1, r1 = RoaringBitmap.and(getBitmapList(a1.getPoints()).listIterator(), 0L, (long)Integer.MAX_VALUE*2-1));//FastAggregation.and(getBitmapArray(a1.getPoints())));
            else
                putCash(a1, r1 = getRecords(a1.relation.firstEntry().getValue().getField(), a1.relation.firstEntry().getValue().getValue()));

        r2 = (RoaringBitmap) getCash(a2);//cash.get(a2.getSignature());
        if (r2 == null)
            if (a2.getPoints().size() > 1)
                putCash(a2, r2 = RoaringBitmap.and(getBitmapList(a2.getPoints()).listIterator(), 0L, (long)Integer.MAX_VALUE*2-1));
            else
                putCash(a2, r2 = getRecords(a2.relation.firstEntry().getValue().getField(), a2.relation.firstEntry().getValue().getValue()));

        if (r1 == null || r2 == null) return;
        putCash(agent, RoaringBitmap.and(r1, r2));

    }

    @Override
    public void putCash(Agent agent, Object map) {
        agent.setCachedRecords(map);
    }

    @Override
    public Object getCash(Agent agent){
        return agent.getCachedRecords();
    }

    @Override
    public void clearCash(Agent agent) {
        agent.setCachedRecords(null);
    }

    @Override
    public void clearCash(List<Agent> agents) {
        agents.forEach(agent -> agent.setCachedRecords(null));
    }


    public InvertedTabularModel getModel() {
        return model;
    }

    public TableDataSet getDataSet() {
        return dataSet;
    }

    public HashMap<String, TreeMap<Object, RoaringBitmap>> getInvertedIndex() {
        return invertedIndex;
    }

    public ArrayList<String> getFieldsList() {
        return fieldsList;
    }

    public double[] getConfidenceLevels() {
        return confidenceLevels;
    }

    public int[] getDi2i() {
        return di2i;
    }

    public int[] getI2di() {
        return i2di;
    }


}
