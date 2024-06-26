package cognitionmodel.models.upright;


import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

public class UrCorComposition implements Cloneable{

    private double p = 1, mr, f, pf, s;

    private BitSet fields = new BitSet();
    private int length;
    private LinkedList<UrAgent> urAgents = new LinkedList<>();
    private BitSet fieldsIndex = new BitSet();

    public UrCorComposition(UrAgent urAgent){
        add(urAgent);
    }
    public UrCorComposition(UrAgent urAgent, int predictingIndex){
        add(urAgent);
    }

    public UrCorComposition(List<UrAgent> UrAgents, int predictingIndex, HashMap<String, UrAgent> zeroMap){
        for (UrAgent agent: UrAgents)
            add(agent);
    }

    public UrCorComposition(){
    }

    public boolean add(UrAgent UrAgent){
        if (!fields.intersects(UrAgent.getFields())) {
            urAgents.add(UrAgent);
            fields.or(UrAgent.getFields());
            recalculate(UrAgent);
            length = fields.cardinality();
            return true;
        }

        return false;
    }

    public boolean add(UrCorComposition composition){
        if (!fields.intersects(composition.getFields())) {
            urAgents.addAll(composition.urAgents);
            mr = mr + composition.mr;
            s = s + composition.s;
            p = p * composition.p;
            //f = f + composition.f;
            pf = pf + composition.pf;
            fields.or(composition.fields);
            fieldsIndex.and(composition.fieldsIndex);
            length = fields.cardinality();
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
        s = s +  urAgent.getMr()*urAgent.getF()/urAgent.getDatasize();
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
    protected UrCorComposition clone() throws CloneNotSupportedException {

        UrCorComposition composition = new UrCorComposition();

        composition.fields = (BitSet) fields.clone();
        composition.f = f;
        composition.mr = mr;
        composition.s = s;
        composition.p = p;
        composition.pf = pf;
        composition.urAgents.addAll(urAgents);
        composition.length = length;
        composition.fieldsIndex = BitSet.valueOf(fieldsIndex.toLongArray());

        return composition;
    }

    @Override
    public String toString() {
        return mr+"\t"+fields+"\t"+ urAgents.toString();

    }

    public void setMr(double mr) {
        this.mr = mr;
    }

    /**
     * Checks whether two compositions can be composed or not
     * @param c1
     * @param c2
     * @return - true in case of they can be composed
     */
    public static boolean check(UrCorComposition c1, UrCorComposition c2, int fieldsLength){
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
