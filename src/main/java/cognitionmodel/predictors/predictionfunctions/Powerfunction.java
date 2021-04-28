package cognitionmodel.predictors.predictionfunctions;

import cognitionmodel.models.Model;

/**
 * Class represents prediction function
 * p = p(relation | relation v ~predicted variable)^wp * z(relation)^wz
 *
 *
 */
public class Powerfunction implements Predictionfunction {

    double wp = 0, wz = 0;
    Model model;

    /**
     * Creates Power function class with parameters
     *
     * @param model - the model contains relations
     * @param wp - power of p
     * @param wz - power of z
     */


    public Powerfunction(Model model, double wp, double wz) {
        this.wp = wp;
        this.wz = wz;
        this.model = model;
    }

    @Override
    public double predictionfunction(int[] signature, int index) {

        double p = model.getFrequency(signature);
        if (p == 0) return 0;

        double z = model.getZ(signature);
        if (z == 0) return 0;

        model.getRelationMethods().addTermToRelation(signature, index, 0);
        double fp = model.getFrequency(signature);
        if (fp == 0) return 0;

        return Math.pow(p / fp, wp) * Math.pow(z, wz);
    }
}
