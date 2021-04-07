package cognitionmodel.models;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.patterns.PatternSet;

import java.util.Map;

/**
 * Abstract class represents model of data set. Consists map of relations produced from data set (D) and pattern set (P).
 * Model = D*P
 */

public abstract class Model {
    private DataSet dataSet;
    private Map relationsMap;
    private PatternSet patternSet;

    /**
     * Creates model object
     * @param dataSet - data for model
     * @param patternSet - set of patterns
     */

    public Model(DataSet dataSet, PatternSet patternSet) {
        this.dataSet = dataSet;
        this.patternSet = patternSet;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public Map getRelationsMap() {
        return relationsMap;
    }

    public PatternSet getPatternSet() {
        return patternSet;
    }



}
