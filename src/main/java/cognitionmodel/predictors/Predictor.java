package cognitionmodel.predictors;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.models.Model;

/**
 * Encapsulates predictor method applied to @param Model
 */

public class Predictor {

    private Model model;

    /**
     * Creates predictor object fo the @param model
     */


    public Predictor(Model model){
        this.model = model;
    }

    /**
     * Makes predictions for the data in @param signatureIndex from the @param dataSet
     *
     *
     * @param dataSet - data set with data for making predictions
     * @param signatureIndex - index of the unknown data in the @param model relations
     * @return - results of predictions saved in PredictionResults object
     */


    public PredictionResults predict(DataSet dataSet, int signatureIndex){


        return null;
    }


}
