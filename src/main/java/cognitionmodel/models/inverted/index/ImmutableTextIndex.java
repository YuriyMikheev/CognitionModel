package cognitionmodel.models.inverted.index;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.InvertedTextModel;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import org.roaringbitmap.buffer.MutableRoaringArray;
import org.roaringbitmap.buffer.MutableRoaringBitmap;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static java.lang.Math.log;

public class ImmutableTextIndex extends TextIndex {

    protected HashMap<String, TreeMap<Object, ImmutableRoaringBitmap>> invertedIndex = new HashMap();


    public ImmutableTextIndex(InvertedTabularModel model) {
        super(model);
    }

    public ImmutableTextIndex(InvertedTextModel model, String textField) {
        super(model, textField);
    }

    public ImmutableTextIndex(InvertedTextModel model, String textField, TableDataSet dataSet, String datasetInfo) {
        super(model, textField, dataSet, datasetInfo);
    }

    public ImmutableTextIndex(InvertedTextModel model, String textField, TableDataSet dataSet, String datasetInfo, int maxShortOffset, int maxLongOffset) {
        super(model, textField, dataSet, datasetInfo, maxShortOffset, maxLongOffset);
    }

    @Override
    public void makeIndex(TableDataSet dataSet, String datasetInfo) {
        System.out.println("Cannot make immutable index");
    }

    @Override
    public long optimize() {
        System.out.println("Cannot optimize immutable index");
        return 0;
    }

