package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;

import java.util.HashMap;
import java.util.HashSet;

public class InvertedTabularModel extends TabularModel {

    private HashMap<String, HashSet<Integer>> invertedIndex = new HashMap<>();

    /**
     * Creates TabularModel object
     *
     * @param dataSet    - data for model
     */
    public InvertedTabularModel(TableDataSet dataSet) {
        super(dataSet);
    }

    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     * @param relationInstance - the instance of relation for this model
     */
    public InvertedTabularModel(TableDataSet dataSet, LightRelation relationInstance, String... enabledFieldsNames){
        super(dataSet, relationInstance, enabledFieldsNames);
    }



    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     */
    public InvertedTabularModel(TableDataSet dataSet, String... enabledFieldsNames) {
        this(dataSet, new LightRelation(), enabledFieldsNames);
    }

    private HashSet<Integer> addToSet(Object set, Integer value){
        ((HashSet<Integer>)set).add(value);
        return ((HashSet<Integer>)set);
    }

    @Override
    protected void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;

        int i = 0;
        for (Tuple tuple: dataSet) {
            for (TupleElement tupleElement: tuple){
                HashSet<Integer> integers;
                if (invertedIndex.containsKey(tupleElement.getValue().toString()))
                    integers = invertedIndex.get(tupleElement.getValue().toString());
                else {
                    integers = new HashSet<>();
                    invertedIndex.put(tupleElement.getValue().toString(), integers);
                }
                integers.add(i);
            }
            i++;
        }
    }




}
