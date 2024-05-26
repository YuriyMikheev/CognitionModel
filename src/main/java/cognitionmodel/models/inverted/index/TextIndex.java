package cognitionmodel.models.inverted.index;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.InvertedTextModel;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.roaringbitmap.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
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

    String[] textData = new String[]{"datasetInfo", "indexStart", "IndexEnd"};
    ModelType modelType = ModelType.GPT_4_32K;
    String textField;
    private InvertedTextModel model;
    static long maxIndex = 0;
    int maxShortOffset = -1;
    int maxLongOffset = -1;

    Encoding encoder;

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

    public String getTextField() {
        return textField;
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
                    return null;
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

       // System.out.println("Optimized: "+optimize()+" bytes");

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
                RoaringBitmap rm =  (RoaringBitmap) getMap(textField+"0").get(value);//
                //RoaringBitmap rm =  RoaringBitmap.addOffset((RoaringBitmap) getMap(textField+"0").get(value), -Integer.parseInt(field.substring(textField.length())));
                return rm;
        }

        return (RoaringBitmap) getMap(field).get(value);
    }


    @Override
    public double getFr(Agent agent) {
        if (agent.getPoints().isEmpty()) return -1;

        RoaringBitmap rb = (RoaringBitmap) getCash(agent);//agent.getCachedRecords();
        if (rb == null) {
            rb = RoaringBitmap.and(getBitmapList(agent.getPoints()).listIterator(), 0L, min(round(getDataSetSize()), 0xffffffffL));
            putCash(agent, rb);
            //cash.put(agent.getSignature(), rb);
        }
        return rb.getCardinality();
    }


    private List<RoaringBitmap> getBitmapList(Collection<Point> points){
        LinkedList<RoaringBitmap> r = new LinkedList<>();
        for (Point point : points) {
            RoaringBitmap rb = getRecords(point.field, point.value);

            if (rb != null)
                r.add(rb);
        }
        return r;
    }

    @Override
    public double getMR(Agent agent){
        if (agent.getRelation().size() <= 1) return 0;

        double z = getFr(agent), f = 1;// records.getCardinality();

        int c = 1, l = 0;
        for (Point point: agent.getRelation().values()) {
            RoaringBitmap fieldrecords = getRecords(point.getField(), point.getValue());
            if (fieldrecords != null) {
                f = f * fieldrecords.getCardinality();
                l++;
                if (f > Double.MAX_VALUE / 1000000) { //prevents double value overflowing
                    f = f / getDataSetSize();
                    c++;
                }
            }
        }
        z = log(z / f) + (l - c) * log(getDataSetSize());
        agent.setMR(z);
        return z;
    }

    void makeFields() {
        fieldsList = new ArrayList<>();
        fields.clear();

        List<String> fl = invertedIndex.keySet().stream().filter(s -> s.contains(textField)).sorted((s1, s2) -> Integer.parseInt(s1.replace(textField, "")) > Integer.parseInt(s2.replace(textField, "")) ? 1 : -1).collect(Collectors.toList());
        di2i = new int[fl.size()];//int[1 + (maxShortOffset != -1 ? maxShortOffset - 1 : 0) + (maxLongOffset != -1 ? 1 : 0)];
        i2di = new int[di2i.length];
        int i = 0;
        for (String f : fl) {
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

    public TreeMap<Object, RoaringBitmap> getIdx(String field) {
        if (field.equals(textField)) field = field+"0";

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

    @Override
    public void mergeAgents(Agent agent, Agent a1, Agent a2) {
        if (agent.getCachedRecords() != null) return;

        RoaringBitmap r1, r2;

        r1 = (RoaringBitmap) getCash(a1);//cash.get(a1.getSignature());
        if (r1 == null)
            if (a1.getPoints().size() > 1)
                 putCash(a1, r1 = RoaringBitmap.and(getBitmapList(a1.getPoints()).listIterator(), 0L, maxIndex));//FastAggregation.and(getBitmapArray(a1.getPoints())));
            else
                //putCash(a1, r1 = (RoaringBitmap) getMap(f1 = a1.relation.firstEntry().getValue().getField()).get(a1.relation.firstEntry().getValue().getValue()));//getRecords(a1.relation.firstEntry().getValue().getField(), a1.relation.firstEntry().getValue().getValue()));
                putCash(a1, r1 = getRecords(a1.relation.firstEntry().getValue().getField(), a1.relation.firstEntry().getValue().getValue()));

        r2 = (RoaringBitmap) getCash(a2);//cash.get(a2.getSignature());
        if (r2 == null)
            if (a2.getPoints().size() > 1)
                putCash(a2, r2 = RoaringBitmap.and(getBitmapList(a2.getPoints()).listIterator(), 0L, maxIndex));
            else
                //putCash(a2, r2 = (RoaringBitmap) getMap(f2 = a2.relation.firstEntry().getValue().getField()).get(a2.relation.firstEntry().getValue().getValue()));//getRecords(a2.relation.firstEntry().getValue().getField(), a2.relation.firstEntry().getValue().getValue()));
                putCash(a2, r2 = getRecords(a2.relation.firstEntry().getValue().getField(), a2.relation.firstEntry().getValue().getValue()));

        if (r1 == null || r2 == null) return;
/*
        Integer s1 = fields.get(f1), s2 = fields.get(f2);

        if (s1 < s2) r2 = RoaringBitmap.addOffset(r2, s1 - s2);
            else
                if (s1 > s2) r1 = RoaringBitmap.addOffset(r1, s2 - s1);*/

        RoaringBitmap rm = and(r1, r2, a1.getFields().nextSetBit(0)-a2.getFields().nextSetBit(0));
       //RoaringBitmap rm = RoaringBitmap.and(r1, RoaringBitmap.addOffset(r2, a1.getFields().nextSetBit(0)-a2.getFields().nextSetBit(0)));

        putCash(agent, rm);
        //agent.getFr();
    }



    public static RoaringBitmap and(RoaringBitmap r1, RoaringBitmap r2, int shift4r2){
        if (r1.isEmpty() || r2.isEmpty()) return null;

        RoaringBitmap roaringBitmap = new RoaringBitmap();

        BatchedIterator mbi;
        BatchedIterator bbi;
        boolean rev = false;
        if (r1.first() > r2.first())
        {
            bbi = new BatchedIterator(r1);
            mbi = new BatchedIterator(r2);
        } else {
            bbi = new BatchedIterator(r2);
            mbi = new BatchedIterator(r1);
            shift4r2 = -shift4r2;
            rev = true;
        }


        long x = 0, y = bbi.nextAfter(0);
        while (mbi.hasNext() && bbi.hasNext() && x !=-1 && y!= -1){
            x = mbi.nextAfter(y-shift4r2);
            if (x == y-shift4r2 & x != -1) {
                if (rev) roaringBitmap.add(x, x+1);
                    else roaringBitmap.add(y, y+1);
                y = bbi.nextAfter(x+1+shift4r2);
            } else
                y = bbi.nextAfter(x+shift4r2);

        }

        return roaringBitmap;
    }


    public void save(OutputStream outputStream) throws IOException {

        ObjectOutputStream writer = new ObjectOutputStream(outputStream);

        writer.writeUTF(modelType.getName());
        writer.writeUTF(textField);
        writer.writeLong(maxIndex);
        writer.writeInt(maxShortOffset);
        writer.writeInt(maxLongOffset);

        writer.writeInt(textData.length);
        Arrays.stream(textData).forEach((f-> {
            try {
                writer.writeUTF(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        List<String> ff = Arrays.stream(textData).collect(Collectors.toList());
        ff.add(textField+"0");

        for (String fi: ff) {
            TreeMap<Object, RoaringBitmap> tm = getIdx(fi);
            writer.writeInt(tm.size());
            for (Map.Entry<Object, RoaringBitmap> e : tm.entrySet()) {
                writer.writeObject(e.getKey());
                RoaringBitmap rb = e.getValue();
                byte[] array = new byte[rb.serializedSizeInBytes()];
                writer.writeInt(array.length);
                e.getValue().serialize(ByteBuffer.wrap(array));
                writer.write(array);
            }
        }
        writer.close();
        outputStream.close();
    }

    public void load(InputStream inputStream) throws IOException, ClassNotFoundException {

        ObjectInputStream reader = new ObjectInputStream(inputStream);

        modelType = ModelType.fromName(reader.readUTF()).get();

        textField = reader.readUTF();
        maxIndex = reader.readLong();
        maxShortOffset = reader.readInt();
        maxLongOffset = reader.readInt();
        int fs = reader.readInt();

        for (int i = 0; i < fs; i++)
            textData[i] = reader.readUTF();

        List<String> ff = Arrays.stream(textData).collect(Collectors.toList());
        ff.add(textField+"0");

        for (String fi: ff) {
            TreeMap<Object, RoaringBitmap> tm = getIdx(fi);
            int n = reader.readInt();
            int pos = 0;
            for (int i = 0; i < n; i++) {
                RoaringBitmap rb = new RoaringBitmap();
                Object k = reader.readObject();
                byte[] array = new byte[reader.readInt()];
                int totalRead = 0;
                while (totalRead < array.length) {
                    int read = reader.read(array, totalRead, array.length - totalRead);
                    if (read == -1) {
                        throw new EOFException("Unexpected EOF stream");
                    }
                    totalRead += read;
                }
                rb.deserialize(ByteBuffer.wrap(array));
                tm.put(k, rb);
            }
        }

        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        encoder = registry.getEncodingForModel(modelType);//registry.getEncoding(EncodingType.CL100K_BASE);

        if (maxShortOffset != -1 & maxLongOffset != -1)
            makeShiftedIndexes(maxShortOffset, maxLongOffset);
        else
            makeFields();

        reader.close();
        inputStream.close();
    }

    public double size(){
        double s = 0;
        TreeMap<Object, RoaringBitmap> tm =  getIdx(textField+"0");
        for (Map.Entry<Object, RoaringBitmap> e : tm.entrySet()) {
            s += e.getValue().getLongSizeInBytes();
        }
        return s;
    }


}