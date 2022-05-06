package cognitionmodel.predictors;

import cognitionmodel.datasets.DataSet;
import cognitionmodel.models.Model;

/**
 * Encapsulates predictor method applied to @param Model
 */

public interface Predictor {


    /**
     * Makes predictions for the data in @param signatureIndex from the @param dataSet based on @param model
     *
     * @param model - model underlines prediction
     * @param dataSet - data set with data for making predictions
     * @param signatureIndex - index of the unknown data in the @param model relations
     * @return - results of predictions saved in PredictionResults object
     */


    public static PredictionResults predict(Model model, DataSet dataSet, int signatureIndex){

        throw new AbstractMethodError("Method is not implemented in the interface");

    }


}
