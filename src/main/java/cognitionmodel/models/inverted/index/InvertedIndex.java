package cognitionmodel.models.inverted.index;


import cognitionmodel.models.inverted.Agent;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;

public interface InvertedIndex {

    /**
     * Get all available fields in index
     *
     * @return - List of fields
     */

    public TreeMap<String, Integer> getFields();

    /**
     * Gets index of the field in index
     * @param field
     * @return
     */

    public Integer getFieldIndex(String field);


    /**
     * Get all values of the field
     * @param field - field in index
     * @return - list of field values
     *
     * @throws IllegalArgumentException if field do not contained in index
     *
     */

    public List<Object> getAllValues(String field);

    /**
     * Gets map of all values of the field
     * @param field - field in index
     * @return - treemap with all values and indices of records for every value
     *
     * @throws IllegalArgumentException if field do not contained in index
     */

    public TreeMap getMap(String field);

    /**
     * Gets set of fields amount
     * @return - amount of fields
     */

    public int getFieldsAmount();

    /**
     * Sets confidence intervals for all fields in data set
     * @param confidenceLevel - the level of confidence (90%, 95%, 99% or any other). if equal NaN it throw off intervals
     */

    public void setConfidenceIntervals(double confidenceLevel);


    /**
     * Gets confidence intervals for fields in data set
     */

    public double[] getConfidenceIntervals();

    /**
     * Sets confidence intervals for fields in data set
     * @param confidenceLevels - the levels of confidence (90%, 95%, 99% or any other) for fields in data set order. if equal NaN it throw off the interval
     */

    public void setConfidenceIntervals(double[] confidenceLevels);


    /**
     * Gets map of records contains values of the field
     * @param field - enabled field from data set
     * @param value - value of the field
     * @return - map of records
     */
    public RoaringBitmap getRecords(String field, Object value);

    /**
     * Gets number of records in data set that is source of the index
     * @return - amount of records
     */

    public double getDataSetSize();

    double getP(Agent agent);

    double getFr(Agent agent);

    double getProductP(Agent agent);

    double getMR(Agent agent);

    void mergeAgents(Agent agent, Agent a1, Agent a2);

    void putCash(Agent agent, Object map);

    Object getCash(Agent agent);

    void clearCash(Agent agent);

    void clearCash(List<Agent> agents);

}
