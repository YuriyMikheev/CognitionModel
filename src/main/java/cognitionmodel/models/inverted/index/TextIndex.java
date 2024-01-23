package cognitionmodel.models.inverted.index;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.InvertedTextModel;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.roaringbitmap.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

import static java.lang.Math.*;


public class TextIndex extends BitInvertedIndex {

    public TextIndex(InvertedTabularModel model) {
        throw new RuntimeException("TextIndex requires more parameters to be constructed.");
    }

    private String[] textData = new String[]{"datasetInfo", "indexStart", "IndexEnd"};
    private ModelType modelType = ModelType.GPT_4_32K;
    private String textField;
    private InvertedTextModel model;
    private static long maxIndex = 0;
    private int maxShortOffset = -1, maxLongOffset = -1;

    private Encoding encoder;

    public TextIndex(InvertedTextModel model, String textField) {
        this.model = model;
        this.textField = textField;
    }

    public TextIndex(InvertedTextModel model, String textField, TableDataSet dataSet, String datasetInfo) {
        this(model, textField);
        if (dataSet != null) makeIndex(dataSet, datasetInfo);
    }

    public TextIndex(InvertedTextModel model, String textField, TableDataSet dataSet, String datasetInfo, int maxShortOffset, int maxLongOffset) {
        this.model = model;
        this.textField = textField;
        this.maxShortOffset = maxShortOffset;
        this.maxLongOffset = maxLongOffset;
        if (dataSet != null) makeIndex(dataSet, datasetInfo);
    }

    public int getMaxShortOffset() {
        return maxShortOffset;
    }

    public void setMaxShortOffset(int maxShortOffset) {
        this.maxShortOffset = maxShortOffset;
    }

    public int getMaxLongOffset() {
        return maxLongOffset;
    }

    public void setMaxLongOffset(int maxLongOffset) {
        this.maxLongOffset = maxLongOffset;
    }

    public void makeIndex(TableDataSet dataSet, String datasetInfo) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        encoder = registry.getEncodingForModel(modelType);//registry.getEncoding(EncodingType.CL100K_BASE);

        int textFieldId = dataSet.getHeader().findFirstIndex(textField);

        String tfs = textField + "0";

        TreeMap<Object, RoaringBitmap> tm =  getIdx(tfs);


        AtomicInteger i = new AtomicInteger();
        try {
            LinkedList<CompletableFuture<Object>> cfl = new LinkedList<>();

            for (Tuple tuple : dataSet) {
                cfl.add(CompletableFuture.supplyAsync(() -> {
                    int j = 0;
                    int fi = i.get();
                    HashMap<Object, RoaringBitmapWriter<RoaringBitmap>> ttm = new HashMap<>();

                    for (TupleElement tupleElement : tuple) {
                        if (j == textFieldId) {
                            String text = tupleElement.getValue().toString();
                            List<Integer> tokens = encoder.encode(text);
                            long index = addMaxIndex(tokens.size());
                            if (maxIndex >= 0xffffffffL) return null;
                               // throw new OutOfRangeException(maxIndex + tokens.size(), 0, 0xffffffff);
                            putValue("indexStart", index, fi);
                            for (Integer t : tokens) {
                                RoaringBitmapWriter<RoaringBitmap> idx;
                                if (ttm.containsKey(t))
                                    idx = ttm.get(t);
                                else {
                                    //idx = new RoaringBitmap();
                                    idx = RoaringBitmapWriter.writer().optimiseForRuns().get();
                                    ttm.put(t, idx);
                                }
                                idx.add(index++, index);
                            }
                            putValue("indexEnd", index, fi);
                        } else {
                            String fieldName = dataSet.getHeader().get(j).getValue().toString();
                            Object val = tupleElement.getValue();
                            if (tupleElement.getType() != TupleElement.Type.Empty)
                                putValue(fieldName, val, fi);
                        }
                        j++;
                    }
                    putValue("datasetInfo", datasetInfo, fi);
                    synchronized (this) {
                        for (Map.Entry<Object, RoaringBitmapWriter<RoaringBitmap>> e : ttm.entrySet())
                            if (tm.containsKey(e.getKey()))
                                tm.get(e.getKey()).or(e.getValue().get());
                            else
                                tm.put(e.getKey(), e.getValue().get());
                    }
                    return ttm;
                }));
                synchronized (this) {
                    i.getAndIncrement();
                }
                if (i.get() % (int) (dataSet.getRecords().size() * 0.01 + 1) == 0 | i.get() == dataSet.getRecords().size())
                //if (i.get() % 10 == 0 || i.get() == dataSet.getRecords().size())
                {
                    cfl.stream().map(CompletableFuture::join).collect(Collectors.toList());
                    cfl.clear();
                }
            }
        } catch (OutOfRangeException e) {
            System.err.println("Out of max index capacity");
        }

