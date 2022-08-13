package cognitionmodel.models.inverted;

import cognitionmodel.datasets.Tuple;
import org.roaringbitmap.RoaringBitmap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.abs;

public class DynamicIntervaledBitInvertedIndex extends BitInvertedIndex{

    private HashMap<String, TreeMap<Object, RoaringBitmap>> sourceInvertedIndex;
    private Tuple record;
    private String predictingField;

    /**
     * Creates IntervaledBitInvertedIndex based on BitInvertedIndex
     * Makes intervals according to confidential intervals from the BitInvertedIndex and the record from the DataSet
     *
     * @param invertedIndex - sourced BitInvertedIndex
     * @param record - record from the DataSet
     * @param predictingField - field that out to be predicted
     *
     */
    public DynamicIntervaledBitInvertedIndex(BitInvertedIndex invertedIndex, Tuple record, String predictingField){
        super();

        this.record = record;
        this.predictingField = predictingField;

        this.dataSet = invertedIndex.dataSet;
        this.model = invertedIndex.model;
        this.fields = invertedIndex.fields;
        this.fieldsList = invertedIndex.fieldsList;
        if (invertedIndex.confidenceLevels != null)
            confidenceLevels = Arrays.copyOf(invertedIndex.confidenceLevels, invertedIndex.confidenceLevels.length);
        else
            throw new IllegalArgumentException("Confidence intervals are not setted. Making index is impossible");

        this.di2i = invertedIndex.di2i;
        this.i2di = invertedIndex.i2di;

        sourceInvertedIndex = invertedIndex.invertedIndex;
        init();
    }

    /**
     * Makes indices for confidential intervals of record values
     */

    private void init(){

        for (String field: fieldsList) {

            if (field.equals(predictingField)) {
                invertedIndex.put(field, sourceInvertedIndex.get(field));
                continue;
            }

            int fi = invertedIndexToDatasetFieldIndex(getFieldIndex(field));
            int ifi = getFieldIndex(field);
            Object value = record.get(fi).getValue();
            RoaringBitmap r = new RoaringBitmap();

            if (value.getClass().getSuperclass() == Number.class) {
                value = Double.parseDouble(value.toString());

                RoaringBitmap  ra = new RoaringBitmap(), rb = new RoaringBitmap();

                TreeMap<Object, RoaringBitmap> values = sourceInvertedIndex.get(field);

                Map.Entry<Object, RoaringBitmap> a; // = values.ceilingEntry(value);
                Map.Entry<Object, RoaringBitmap> b;// = values.floorEntry(value);

                try {
                    a = values.ceilingEntry(value);
                    b = values.floorEntry(value);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(value + " " + value.getClass() + " do not fit to type of data set field \"" + field + "\"");
                }

                long ac, bc;

                if (values.containsKey(value)) {
                    r = values.get(value);
                    a = values.lowerEntry(a.getKey());
                    ra.or(r);
                    b = values.higherEntry(b.getKey());
                    rb.or(r);
                }

                double thr = dataSet.size() * (1 - confidenceLevels[ifi]);
                boolean sa = true;
                boolean sb = true;
                int c = 0;

                while (r.getCardinality() <= thr & (sa || sb)) {
                    sa = false;
                    sb = false;

                    if (a != null) {
                        ra.or(a.getValue());
                        sa = ra.getCardinality() < thr;
                        a = values.lowerEntry(a.getKey());
                    }
                    if (b != null) {
                        rb.or(b.getValue());
                        sb = rb.getCardinality() < thr;
                        b = values.higherEntry(b.getKey());
                    }

                    if (sa & sb) {
                        if (RoaringBitmap.or(ra,rb).getCardinality() > thr) {
                            sa = ra.getCardinality() < rb.getCardinality();
                            sb = ra.getCardinality() > rb.getCardinality();
                            if (!sb & !sa) sa = true;
                        }
                    }

                    if (sa) r.or(ra);
                    if (sb) r.or(rb);
                    c++;
                }

                confidenceLevels[ifi] = 1 - (c <= 1 ? 0: (double) r.getCardinality() / dataSet.size());

            } else {
                try {
                    r = sourceInvertedIndex.get(field).get(value);
                } catch (ClassCastException e){
                  //  System.out.println("!!!");
                    r = null;
                }
                confidenceLevels[ifi] = 1.0;
            }
            invertedIndex.put(field, new TreeMap<>());
            invertedIndex.get(field).put(value, r);

        }

    }

}
