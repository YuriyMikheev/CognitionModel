package cognitionmodel.predictors.predictionfunctions;

import cognitionmodel.models.Model;
import cognitionmodel.models.TabularModel;

/**
 * Class represents prediction function
 * the function uses TabularModel.zd() instead of Model.z()
 * p = p(relation | relation v ~predicted variable)^wp * zd(relation)^wz
 *
 *
 */
public class ZdPowerfunction implements Predictionfunction {

    double wp = 0, wz = 0;
    TabularModel model;

    /**
     * Creates Power function class with parameters
     *
     * @param model - the model contains relations
     * @param wp - power of p
     * @param wz - power of z
     */


    public ZdPowerfunction(TabularModel model, double wp, double wz) {
        this.wp = wp;
        this.wz = wz;
        this.model = model;
    }

    @Override
    public double predictionfunction(int[] signature, int index) {

        double p = model.getFrequency(signature);
        if (p == 0) return 0;

        double z = model.getZd(signature);
        if (z == 0) return 0;

        model.getRelationMethods().addTermToRelation(signature, index, 0);
        double fp = model.getFrequency(signature);
        if (fp == 0) return 0;

        double r = Math.pow(p / fp, wp) * Math.pow(z, wz);

        if (Double.isNaN(r))
            r = r/1;

        return r;
    }
}
