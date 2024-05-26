package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.index.Point;
import cognitionmodel.predictors.predictionfunctions.Predictionfunction;
import org.fusesource.jansi.Ansi;

import java.util.*;

import static java.lang.Math.abs;
import static org.fusesource.jansi.Ansi.ansi;

public class Composition implements Cloneable{

    private double p = 1, mr, f, pf;

    private BitSet fields = new BitSet();
    private int predictingIndex;
    private Predictionfunction predictionfunction = null;
    private int length;
    private  HashMap<String, Agent> zeroMap;
    private LinkedList<Agent> agents = new LinkedList<>();
    private BitSet fieldsIndex = new BitSet();

    public Composition(Agent agent, int predictingIndex, Predictionfunction predictionfunction, HashMap<String, Agent> zeroMap){
        this.predictingIndex = predictingIndex;
        this.predictionfunction = predictionfunction;
        this.zeroMap = zeroMap;

        add(agent);
    }
    public Composition(Agent agent, int predictingIndex, HashMap<String, Agent> zeroMap){
        this.predictingIndex = predictingIndex;
        this.zeroMap = zeroMap;
        add(agent);
    }

    public Composition(List<Agent> agents, int predictingIndex, HashMap<String, Agent> zeroMap){
        this.predictingIndex = predictingIndex;
        this.zeroMap = zeroMap;
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
            if (predictingIndex != -1) fields.set(predictingIndex, false);
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
            //f = f + composition.f;
            pf = pf + composition.pf;
            fields.or(composition.fields);
            fieldsIndex.and(composition.fieldsIndex);
            if (predictingIndex != -1) fields.set(predictingIndex, false);
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
       // f = f + agent.getFr();


        if (predictionfunction != null) {
            Agent pva = null;
            if (zeroMap != null) {
                BitSet fs = agent.getFields();
                fs.set(predictingIndex, false);
                pva = zeroMap.get(fs.toString());
            }
            pf = pf + predictionfunction.predictionfunction(agent, pva);
        }
    }

    public double getP() {
        return p;
    }

    public double getMr() {
        return predictionfunction != null? pf: mr;
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
    protected Composition clone() throws CloneNotSupportedException {

        Composition composition = new Composition();

        composition.fields = (BitSet) fields.clone();
        composition.f = f;
        composition.mr = mr;
        composition.p = p;
        composition.pf = pf;
        composition.agents.addAll(agents);
        composition.predictingIndex = predictingIndex;
        composition.length = length;
        composition.predictionfunction = predictionfunction;
        composition.fieldsIndex = BitSet.valueOf(fieldsIndex.toLongArray());

        return composition;
    }

    @Override
    public String toString() {
        return mr+"\t"+fields+"\t"+agents.toString();

    }

    public void setMr(double mr) {
        this.mr = mr;
    }



    public Object getPredictingValue(){
        return agents.getFirst().getPredictingValue();
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

    /**
     * Complements composition with the points that are absent into it. Each absent point is represented by agent.
     * Puts that agents into "null"
     * @param agentList
     * @return - do change and return
     */
    public Composition complementComposition(LinkedList<Agent> agentList){
        for (Agent a: agentList)
            if (a.getRelation().size() == 1) {
                BitSet bitSet = BitSet.valueOf(a.getFields().toLongArray());
                bitSet.and(fields);
                if (bitSet.cardinality() == 0)
                    add(a);
            }
        return this;
    }

}