    @Override
    public void makeShiftedIndexes(int maxShortOffset, int maxLongOffset) {
        setMaxLongOffset(maxLongOffset);
        setMaxShortOffset(maxShortOffset);

        if (maxShortOffset != -1)
            for (int i = 1; i < maxShortOffset; i++) {
                TreeMap<Object, ImmutableRoaringBitmap> nidx = getImmutableIdx(getTextField() + i);

            }

        if (maxLongOffset != -1) {
            TreeMap<Object, ImmutableRoaringBitmap> nidx = getImmutableIdx(getTextField() + "_long");

            for (Map.Entry<Object, ImmutableRoaringBitmap> e : invertedIndex.get(getTextField()).entrySet()) {
                MutableRoaringBitmap nr = new MutableRoaringBitmap();
                e.getValue().forEach((IntConsumer) a -> {
                    nr.add((long) a, a + maxLongOffset);
                });
                nidx.put(e.getKey(), nr);
            }
        }

        makeFields();
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
/*    @Override
    public RoaringBitmap getRecords(String field, Object value){

        return getImmutableRecords(field, value).toRoaringBitmap();
    }*/


    public ImmutableRoaringBitmap getImmutableRecords(String field, Object value){
        if (field.contains(getTextField()))
            if (!field.equals(getTextField()+"0")){
                MutableRoaringBitmap rm =  MutableRoaringBitmap.addOffset((ImmutableRoaringBitmap) getMap(getTextField()+"0").get(value), -Integer.parseInt(field.substring(getTextField().length())));
                return rm.toImmutableRoaringBitmap();
            }

        return (ImmutableRoaringBitmap) getMap(field).get(value);
    }


    @Override
    public double getFr(Agent agent) {
        if (agent.getPoints().isEmpty()) return -1;

        ImmutableRoaringBitmap rb = (ImmutableRoaringBitmap) getCash(agent);//agent.getCachedRecords();
        if (rb == null) {
            rb = ImmutableRoaringBitmap.and(getBitmapList(agent.getPoints()).listIterator(), 0L, min(round(getDataSetSize()), 0xffffffffL));
            putCash(agent, rb);
            //cash.put(agent.getSignature(), rb);
        }
        return rb.getCardinality();
    }

    private List<ImmutableRoaringBitmap> getBitmapList(Collection<Point> points){
        LinkedList<ImmutableRoaringBitmap> r = new LinkedList<>();
        for (Point point : points) {
            ImmutableRoaringBitmap rb = getImmutableRecords(point.field, point.value);

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
            ImmutableRoaringBitmap fieldrecords = getImmutableRecords(point.getField(), point.getValue());
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

    protected TreeMap<Object, ImmutableRoaringBitmap> getImmutableIdx(String field) {
        if (!invertedIndex.containsKey(field))
            invertedIndex.put(field, new TreeMap<>());

        return invertedIndex.get(field);
    }

/*    protected TreeMap<Object, RoaringBitmap> getIdx(String field) {
        throw new UnsupportedOperationException("Not available here");
    }*/


    @Override
    public void mergeAgents(Agent agent, Agent a1, Agent a2) {
        if (agent.getCachedRecords() != null) return;

        ImmutableRoaringBitmap r1, r2;
        String f1 = null, f2 = null;
        r1 = (ImmutableRoaringBitmap) getCash(a1);//cash.get(a1.getSignature());
        if (r1 == null)
            if (a1.getPoints().size() > 1)
                putCash(a1, r1 = ImmutableRoaringBitmap.and(getBitmapList(a1.getPoints()).listIterator(), 0L, (long)Integer.MAX_VALUE*2-1).toImmutableRoaringBitmap());//FastAggregation.and(getBitmapArray(a1.getPoints())));
            else
                //putCash(a1, r1 = (RoaringBitmap) getMap(f1 = a1.relation.firstEntry().getValue().getField()).get(a1.relation.firstEntry().getValue().getValue()));//getRecords(a1.relation.firstEntry().getValue().getField(), a1.relation.firstEntry().getValue().getValue()));
                putCash(a1, r1 = getImmutableRecords(a1.relation.firstEntry().getValue().getField(), a1.relation.firstEntry().getValue().getValue()));

        r2 = (ImmutableRoaringBitmap) getCash(a2);//cash.get(a2.getSignature());
        if (r2 == null)
            if (a2.getPoints().size() > 1)
                putCash(a2, r2 = ImmutableRoaringBitmap.and(getBitmapList(a2.getPoints()).listIterator(), 0L, (long)Integer.MAX_VALUE*2-1));
            else
                //putCash(a2, r2 = (RoaringBitmap) getMap(f2 = a2.relation.firstEntry().getValue().getField()).get(a2.relation.firstEntry().getValue().getValue()));//getRecords(a2.relation.firstEntry().getValue().getField(), a2.relation.firstEntry().getValue().getValue()));
                putCash(a2, r2 = getImmutableRecords(a2.relation.firstEntry().getValue().getField(), a2.relation.firstEntry().getValue().getValue()));

        if (r1 == null || r2 == null) return;
/*
        Integer s1 = fields.get(f1), s2 = fields.get(f2);

        if (s1 < s2) r2 = RoaringBitmap.addOffset(r2, s1 - s2);
            else
                if (s1 > s2) r1 = RoaringBitmap.addOffset(r1, s2 - s1);*/

        putCash(agent, ImmutableRoaringBitmap.and(r1, r2));

    }

    public void load(InputStream inputStream) throws IOException, ClassNotFoundException {

        ObjectInputStream reader = new ObjectInputStream(inputStream);

        modelType = ModelType.fromName(reader.readUTF()).get();

        textField = reader.readUTF();
        maxIndex = reader.readLong();
        setMaxShortOffset(reader.readInt());
        setMaxLongOffset(reader.readInt());
        int fs = reader.readInt();

        for (int i = 0; i < fs; i++)
            textData[i] = reader.readUTF();

        List<String> ff = Arrays.stream(textData).collect(Collectors.toList());
        ff.add(textField+"0");

        for (String fi: ff) {
            TreeMap<Object, ImmutableRoaringBitmap> tm = getImmutableIdx(fi);
            int n = reader.readInt();
            int pos = 0;
            for (int i = 0; i < n; i++) {
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
                ImmutableRoaringBitmap rb = new ImmutableRoaringBitmap(ByteBuffer.wrap(array));
                //rb.deserialize(ByteBuffer.wrap(array));
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

    @Override
    public TreeMap getMap(String field) {
        if (!invertedIndex.containsKey(field))
            throw new IllegalArgumentException("Field " + field + " is not contained in index") ;
        return invertedIndex.get(field);
    }

    @Override
    public int getFieldsAmount() {
        return invertedIndex.entrySet().size();
    }


}
