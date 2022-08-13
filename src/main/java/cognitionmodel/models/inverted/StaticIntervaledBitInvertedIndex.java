package cognitionmodel.models.inverted;

import org.roaringbitmap.RoaringBitmap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.lang.Math.round;

public class StaticIntervaledBitInvertedIndex extends BitInvertedIndex{

    private HashMap<String, TreeMap<Object, RoaringBitmap>> sourceInvertedIndex;
    private String predictingField;
    private int numofintevals;
    private double procentile;
    private double[] percenteles;


    /**
     * Creates IntervaledBitInvertedIndex based on BitInvertedIndex
     * Makes equal intervals that split numerical fields to intervals with equal range of values
     *
     * @param invertedIndex - sourced BitInvertedIndex
     * @param predictingField - field that out to be predicted
     * @param numofintervals - amount of intervals
     *
     */
    public StaticIntervaledBitInvertedIndex(BitInvertedIndex invertedIndex, String predictingField, int numofintervals){
        super();

        this.predictingField = predictingField;
        this.numofintevals = numofintervals;

        this.dataSet = invertedIndex.dataSet;
        this.model = invertedIndex.model;
        this.fields = invertedIndex.fields;
        this.fieldsList = invertedIndex.fieldsList;
        this.di2i = invertedIndex.di2i;
        this.i2di = invertedIndex.i2di;

        sourceInvertedIndex = invertedIndex.invertedIndex;
        initamount();
    }


    /**
     * Creates IntervaledBitInvertedIndex based on BitInvertedIndex
     * Makes equal intervals that split numerical fields to intervals with equal range of values
     *
     * @param invertedIndex - sourced BitInvertedIndex
     * @param predictingField - field that out to be predicted
     * @param percentile - percentile of every interval
     *
     */
    public StaticIntervaledBitInvertedIndex(BitInvertedIndex invertedIndex, String predictingField, double percentile){
        super();

        this.predictingField = predictingField;
        this.procentile = percentile;

        this.dataSet = invertedIndex.dataSet;
        this.model = invertedIndex.model;
        this.fields = invertedIndex.fields;
        this.fieldsList = invertedIndex.fieldsList;
        this.di2i = invertedIndex.di2i;
        this.i2di = invertedIndex.i2di;

        sourceInvertedIndex = invertedIndex.invertedIndex;

        percenteles = new double[(int)round(1.0/percentile)];
        Arrays.fill(percenteles, percentile);

        initprocentile(percenteles);
    }

    /**
     * Creates IntervaledBitInvertedIndex based on BitInvertedIndex
     * Makes equal intervals that split numerical fields to intervals with equal range of values
     *
     * @param invertedIndex - sourced BitInvertedIndex
     * @param predictingField - field that out to be predicted
     * @param percentiles - array of percentiles of every interval
     *
     */
    public StaticIntervaledBitInvertedIndex(BitInvertedIndex invertedIndex, String predictingField, double[] percentiles){
        super();

        this.predictingField = predictingField;
        this.procentile = procentile;

        this.dataSet = invertedIndex.dataSet;
        this.model = invertedIndex.model;
        this.fields = invertedIndex.fields;
        this.fieldsList = invertedIndex.fieldsList;
        this.di2i = invertedIndex.di2i;
        this.i2di = invertedIndex.i2di;

        sourceInvertedIndex = invertedIndex.invertedIndex;

        double s = Arrays.stream(percentiles).sum();
        if (s > 1) {
            for (int i = 0; i < percentiles.length; i++)
                percentiles[i] = percentiles[i] / s;
            System.err.print("Sum of percentiles in StaticIntervaledBitInvertedIndex higher then 1.0. Normalizing percentiles");
            Arrays.stream(percentiles).forEach(d -> {
                System.err.print(d + " ");
            });
        }

        initprocentile(percentiles);
    }

    /**
     * Makes indices for confidential intervals of record values
     */

    private void initamount(){

        for (String field: fieldsList) {

            if (field.equals(predictingField)) {
                invertedIndex.put(field, sourceInvertedIndex.get(field));
                continue;
            }

            Object value = sourceInvertedIndex.get(field).keySet().stream().findFirst().get();

            if (value.getClass().getSuperclass() == Number.class) {
                value = Double.parseDouble(value.toString());

                TreeMap<Object, RoaringBitmap> values = sourceInvertedIndex.get(field);

                double mx = values.keySet().stream().mapToDouble(o -> (Double) o).max().getAsDouble();
                double mi = values.keySet().stream().mapToDouble(o -> (Double) o).min().getAsDouble();
                double intervalrange = (mx*1.0001 - mi) / numofintevals;
                TreeMap<Object, RoaringBitmap> rt = new TreeMap<>();
                RoaringBitmap r = new RoaringBitmap(); rt.put(mi, r);
                invertedIndex.put(field, rt);

                for (Map.Entry<Object, RoaringBitmap> val: values.entrySet()){
                    if ((Double)val.getKey() >= mi + intervalrange * rt.size()){
                        r = new RoaringBitmap(); rt.put(mi + intervalrange * rt.size(), r);
                        r.or(val.getValue());
                    } else
                        r.or(val.getValue());
                }

            } else {
                try {
                    invertedIndex.put(field, sourceInvertedIndex.get(field));
                } catch (ClassCastException e){
                  //  System.out.println("!!!");
                }
            }
        }
    }

    private void initprocentile(double procentiles[]){

        for (String field: fieldsList) {

            if (field.equals(predictingField)) {
                invertedIndex.put(field, sourceInvertedIndex.get(field));
                continue;
            }

            Object value = sourceInvertedIndex.get(field).keySet().stream().findFirst().get();

            if (value.getClass().getSuperclass() == Number.class) {
                value = Double.parseDouble(value.toString());

                TreeMap<Object, RoaringBitmap> values = sourceInvertedIndex.get(field);

                double mi = values.keySet().stream().mapToDouble(o -> (Double) o).min().getAsDouble();

                TreeMap<Object, RoaringBitmap> rt = new TreeMap<>();
                RoaringBitmap r = new RoaringBitmap(); rt.put(mi, r);
                invertedIndex.put(field, rt);

                int i = 0;
                for (Map.Entry<Object, RoaringBitmap> val: values.entrySet()){
                    if (r.getCardinality() >= procentiles[i] * model.getDataSet().size()){
                        mi = (double)val.getKey(); i++;
                        r = new RoaringBitmap(); rt.put(mi , r);
                        r.or(val.getValue());
                    } else
                        r.or(val.getValue());
                }

            } else {
                try {
                    invertedIndex.put(field, sourceInvertedIndex.get(field));
                } catch (ClassCastException e){
                    //  System.out.println("!!!");
                }
            }
        }
    }


    @Override
    public RoaringBitmap getRecords(String field, Object value){
        if (value.getClass().getSuperclass() == Number.class) {
            value = Double.parseDouble(value.toString());
            Map.Entry<Object, RoaringBitmap> e = getMap(field).floorEntry(value);
            if (e != null)
                return (RoaringBitmap) getMap(field).floorEntry(value).getValue();
            else
                return (RoaringBitmap) getMap(field).firstEntry().getValue();
        }
        try {
            return (RoaringBitmap) getMap(field).get(value);
        } catch (ClassCastException e){
            return null;
        }
    }
}
