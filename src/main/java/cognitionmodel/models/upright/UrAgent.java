package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.Point;
import org.roaringbitmap.RoaringBitmap;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Double.NaN;
import static java.lang.Math.log;

public class UrAgent{
    private String agentHash = "";
    private LinkedList<UrPoint> points = new LinkedList<>();
    private long f = 0, datasize = 0;
    private double mr = NaN;

    //private HashMap<Object, Long> tokensFreqs;
    private long startpos;
    private RoaringBitmap idx = new RoaringBitmap();

    private BitSet fields = new BitSet();

    private int relation = UrRelation.RELATION_UNDEFINED;

    public static final double zeroMr = 0;// zeroMR gives more MR to the compositions that have more agents

    public UrAgent(List<UrPoint> points, long f,  long datasize) {
        this(points, f, datasize, 0);

    }

    public UrAgent(List<UrPoint> points, long f, long datasize, long startpos) {
        this.points = new LinkedList<>(); this.points.addAll(points);
        this.agentHash = points.toString();
        this.f = f;
       // this.tokensFreqs = tokensFreqs;
        this.datasize = datasize;
        for (UrPoint point: points)
            fields.set(point.getPosition());
        this.startpos = startpos;
        idx.add(startpos, startpos+1);
    }

    public UrAgent(UrPoint point, long f, long datasize) {
        this(point, f, datasize, 0);
    }

    public UrAgent(UrPoint point, RoaringBitmap idx, long datasize) {
        this(point, idx.getLongCardinality(), datasize, 0);
        this.idx = idx;
    }


    public UrAgent(UrPoint point, long f, long datasize, long startpos) {
        this.points = new LinkedList<>();
        points.add(point);
        this.agentHash = points.toString();
        this.f = f;
      // this.tokensFreqs = tokensFreqs;
        this.datasize = datasize;
        fields.set(point.getPosition());
        this.startpos = startpos;
        idx.add(startpos, startpos+1);

    }

    public void setIdx(RoaringBitmap idx) {
        this.idx = idx;
    }

    public long getStartpos() {
        return startpos;
    }

    public String getAgentHash() {
        return agentHash.isEmpty()? agentHash = points.toString(): agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public List<Integer> getTokens(){

        LinkedList<Integer> tokens = new LinkedList<>();

        for (UrPoint point: points) {
            if (point.getToken() instanceof UrAgent)
                tokens.addAll(((UrAgent) point.getToken()).getTokens());
            else
                tokens.add((Integer) point.getToken());
        }

        return tokens;
    }

    public double getP(){
        return ((double) f)/datasize;
    }
    public BitSet getFields() {
        return fields;
    }

    public LinkedList<UrPoint> getPoints() {
        return points;
    }

    public long getF() {
        return f;
    }

    public void setF(long f) {
        this.f = f;
    }

    public void addPoint(UrPoint point){
        points.add(point);
        mr = NaN;
        fields.set(point.getPosition());
        agentHash = "";
    }

    public double getMr() {
        if (Double.isNaN(mr)){
            double z = f, fr = 1;// records.getCardinality();
            if (points.size() < 2) {
                if (points.size() == 1)
                    if (points.getFirst().token instanceof UrAgent)
                        return mr = ((UrAgent) points.getFirst().token).getMr();
                return mr = zeroMr;
            }

            int c = 1, l = 0;
            for (UrPoint point: points) {
                fr = fr * ((UrAgent)point.getToken()).getIdx().getCardinality();//tokensFreqs.get(point.getToken());
                l++;
                if (fr > Double.MAX_VALUE / 200000) { //prevents double value overflowing
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
    public String toString(){
        return points +"\t"+ points.size() + "\t"+getF() + "\t"+getMr();
    }

    public String getInfo(){
        return points.stream().map(p->p.getToken()).collect(Collectors.toList()).toString();
    }

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

    public int getFirstPos(){
        if (points.isEmpty()) return -1;
        return points.getFirst().getPosition();
    }

    public UrAgent clone(){
        UrAgent na = new UrAgent(getPoints(), f,  datasize);
        na.idx = idx;
        return na;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }
}
