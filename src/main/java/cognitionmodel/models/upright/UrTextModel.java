package cognitionmodel.models.upright;

import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.models.upright.composers.UrTextComposer;
import cognitionmodel.models.upright.composers.UrComposition;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.*;
import static org.fusesource.jansi.Ansi.ansi;

public class UrTextModel {


    private UrTextDataSet dataSet;
    HashMap<Object, Long> tokensFreq;
    public UrTextModel(String tokensFile) throws IOException {
        dataSet = new UrTextDataSet(tokensFile);
        for (int i = 0; i < dataSet.getFreqs().length; i++)
            tokensFreq.put(i, Long.valueOf(dataSet.getFreqs()[i]));
    }
    public UrTextModel(@NotNull UrTextDataSet dataSet){
        this.dataSet = dataSet;
        for (int i = 0; i < dataSet.getFreqs().length; i++)
            tokensFreq.put(i, Long.valueOf(dataSet.getFreqs()[i]));
    }


    public UrTextDataSet getDataSet() {
        return dataSet;
    }


    public String generate(String text, int attentionSize){
        List<Integer> in = (dataSet.getEncoder().encode(text));

        ArrayList<UrAgent> al = new ArrayList<>(makeAgentsList(in, attentionSize));

        UrTextComposer composer = new UrTextComposer(in.size(), in.size());

        List<UrComposition> compositions = composer.composeToSortedList(al);

        String r = "";
        for (int i = 0; i < 10; i++)
            r = r + compositionToColourString(compositions.get(i), in.size())+"; "+compositions.get(i).getUrAgents().size()+"\n";

        return r;
    }


    public List<UrAgent> makeAgentsListWrong(@NotNull List<Integer> in, int attentionSize){
        HashMap<String, UrAgent>  agents = new HashMap<>();

        HashMap<Integer, RoaringBitmap> idx = new HashMap<>();

        for (int i = 0; i < in.size(); i++) {
            if (!idx.containsKey(in.get(i))) idx.put(in.get(i), RoaringBitmap.bitmapOfRange(i, i+1));
                else idx.get(in.get(i)).add(i);
        }

        long doff = 1; int ti = 0;
        LinkedList<UrPoint> points = new LinkedList<>();
        LinkedList<UrPoint> anypoints = new LinkedList<>();

        for (Iterator<Integer> iterator = dataSet.getTextTokens().iterator(); ti < getDataSet().getTextTokens().size() + attentionSize; ti++ ) {

            int token = iterator.next();

            if (idx.containsKey(token)){
                anypoints.add(new UrPoint(ti, token));
            }

            if (!anypoints.isEmpty())
                if (ti - anypoints.getFirst().getPosition() >= attentionSize) {// || (ti - anypoints.getFirst().position < attentionSize && dataSet.getTextTokens().size() - ti < attentionSize)) {
                    UrPoint p1 = anypoints.pop();
                    RoaringBitmap pos = idx.get(p1.getToken());

                    for (int i1 : pos) {
                        if (p1.getPosition() < dataSet.getTextTokens().size()) {
                            points.add(new UrPoint(i1, p1.getToken()));

                            for (Iterator<UrPoint> pointIterator = anypoints.iterator(); pointIterator.hasNext(); ) {
                                UrPoint p2 = pointIterator.next();
                                if (p2.getPosition() - p1.getPosition() < attentionSize && p2.getPosition() < dataSet.getTextTokens().size()) {
                                    RoaringBitmap pos2 = idx.get(p2.getToken());
                                    for (int i2 : pos2)
                                        if (abs(i2 - i1) < attentionSize & i2 > i1 && abs(p1.getPosition() - p2.getPosition()) == abs(i1 - i2))
                                            points.add(new UrPoint(i2, p2.getToken()));
                                }
                            }
                            if (!points.isEmpty()) {
                                LinkedList<UrPoint> al = new LinkedList<>();
                                for (UrPoint p : points) {
                                    al.add(p);
                                    String s = al.toString();
                                    if (!agents.containsKey(s))
                                        agents.put(s, new UrAgent(al, 1, dataSet.getTextTokens().size()));
                                    else
                                        agents.get(s).incF(1);
                                }

/*                               while (al.size() > 1){
                                    al.removeFirst();
                                    String s = al.toString();
                                    if (!agents.containsKey(s))
                                        agents.put(s, new UrAgent(al, 1, dataSet.getFreqs(), dataSet.getTextTokens().size()));
                                    else
                                        agents.get(s).incF(1);
                                }*/
                                points.clear();
                            }
                        }
                    }
                }


        }


        List<UrAgent> al = new ArrayList<>(agents.values());

        al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

        return al;
    }


