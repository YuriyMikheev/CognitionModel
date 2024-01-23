package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.composers.Composition;
import cognitionmodel.models.inverted.composers.InvertedComposer;
import cognitionmodel.models.inverted.decomposers.IterativeDecomposer;
import cognitionmodel.models.inverted.index.Point;
import cognitionmodel.models.inverted.index.TextIndex;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class InvertedTextModel {

    private TableDataSet dataSet;

    private TextIndex textIndex;
    private String indexField;


    public InvertedTextModel(TableDataSet dataSet, String indexField, String datasetInfo) {
        this.dataSet = dataSet;
        this.indexField = indexField;
        textIndex = new TextIndex(this, indexField, dataSet, datasetInfo);
    }

    public String generate(String request){
        String answer = "";

        Tuple requestTuple = new Tuple().addAll(textIndex.getEncoder().encode(request));
        getTextIndex().makeShiftedIndexes(requestTuple.size(), -1);

        IterativeDecomposer decomposer = new IterativeDecomposer(textIndex, "", false, requestTuple.size(),null);
        InvertedComposer composer = new InvertedComposer(requestTuple.size(), -1, null);

        long t = System.currentTimeMillis();
        HashMap<Object, LinkedList<Agent>> d = decomposer.decompose(requestTuple,"");
        HashMap<Object, List<Composition>> cl = composer.composeToSortedCompositions(d, c-> c.getFields().cardinality() == requestTuple.size());
        System.out.println(System.currentTimeMillis() - t);

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

        for (Composition c: cl.get("null"))
            System.out.println(compositionToColourString(c, "text"));

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
                sa[j] = agentColors[i] + textIndex.getEncoder().decode(ll) + ansi().fgDefault();
                ll.clear();
            }
            i++;
        }

        for (String s: sa)
            cs = cs + s;


        return cs + " ; "+composition.getMr();
    }

}
