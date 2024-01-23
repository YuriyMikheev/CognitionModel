package cognitionmodel.models.inverted.producers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.index.InvertedIndex;
import cognitionmodel.models.inverted.composers.Composition;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;

import java.util.*;
import java.util.stream.Collectors;

public class Classifier implements Producer {

    private InvertedIndex invertedIndex;
    private String predictingField;
    private Predictionfunction predictionfunction;
    private int predictingFieldIndex;
    private Object[] predictingValues;

    public Classifier(InvertedIndex invertedIndex, String predictingField, Predictionfunction predictionfunction){
        this.invertedIndex = invertedIndex;
        this.predictingField = predictingField;
        this.predictionfunction = predictionfunction;
        this.predictingFieldIndex = invertedIndex.getFieldIndex(predictingField);

        predictingValues = invertedIndex.getAllValues(predictingField).toArray(new Object[]{});
    }

    public Tuple produce(HashMap<Object, List<Composition>> compositions) {

        Object o = Arrays.stream(predictingValues).max((o1, o2) -> {
            if (compositions.get(o1).isEmpty()) return -1;
            if (compositions.get(o2).isEmpty()) return 1;
            return compositions.get(o1).get(0).getMr() > compositions.get(o2).get(0).getMr() ? 1: -1;
        }).get();

        Tuple r = new Tuple().add(o).add("value").addAll(Arrays.stream(predictingValues).map(e -> compositions.get(e).isEmpty()? -100: compositions.get(e).get(0).getMr()).collect(Collectors.toList()));

        return r;
    }


    public String getPredictingField() {
        return predictingField;
    }
}