        System.out.println("Optimized: "+optimize()+" bytes");

        if (maxShortOffset != -1 & maxLongOffset != -1)
            makeShiftedIndexes(maxShortOffset, maxLongOffset);
        else
            makeFields();

    }

    public long optimize(){
        double os = 0;
        TreeMap<Object, RoaringBitmap> tm =  getIdx(textField+"0");
        for (Map.Entry<Object, RoaringBitmap> e : tm.entrySet()) {
            os += e.getValue().getLongSizeInBytes();
            e.getValue().runOptimize();
            os -= e.getValue().getLongSizeInBytes();
        }

        return round(os);
    }

    public void makeShiftedIndexes(int maxShortOffset, int maxLongOffset) {
        this.maxLongOffset = maxLongOffset;
        this.maxShortOffset = maxShortOffset;

        if (maxShortOffset != -1)
            for (int i = 1; i < maxShortOffset; i++) {
                TreeMap<Object, RoaringBitmap> nidx = getIdx(textField + i);

/*                for (Map.Entry<Object, RoaringBitmap> e : invertedIndex.get(textField + "0").entrySet()) {
                    nidx.put(e.getKey(), RoaringBitmap.addOffset(e.getValue(), -i));
                }*/
            }

        if (maxLongOffset != -1) {
            TreeMap<Object, RoaringBitmap> nidx = getIdx(textField + "_long");

            for (Map.Entry<Object, RoaringBitmap> e : invertedIndex.get(textField).entrySet()) {
                RoaringBitmap nr = new RoaringBitmap();
                e.getValue().forEach((IntConsumer) a -> {
                    nr.add((long) a, a + maxLongOffset);
                });
                nidx.put(e.getKey(), nr);
            }
        }

        makeFields();
    }

    @Override
    public RoaringBitmap getRecords(String field, Object value){
        if (field.contains(textField))
            if (!field.equals(textField+"0")){
                RoaringBitmap rm = RoaringBitmap.addOffset((RoaringBitmap) getMap(textField+"0").get(value), -Integer.parseInt(field.substring(textField.length())));
                return rm;
        }

        return (RoaringBitmap) getMap(field).get(value);
    }


    private void makeFields() {
        fieldsList = new ArrayList<>();
        fields.clear();

        di2i = new int[1 + (maxShortOffset != -1 ? maxShortOffset - 1 : 0) + (maxLongOffset != -1 ? 1 : 0)];
        i2di = new int[di2i.length];
        int i = 0;
        for (String f : invertedIndex.keySet().stream().filter(s -> s.contains(textField)).sorted((s1, s2) -> Integer.parseInt(s1.replace(textField, "")) > Integer.parseInt(s2.replace(textField, "")) ? 1 : -1).collect(Collectors.toList())) {
            di2i[i] = fields.size();
            i2di[fields.size()] = i;
            fields.put(f, fields.size());
            fieldsList.add(f);
            i++;
        }
    }


    public synchronized long addMaxIndex(long amount) {
        long oi = maxIndex;
        maxIndex += amount;
        return oi;
    }

    private synchronized void  putValue(String field, Object value, long index) {
        RoaringBitmap idx;
        if (!invertedIndex.containsKey(field))
            invertedIndex.put(field, new TreeMap<>());
        if (invertedIndex.get(field).containsKey(value))
            idx = invertedIndex.get(field).get(value);
        else {
            idx = new RoaringBitmap();
            invertedIndex.get(field).put(value, idx);
        }
        idx.add(index, index + 1l);
    }

    private TreeMap<Object, RoaringBitmap> getIdx(String field) {
        if (!invertedIndex.containsKey(field))
            invertedIndex.put(field, new TreeMap<>());

        return invertedIndex.get(field);
    }

    public Encoding getEncoder() {
        return encoder;
    }

    @Override
    public double getDataSetSize() {
        return maxIndex;
    }
    @Override
    public Integer getFieldIndex(String field){
        if (field.equals(textField)) return fields.get(textField+"0");
        return (fields.containsKey(field)?fields.get(field):-1);
    }
}