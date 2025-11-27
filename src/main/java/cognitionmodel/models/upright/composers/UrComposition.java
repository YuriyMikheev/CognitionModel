package cognitionmodel.models.upright.composers;


import cognitionmodel.models.inverted.index.TextIndex;
import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static org.fusesource.jansi.Ansi.ansi;

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

    public UrComposition(@NotNull List<UrAgent> UrAgents, int predictingIndex, HashMap<String, UrAgent> zeroMap){
        for (UrAgent UrAgent: UrAgents)
            add(UrAgent);
    }

    public UrComposition(){
    }

    public boolean add(@NotNull UrAgent agent){
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

    public boolean add(@NotNull UrComposition composition){
        //if (!fields.intersects(composition.getFields()))
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
        //return false;
    }

    public BitSet getFields() {
        return fields;
    }

    public LinkedList<UrAgent> getUrAgents() {
        return urAgents;
    }

    private void recalculate(@NotNull UrAgent urAgent){
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
    public static boolean check(@NotNull UrComposition c1, @NotNull UrComposition c2, int fieldsLength){
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
    public static String compositionToColourString(@NotNull UrComposition composition){
        String cs = "";
        int length = composition.getUrAgents().stream().mapToInt(a-> a.getPointList().stream().mapToInt(p->((UrPoint)p).getPosition()).max().getAsInt()).max().getAsInt()+1;

        String[] agentColors = new String[composition.getUrAgents().size()];
        for (int i = 0; i < agentColors.length; i++) {
            if (i < Ansi.Color.values().length - 1)
                agentColors[i] = ansi().fg(Ansi.Color.values()[i % 8]).toString();
            else if (i < (Ansi.Color.values().length - 1) * 2)
                agentColors[i] = ansi().fgBright(Ansi.Color.values()[i % 8]).toString();
            else if (i < (Ansi.Color.values().length - 1) * 3)
                agentColors[i] = ansi().bg(Ansi.Color.values()[i % 8]).toString();
            else if (i < (Ansi.Color.values().length - 1) * 4)
                agentColors[i] = ansi().bgBright(Ansi.Color.values()[i % 8]).toString();
        }

        String[] sa = new String[length];
        Arrays.fill(sa,"");

        int i = 0;
        List<Integer> ll = new LinkedList<>();
        for (UrAgent a: composition.getUrAgents()){
            for (UrPoint p: a.getPointList()) {
                if (p.getToken() instanceof UrAgent)
                    //ll.addAll(((UrAgent) p.getToken()).getPointList().stream().map(pt -> (Integer)pt.getToken()).collect(Collectors.toList()));
                    ll.add((int)((UrAgent)p.getToken()).getPoints().getFirst().getToken());
                else
                    ll.add((int)p.getToken());
                sa[p.getPosition()] = agentColors[i % 32] + TextIndex.getEncoder().decode(ll) + ansi().reset();
                ll.clear();
            }
            i++;
        }

        for (String s: sa)
            cs = cs + s;

        return composition.getMr()+" \t " + composition.getP()*composition.getMr()+" \t " + composition.getS()+" \t "+ cs;
    }


}
