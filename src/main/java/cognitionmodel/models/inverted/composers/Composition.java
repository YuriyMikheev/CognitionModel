package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

public class Composition implements Cloneable{

    private double p, mr, f;

    private BitSet fields = new BitSet();
    private int predictingIndex;
    private int length;

    private LinkedList<Agent> agents = new LinkedList<>();

    public Composition(Agent agent, int predictingIndex){
        this.predictingIndex = predictingIndex;
        add(agent);
    }

    public Composition(List<Agent> agents, int predictingIndex){
        this.predictingIndex = predictingIndex;
        for (Agent agent: agents)
            add(agent);
    }

    public Composition(){
    }

    public boolean add(Agent agent){
        if (!fields.intersects(agent.getFields())) {
            agents.add(agent);
            fields.or(agent.getFields());
            recalculate(agent);
            fields.set(predictingIndex, false);
            length = fields.cardinality();
            return true;
        }

        return false;
    }

    public boolean add(Composition composition){
        if (!fields.intersects(composition.getFields())) {
            agents.addAll(composition.agents);
            mr = mr + composition.mr;
            p = p * composition.p;
            f = f + composition.f;
            fields.or(composition.fields);
            fields.set(predictingIndex, false);
            length = fields.cardinality();
            return true;
        }
        return false;
    }

    public BitSet getFields() {
        return fields;
    }

    public LinkedList<Agent> getAgents() {
        return agents;
    }

    private void recalculate(Agent agent){
        mr = mr + agent.getMR();
        p = p * agent.getP();
        f = f + agent.getFr();
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

    @Override
    protected Composition clone() throws CloneNotSupportedException {

        Composition composition = new Composition();

        composition.fields = (BitSet) fields.clone();
        composition.f = f;
        composition.mr = mr;
        composition.p = p;
        composition.agents.addAll(agents);
        composition.predictingIndex = predictingIndex;
        composition.length = length;

        return composition;
    }

    @Override
    public String toString() {
        return mr+"\t"+fields+"\t"+agents.toString();

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
    public static boolean check(Composition c1, Composition c2, int fieldsLength){
        if (c1.length + c2.length >= fieldsLength) return false;
        return !c1.getFields().intersects(c2.fields);
    }
}
