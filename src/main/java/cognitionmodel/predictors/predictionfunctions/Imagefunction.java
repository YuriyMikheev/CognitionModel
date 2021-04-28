package cognitionmodel.predictors.predictionfunctions;

import cognitionmodel.models.Model;

/**
 * Class represents prediction function for images
 *
 *
 */


public class Imagefunction implements Predictionfunction {

    Model model;

    /**
     * Creates Power function class with parameters
     *
     * @param model - the model contains relations
     */


    public Imagefunction(Model model) {

        this.model = model;
    }

    @Override
    public double predictionfunction(int[] signature, int index) {

        return model.getZfast(signature);
    }
}