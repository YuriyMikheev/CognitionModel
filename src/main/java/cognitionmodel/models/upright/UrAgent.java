package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.Point;
import org.roaringbitmap.RoaringBitmap;

import java.util.BitSet;
import java.util.LinkedList;

import static java.lang.Double.NaN;
import static java.lang.Math.log;

public class UrAgent{
    private String tokens = "";
    private LinkedList<UrPoint> relations = new LinkedList<>();
    private long f = 0, datasize = 0;
    private double mr = NaN;

    private int[] tokensFreqs;
    private long startpos;
    private RoaringBitmap idx = new RoaringBitmap();

    private BitSet fields = new BitSet();

    public static final double zeroMr = 0;// zeroMR gives more MR to the compositions that have more agents

    public UrAgent(LinkedList<UrPoint> relations, long f, int[] tokensFreqs, long datasize) {
        this(relations, f, tokensFreqs, datasize, 0);

    }

    public UrAgent(LinkedList<UrPoint> relations, long f, int[] tokensFreqs, long datasize, long startpos) {
        this.relations = new LinkedList<>(); this.relations.addAll(relations);
        this.tokens = relations.toString();
        this.f = f;
        this.tokensFreqs = tokensFreqs;
        this.datasize = datasize;
        for (UrPoint point: relations)
            fields.set(point.getPosition());
        this.startpos = startpos;
    }

    public UrAgent(UrPoint relation, long f, int[] tokensFreqs, long datasize) {
        this(relation, f, tokensFreqs, datasize, 0);
    }

    public UrAgent(UrPoint relation, long f, int[] tokensFreqs, long datasize, long startpos) {
        this.relations = new LinkedList<>();
        relations.add(relation);
        this.tokens = relations.toString();
        this.f = f;
        this.tokensFreqs = tokensFreqs;
        this.datasize = datasize;
        fields.set(relation.getPosition());
        this.startpos = startpos;

    }

    public long getStartpos() {
        return startpos;
    }

    public String getTokens() {
        return tokens.isEmpty()? tokens = relations.toString(): tokens;
    }

    public double getP(){
        return ((double) f)/datasize;
    }
    public BitSet getFields() {
        return fields;
    }

    public LinkedList<UrPoint> getRelations() {
        return relations;
    }

    public long getF() {
        return f;
    }

    public void setF(long f) {
        this.f = f;
    }

    public void addPoint(UrPoint point){
        relations.add(point);
        mr = NaN;
        fields.set(point.getPosition());
        tokens = "";
    }

    public double getMr() {
        if (Double.isNaN(mr)){
            double z = f, fr = 1;// records.getCardinality();
            if (relations.size() < 2) return mr = zeroMr;

            int c = 1, l = 0;
            for (UrPoint point: relations) {
                fr = fr * tokensFreqs[point.getToken()];
                l++;
                if (fr > Double.MAX_VALUE / 1000000) { //prevents double value overflowing
                    fr = fr / datasize;
                    c++;
                }
            }
            z = log(z / fr) + (l - c) * log(datasize);
            return mr = z;
        }

        return mr;
    }

    @Override
    public String toString() {
        return relations +"\t"+relations.size() + "\t"+getF() + "\t"+getMr();
    }

    /*    public void setMr(double mr) {
        this.mr = mr;
    }*/

    public long incF(long d){
        return f = f + d;
    }
    public synchronized void addIndex(long index){
        idx.add(index, index+1);
    }

    public RoaringBitmap getIdx() {
        return idx;
    }

    public long getDatasize(){
        return datasize;
    }

}
