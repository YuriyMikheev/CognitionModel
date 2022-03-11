package cognitionmodel.predictors.predictionfunctions;

import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;

/**
 * Interface for prediction functions
 */

public interface Predictionfunction {


    public double predictionfunction(int[] signature, int index);
    public double predictionfunction(Agent agent, String predictionfield);

}