    public List<UrAgent> makeAgentsList(@NotNull List<Integer> in, int attentionSize){
        HashMap<String, UrAgent>  agents = new HashMap<>();

        HashMap<Integer, RoaringBitmap> idx = new HashMap<>();

        for (int i = 0; i < in.size(); i++) {
            if (!idx.containsKey(in.get(i))) idx.put(in.get(i), RoaringBitmap.bitmapOfRange(i, i+1));
            else idx.get(in.get(i)).add(i);
        }

        int ti = 0;

        HashMap<String, UrAgent>  nagents = new HashMap<>();
        LinkedList<UrAgent> nlist = new LinkedList<>();

        for (Iterator<Integer> iterator = dataSet.getTextTokens().iterator(); ti < dataSet.getTextTokens().size() + attentionSize; ti++ ) {

            int token = iterator.next();

            if (idx.containsKey(token) && ti < dataSet.getTextTokens().size() ){
                RoaringBitmap pos = idx.get(token);
                LinkedList<UrAgent> nnlist = new LinkedList<>();

                for (UrAgent a: nlist){
                    int i1 = a.getPoints().getFirst().getPosition();
                    int i0 = i1;
                    while (i1 != -1 && i1 <= i0 + attentionSize) {
                        if (i1 - a.getPoints().getFirst().getPosition() == ti - a.getStartPos()) {
                            LinkedList<UrPoint> pl = new LinkedList<>(a.getPoints());
                            pl.add(new UrPoint(i1, token));
                            String s = pl.toString();
                            if (!nagents.containsKey(s)) {
                                UrAgent nag = new UrAgent(pl, 1, dataSet.getTextTokens().size(), a.getStartPos());
                                nagents.put(s, nag);
                                nnlist.add(nag);
                            } else
                                incAgentF(nagents.get(s),1);
                        }
                        i1 = (int)pos.nextValue(i1+1);
                    }
                }

                for (int i1 : pos) {
                    UrPoint p1 = new UrPoint(i1, token);
                    String s = "["+p1+"]";
                    UrAgent nag = new UrAgent(p1, 1,  dataSet.getTextTokens().size(), ti);
                    nagents.put(s, nag);
                    nlist.add(nag);
                }
                nlist.addAll(nnlist);
            }

            if (!nlist.isEmpty()) {
                UrAgent nag;
                while (!nlist.isEmpty() && (nag = nlist.getFirst()).getStartPos() + attentionSize <= ti) {
                    nlist.removeFirst();
                    String s = nag.getPoints().toString();
                    nagents.remove(s);
                    if (!agents.containsKey(s))
                        agents.put(s, nag);
                    else
                        incAgentF(agents.get(s), nag.getF());
                }
            }
        }

        List<UrAgent> al = new ArrayList<>(agents.values());

        al.sort((a1,a2) -> a1.getPoints().size() == a2.getPoints().size()? 0: a1.getPoints().size() > a2.getPoints().size()? 1:-1);

        return al;
    }


    private void incAgentF(@NotNull UrAgent agent, long f){
        if (agent.getPoints().size() > 1) agent.incF(f);
    }

    public String compositionToColourString(@NotNull UrComposition composition, int length){
        String cs = "";

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
            for (UrPoint p: a.getPoints()) {
                ll.add((int)p.getToken());
                sa[p.getPosition()] = agentColors[i % 32] + dataSet.getEncoder().decode(ll) + ansi().reset();
                ll.clear();
            }
            i++;
        }

        for (String s: sa)
            cs = cs + s;

        return cs + " ; "+composition.getMr();
    }



}
