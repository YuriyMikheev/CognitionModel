package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.relations.LightRelation;

import java.util.*;

public class InvertedTabularModel extends TabularModel {

    private TreeMap<Object, HashSet<Integer>>[] invertedIndex;// = new HashMap<>();
    private TableDataSet dataSet;

    /**
     * Creates TabularModel object with inverted indexes of the dataset
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
        super.setDataSet(dataSet);

        this.dataSet = (TableDataSet) dataSet;
        invertedIndex = new TreeMap[this.dataSet.getHeader().size()];

        for (int i = 0; i < this.dataSet.getHeader().size(); i++) {
            invertedIndex[i] = new TreeMap<Object, HashSet<Integer>>();
        }

        int i = 0;
        for (Tuple tuple: dataSet) {
            int j = 0;
            for (TupleElement tupleElement: tuple){
                HashSet<Integer> integers;
                if (invertedIndex[j].containsKey(tupleElement.getValue()))
                    integers = invertedIndex[j].get(tupleElement.getValue());
                else {
                    integers = new HashSet<>();
                    invertedIndex[j].put(tupleElement.getValue(), integers);
                }
                integers.add(i);
                j++;
            }
            i++;
        }
    }

    private class Agent{
        public double Z = 0;
        ArrayList<Object[]> spot = new ArrayList<>();
        BitSet records = new BitSet();

        public Agent(Object[] startPoint){
            addPoint(startPoint);
        }

        private void addPoint(Object[] point){
            spot.add(point);

        }

    }


}
