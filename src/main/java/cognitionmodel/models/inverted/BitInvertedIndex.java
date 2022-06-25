package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class BitInvertedIndex implements InvertedIndex{

    private InvertedTabularModel model;
    private TableDataSet dataSet;
    private HashMap<String, TreeMap<Object, RoaringBitmap>> invertedIndex = new HashMap();
    private HashMap<String, TreeMap<Object, RoaringBitmap>> indexWithIntervals = new HashMap();
    private TreeMap<String, Integer> fields = new TreeMap<>();
    private ArrayList<String> fieldsList;
    private double[] confidenceLevels = null;
    private int[] di2i, i2di;

    public BitInvertedIndex(InvertedTabularModel model){
        this.dataSet = model.getDataSet();
        this.model = model;
        init();
    }

    protected void init() {

        di2i = new int[dataSet.getHeader().size()];
        i2di = new int[dataSet.getHeader().size()];
        for (int i = 0; i < dataSet.getHeader().size(); i++)
            if (model.getEnabledFields()[i] == 1) {
                invertedIndex.put(dataSet.getHeader().get(i).getValue().toString(), new TreeMap<Object, RoaringBitmap>());
                di2i[i] = fields.size(); i2di[fields.size()] = i;
                fields.put(dataSet.getHeader().get(i).getValue().toString(), fields.size());
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

                    if (tupleElement.getType() == TupleElement.Type.Int) val = (int)tupleElement.getValue() * 1.0;
                        else val = tupleElement.getValue();
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

        fieldsList = new ArrayList<>(); fieldsList.addAll(fields.keySet().stream().collect(Collectors.toList()));
    }

    public RoaringBitmap getValueIndex(String field, Object value){
        if (value.getClass() == Integer.class) value = (int)value * 1.0;
        return (RoaringBitmap) getMap(field).get(value);
    }

    public int dataSetFieldIndexToInvertedFieldIndex(int index){
        return di2i[index];
    }

    public int invertedIndexToDatasetFieldIndex(int index){
        return i2di[index];
    }

    /**
     * Gets number of the field in index. The index do not includes not enabled fields. Order of fields corresponds the order in data set excluding not enabled fields.
     * @param field - field in index
     * @return
     */

    @Override
    public Integer getFieldIndex(String field){
        return (fields.containsKey(field)?fields.get(field):-1);
    }

    @Override
    public List<String> getFields() {
        return fieldsList;
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

    @Override
    public TreeMap getMap(String field) {
        if (!invertedIndex.containsKey(field))
            throw new IllegalArgumentException("Field " + field + " do not contained in index") ;
        return invertedIndex.get(field);
    }

    @Override
    public int getFieldsAmount() {
        return invertedIndex.entrySet().size();
    }

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
    public void setConfidenceIntervals(double[] confidenceLevels) {
        this.confidenceLevels = confidenceLevels;
    }

    public RoaringBitmap getRecords(String field, Object value){
        if (confidenceLevels == null) return getIndexedRecords(field, value);
        if (Double.isNaN(confidenceLevels[getFieldIndex(field)])) return getIndexedRecords(field, value);



        RoaringBitmap r = new RoaringBitmap(), ra = new RoaringBitmap(), rb = new RoaringBitmap();

        TreeMap<Object, RoaringBitmap> values = invertedIndex.get(field);

        Map.Entry<Object, RoaringBitmap> a; // = values.ceilingEntry(value);
        Map.Entry<Object, RoaringBitmap> b;// = values.floorEntry(value);

        try {
            a = values.ceilingEntry(value);
            b = values.floorEntry(value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(value + " "+ value.getClass()+ " do not fit to type of data set field \""+field+"\"");
        }

        if (values.containsKey(value)){
            r = values.get(value);
            a = values.lowerEntry(a.getKey()); ra.or(r);
            b = values.higherEntry(b.getKey()); rb.or(r);
        }

        double thr = dataSet.size() * (1 - confidenceLevels[getFieldIndex(field)]);
        long ro = r.getCardinality();

        do {
            ro = r.getCardinality();
            if (a != null) {
                ra.or(a.getValue());
            }
            if (b != null) {
                rb.or(b.getValue());
            }
            if (abs(ra.getCardinality() - thr) < abs(rb.getCardinality() - thr))
            {
                if (a != null & abs(r.getCardinality() - thr) > abs(ra.getCardinality() - thr)) {
                    a = values.higherEntry(a.getKey());
                    r.or(ra);
                    rb.or(ra);
                } //else a = (ra.getCardinality() > thr? null: a);
            } else {
                if (b!=null & abs(r.getCardinality() - thr) > abs(rb.getCardinality() - thr)) {
                    r.or(rb);
                    ra.or(rb);
                    b = values.lowerEntry(b.getKey());
                } //else b = (rb.getCardinality() > thr? null: b);
            }

        } while (!(r.getCardinality() > thr) & r.getCardinality() != ro);

        confidenceLevels[getFieldIndex(field)] = 1 - (double)r.getCardinality() / dataSet.size();

        return r;
    }

    private RoaringBitmap getIndexedRecords(String field, Object value){
        return (RoaringBitmap) model.getInvertedIndex().getMap(field).get(value);
    }


}
