package cognitionmodel.predictors.predictionfunctions;

import cognitionmodel.models.inverted.BitAgent;

/**
 * Interface for prediction functions
 */

public interface Predictionfunction {


    public double predictionfunction(int[] signature, int index);
    public double predictionfunction(BitAgent agent, String predictionfield);

}
