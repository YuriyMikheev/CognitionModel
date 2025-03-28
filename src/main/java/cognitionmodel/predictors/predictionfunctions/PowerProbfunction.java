package cognitionmodel.predictors.predictionfunctions;

import cognitionmodel.models.Model;
import cognitionmodel.models.inverted.Agent;

import static java.lang.StrictMath.pow;

/**
 * Class represents prediction function
 * p = p(relation | relation v ~predicted variable)^wp * z(relation)^wz
 *
 *
 */
public class PowerProbfunction implements Predictionfunction {

    double wp = 0, wz = 0;
    Model model;

    /**
     * Creates Power function class with parameters
     *
     * @param model - the model contains relations
     * @param wp - power of p
     * @param wz - power of z
     */


    public PowerProbfunction(Model model, double wp, double wz) {
        this.wp = wp;
        this.wz = wz;
        this.model = model;
    }

    public double getWp() {
        return wp;
    }

    public double getWz() {
        return wz;
    }

    @Override
    public double predictionfunction(int[] signature, int index) {

        double p = model.getFrequency(signature);
        if (p == 0) return 0;

        double z = (double) model.getFrequency(signature) / model.getDataSet().size();
        if (z == 0) return 0;

        model.getRelationMethods().addTermToRelation(signature, index, 0);
        double fp = model.getFrequency(signature);
        if (fp == 0) return 0;

        return Math.pow(p / fp, wp) * Math.pow(z, wz);
    }

    @Override
    public double predictionfunction(Agent agent, String predictingfield) {
        if (wp == 0) return pow(agent.getP(),wz);
        return  pow(agent.getCondP(predictingfield), wp) * pow(agent.getP(),wz);
    }

    @Override
    public double predictionfunction(Agent agent, Agent agentWithoutPredictionfield) {
        if (wp == 0 | agentWithoutPredictionfield == null) return pow(agent.getP(),wz);
        return pow((double) agent.getP(), wp) * pow(agent.getMR(),wz);
    }

    @Override
    public Model getModel() {
        return model;
    }
}
