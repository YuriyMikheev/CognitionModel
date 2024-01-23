package cognitionmodel.predictors.predictionfunctions;

import cognitionmodel.models.Model;
import cognitionmodel.models.inverted.Agent;

/**
 * Interface for prediction functions
 */

public interface Predictionfunction {


    public double predictionfunction(int[] signature, int index);
    public double predictionfunction(Agent agent, String predictionfield);
    public double predictionfunction(Agent agent, Agent agentWithoutPredictionfield);

    public Model getModel();
}
