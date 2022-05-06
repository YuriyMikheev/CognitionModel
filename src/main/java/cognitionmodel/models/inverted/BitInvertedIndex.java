package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.stream.Collectors;

public class BitInvertedIndex implements InvertedIndex{

    InvertedTabularModel model;
    TableDataSet dataSet;
    private HashMap<String, TreeMap<Object, RoaringBitmap>> invertedIndex = new HashMap();

    public BitInvertedIndex(InvertedTabularModel model){
        this.dataSet = model.getDataSet();
        this.model = model;
        init();
    }

    protected void init() {

        for (int i = 0; i < dataSet.getHeader().size(); i++)
            if (model.getEnabledFields()[i] == 1)
                invertedIndex.put(dataSet.getHeader().get(i).getValue().toString(), new TreeMap<Object, RoaringBitmap>());

        int i = 0;
        for (Tuple tuple: dataSet) {
            int j = 0;
            for (TupleElement tupleElement: tuple){
                if (model.getEnabledFields()[j] == 1) {
                    String fieldName = dataSet.getHeader().get(j).getValue().toString();
                    RoaringBitmap idx;
                    if (invertedIndex.get(fieldName).containsKey(tupleElement.getValue()))
                        idx = invertedIndex.get(fieldName).get(tupleElement.getValue());
                    else {
                        idx = RoaringBitmap.bitmapOf(i);
                        invertedIndex.get(fieldName).put(tupleElement.getValue(), idx);
                    }
                    idx.add(i);
                }
                j++;
            }
            i++;
        }
    }

    @Override
    public List<String> getFields() {
        return invertedIndex.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public List<Object> getAllValues(String field) {
        if (!invertedIndex.containsKey(field))
            throw new IllegalArgumentException("Field" + field + " do not contained in index") ;
        return invertedIndex.get(field).keySet().stream().collect(Collectors.toList());
    }

    @Override
    public TreeMap getMap(String field) {
        if (!invertedIndex.containsKey(field))
            throw new IllegalArgumentException("Field" + field + " do not contained in index") ;
        return invertedIndex.get(field);
    }

    @Override
    public Set<Map.Entry<String, TreeMap<Object, RoaringBitmap>>> getEntrySet() {
        return invertedIndex.entrySet();
    }
}
