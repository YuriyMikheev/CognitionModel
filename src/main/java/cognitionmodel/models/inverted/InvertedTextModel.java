package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.composers.Composition;
import cognitionmodel.models.inverted.composers.InvertedComposerFull;
import cognitionmodel.models.inverted.decomposers.IterativeDecomposer;
import cognitionmodel.models.inverted.index.Point;
import cognitionmodel.models.inverted.index.TextIndex;
import org.fusesource.jansi.Ansi;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static org.fusesource.jansi.Ansi.ansi;

public class InvertedTextModel {

    private TableDataSet dataSet;

    private TextIndex textIndex;
    private String indexField;
    private double minMr = -10;
    int depth = Integer.MAX_VALUE, range = Integer.MAX_VALUE, maxComp = 10;

    public InvertedTextModel(TableDataSet dataSet, String indexField, String datasetInfo) {
        this.dataSet = dataSet;
        this.indexField = indexField;
        setTextIndex(new TextIndex(this, indexField, dataSet, datasetInfo));
    }

    public InvertedTextModel(TextIndex textIndex, String indexField, String datasetInfo) {
        this.dataSet = dataSet;
        this.indexField = indexField;
        setTextIndex(textIndex);
    }

    public void setTextIndex(TextIndex textIndex){
        this.textIndex = textIndex;
        getTextIndex().makeShiftedIndexes(1000, -1);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double getMinMr() {
        return minMr;
    }

    public void setMinMr(double minMr) {
        this.minMr = minMr;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getMaxComp() {
        return maxComp;
    }

    public void setMaxComp(int maxComp) {
        this.maxComp = maxComp;
    }

    private int agentFieldsRange(Agent a){
        if (a.getRelation().isEmpty()) return 0;
        BitSet b = a.getFields();
        return -b.nextSetBit(0)+b.length()-1;
    }

    public String generate(String request){
        String answer = "";

        Tuple requestTuple = new Tuple().addAll(textIndex.getEncoder().encode(request));

        IterativeDecomposer decomposer = new IterativeDecomposer(textIndex, "", false, depth, a-> a.getMR() > minMr && agentFieldsRange(a) < range);
      //  IterativeTextDecomposer decomposer = new IterativeTextDecomposer(textIndex, "", false, depth, a-> a.getMR() > minMr); decomposer.setRange(range);
        InvertedComposerFull composer = new InvertedComposerFull(requestTuple.size(), -1, null);
        composer.setMaxN(10000);

        long t = System.currentTimeMillis();
        HashMap<Object, LinkedList<Agent>> d = decomposer.decompose(requestTuple,"");
        t = (System.currentTimeMillis() - t);
        System.out.println(String.format("Decomposer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))));
        System.out.println("Agents: " + d.get("null").size());

        t = System.currentTimeMillis();
        HashMap<Object, List<Composition>> cl = composer.composeToSortedCompositions(d,  c->true);//;c-> c.getFields().cardinality() == requestTuple.size());
        t = (System.currentTimeMillis() - t);
        System.out.println(String.format("Composer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))));
        System.out.println("Compositions: " + cl.get("null").size());

        System.out.println("length "+requestTuple.size());
/*
        for (Agent a: d.get("null").stream().sorted((a1, a2)-> a1.getMR() > a2.getMR()? -1:1).collect(Collectors.toList())){
            String s = "";
            LinkedList<Integer> tl = new LinkedList<>();

            for (Point p: a.getRelation().values().stream().sorted((p1, p2) -> Integer.parseInt(p1.getField().replace(indexField,"")) > Integer.parseInt(p2.getField().replace(indexField,""))?1:-1).collect(Collectors.toList())) {
                tl.add((Integer) p.getValue());
                s = s + p.getField().replace(indexField,"")+": \u001B[33m"+textIndex.getEncoder().decode(tl)+"\u001B[0m;";
                tl.poll();
            }

            s = s + a.getMR();
            System.out.println(s);
        }
*/
        int i = 0;
        if (cl.get("null")!=null)
            for (Composition c: cl.get("null"))
                if (i++ < 10) answer += compositionToColourString(c.complementComposition(d.get("null")), "text")+"; "+c.getAgents().size()+"\n";
                    else break;
        else
            System.err.println("nothing!");

        return answer;
    }

    public TextIndex getTextIndex() {
        return textIndex;
    }

    public TableDataSet getDataSet() {
        return dataSet;
    }


    public String compositionToColourString(Composition composition, String textField){
        String cs = "";

        String[] agentColors = new String[composition.getAgents().size()];
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

        String[] sa = new String[textIndex.getFieldsAmount()];
        Arrays.fill(sa,"");

        int i = 0;
        List<Integer> ll = new LinkedList<>();
        for (Agent a: composition.getAgents()){
            for (Point p: a.getRelation().values()) {
                ll.add((Integer) p.getValue());
                int j = Integer.parseInt(p.getField().substring(textField.length()));
                sa[j] = agentColors[i % 32] + textIndex.getEncoder().decode(ll) + ansi().reset();
                ll.clear();
            }
            i++;
        }

        for (String s: sa)
            cs = cs + s;


        return cs + " ; "+composition.getMr();
    }

}
