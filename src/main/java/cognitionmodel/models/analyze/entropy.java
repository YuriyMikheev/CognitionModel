package cognitionmodel.models.analyze;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.Model;
import cognitionmodel.models.TabularModel;
import cognitionmodel.patterns.Pattern;
import cognitionmodel.patterns.PatternSet;

import javax.swing.text.html.HTMLDocument;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.round;

public class entropy {

    /**
     * Calculates information entropy of records as a whole H = - sum(p(record)*log(p(record))
     * @param tableDataSet - data set
     * @return information entropy of records
     */

    public static double recordsEntropy(TableDataSet tableDataSet){
        double r = 0;

        HashMap<String, Double> freqs = new HashMap<>();

        for (Tuple record: tableDataSet.getRecords()){
            String s = record.toString();
            freqs.compute(s,  (k, v) -> (v == null) ? 1: v+1);
        }

        for (Double f: freqs.values())
            r -= (f/tableDataSet.size())*log(f/tableDataSet.size());

        return r;
    }

    /**
     * Calculates informational entropy of the field H = - sum(p(record(fieldName)*log(p(record(fieldName))
     *
     * @param tableDataSet - data set
     * @param fieldName - name of the field
     * @return informational entropy of the field
     */

    public static double fieldEntropy(TableDataSet tableDataSet, String fieldName){
        double r = 0;

        HashMap<String, Double> freqs = new HashMap<>();

        int idx = tableDataSet.getFieldIndex(fieldName);

        for (Tuple record: tableDataSet.getRecords())
            if (record.size() > idx) {
                String s = record.get(idx).toString();
                freqs.compute(s,  (k, v) -> (v == null) ? 1: v+1);
            }

        for (Double f: freqs.values())
            r -= (f/tableDataSet.size())*log(f/tableDataSet.size());

        return r;
    }

    /**
     * Calculates informational entropy of all fields H = - sumByfileds(sum(p(record(fieldName))*log(p(record(fieldName)))
     *
     * @param tableDataSet - data set
     * @return informational entropy of the field
     */

    public static double fieldsEntropy(TableDataSet tableDataSet){
        double r = 0;

        for (TupleElement h:tableDataSet.getHeader())
            r += fieldEntropy(tableDataSet, h.getValue().toString());

        return r;
    }

    /**
     * Calculates informational entropy of all relations in model H = - sum(p(relation)*log(p(relation))
     *
     * @param model - model
     * @return informational entropy of the model
     */

    public static double modelEntropy(Model model){


        return modelEntropy(model, model.getDataSet().size());
    }

    /**
     * Calculates informational entropy of all relations in model H = - sum(p(relation)*log(p(relation))
     * @param dataSize - size of the data based for calculation of the probability
     * @param model - model
     * @return informational entropy of the model
     */

    public static double modelEntropy(Model model, double dataSize){
        double r = 0;

        Iterator<Map.Entry<int[], Integer>> relations = model.relationIterator();

        int i = 0;
        while (relations.hasNext()){
            double p = (double)relations.next().getValue() / dataSize;
            r -= p*log(p);
            if (p > 1) {
                System.err.println("Probability is more then 1:  "+ p );
                //break;
            }
            i++;
        }

        return r;
    }


    /**
     * Calculates informational entropy of all relations in model H = - sum(p(relation)*log(p(relation))
     * @param model - model
     * @return informational entropy of the model
     */

    public static double imagesModelEntropy(Model model){
        double r = 0;

        Iterator<Map.Entry<int[], Integer>> relations = model.relationIterator();

        HashMap<Integer, Long> l = new HashMap<>();

        long dataSize = round(model.getDataSet().size());

        for (Pattern pattern: model.getPatternSet()) {
            l.compute(pattern.getSetAmount(), (k, v) -> (v == null ? dataSize : v + dataSize));
        }

        while (relations.hasNext()){
            Map.Entry<int[], Integer> e = relations.next();
            double p = (double) e.getValue()  / (l.get(e.getKey().length));
            r -= p*log(p);
        }

        return r;
    }


}
