package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

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
        init();
    }

    private void init() {
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


    /**
     * Gets the list of inverted index fields. Inverted index have all indexed fields and does not include fields that not enabled in data set
     * @return
     */

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

    /**
     * Returns Tree map object that contains all values and indexes of the field
     * @param field - field in index
     * @return
     */

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
    public RoaringBitmap getRecords(String field, Object value){
        if (value.getClass().getSuperclass() == Number.class) value = Double.parseDouble(value.toString());
        try {
            return (RoaringBitmap) getMap(field).get(value);
        } catch (ClassCastException e){
            return null;
        }
    }

    @Override
    public double getDataSetSize() {
        return dataSet.size();
    }

}
