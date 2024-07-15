package cognitionmodel.models.upright;


import org.roaringbitmap.RoaringBitmap;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.log;

public class UrComposition implements Cloneable{

    private double p = 1, mr, f, pf, s;

    private BitSet fields = new BitSet();
    private int length;
    private LinkedList<UrAgent> urAgents = new LinkedList<>();
    private BitSet fieldsIndex = new BitSet();
    private RoaringBitmap indexes = new RoaringBitmap();

    public UrComposition(UrAgent UrAgent){
        add(UrAgent);
    }
    public UrComposition(UrAgent UrAgent, int predictingIndex){
        add(UrAgent);
    }

    public UrComposition(List<UrAgent> UrAgents, int predictingIndex, HashMap<String, UrAgent> zeroMap){
        for (UrAgent UrAgent: UrAgents)
            add(UrAgent);
    }

    public UrComposition(){
    }

    public boolean add(UrAgent agent){
        if (!fields.intersects(agent.getFields()))
      //  if (!RoaringBitmap.intersects(indexes, agent.getIdx()))
        {
            urAgents.add(agent);
            fields.or(agent.getFields());
            recalculate(agent);
            length = fields.cardinality();
/*            if (urAgents.size() > 1)
                indexes.and(RoaringBitmap.addOffset(agent.getIdx(), -agent.getRelations().getFirst().getPosition() + urAgents.getFirst().getRelations().getFirst().getPosition()));
            else
                indexes.or(agent.getIdx());*/
            return true;
        }

        return false;
    }

    public boolean add(UrComposition composition){
        if (!fields.intersects(composition.getFields()))
        //if (!RoaringBitmap.intersects(indexes, composition.indexes))
        {
            urAgents.addAll(composition.urAgents);
            mr = mr + composition.mr;
            s = s + composition.s;
            p = p * composition.p;
            //f = f + composition.f;
            pf = pf + composition.pf;
            fields.or(composition.fields);
            fieldsIndex.and(composition.fieldsIndex);
            length = fields.cardinality();
            indexes.and(composition.indexes);
            return true;
        }
        return false;
    }

    public BitSet getFields() {
        return fields;
    }

    public LinkedList<UrAgent> getUrAgents() {
        return urAgents;
    }

    private void recalculate(UrAgent urAgent){
        mr = mr + urAgent.getMr();
        s = s - urAgent.getP()*log(urAgent.getP());
        p = p * urAgent.getP();
    }

    public double getP() {
        return p;
    }

    public double getMr() {
        return mr;
    }
    public double getAbsMr() {
        return abs(mr);
    }

    public double getF() {
        return f;
    }

    public BitSet getFieldsIndex() {
        return fieldsIndex;
    }

    public void setFieldsIndex(BitSet fieldsIndex) {
        this.fieldsIndex = fieldsIndex;
    }

    @Override
    protected UrComposition clone() throws CloneNotSupportedException {

        UrComposition composition = new UrComposition();

        composition.fields = (BitSet) fields.clone();
        composition.f = f;
        composition.mr = mr;
        composition.s = s;
        composition.p = p;
        composition.pf = pf;
        composition.urAgents.addAll(urAgents);
        composition.length = length;
        composition.fieldsIndex = BitSet.valueOf(fieldsIndex.toLongArray());
        composition.indexes = indexes.clone();

        return composition;
    }

    @Override
    public String toString() {
        return mr+"\t"+fields+"\t"+ urAgents.toString();

    }

    /*public void setMr(double mr) {
        this.mr = mr;
    }
*/
    /**
     * Checks whether two compositions can be composed or not
     * @param c1
     * @param c2
     * @return - true in case of they can be composed
     */
    public static boolean check(UrComposition c1, UrComposition c2, int fieldsLength){
        if (c1.length + c2.length >= fieldsLength) return false;
        return !c1.getFields().intersects(c2.fields);
    }

    public double getS(){
/*         double r = 0;
       for (UrAgent a: urAgents){
            r = r + a.getMr()*a.getF()/a.getDatasize();
        }*/
        return s;
    }


}
