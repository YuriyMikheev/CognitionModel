package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import cognitionmodel.models.inverted.index.TextIndex;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.log;

/***
 * Generates new tokens basing on index and input list of UrAgents
 */

public class UrGenerator {

    private TextIndex textIndex;
    private UprightTextDataSet dataSet;


    public UrGenerator(TextIndex textIndex, String tokensFile) throws IOException {
        this.textIndex = textIndex;
        dataSet = new UprightTextDataSet(tokensFile);
    }

    public TextIndex getTextIndex() {
        return textIndex;
    }

    public UprightTextDataSet getDataSet() {
        return dataSet;
    }

    public List<UrPoint> newTokens(List<UrAgent> agents, int attentionSize, int variants){
        //Integer maxToken = (Integer) textIndex.getIdx(textIndex.getTextField()).lastKey();
        List<Integer>[] maxIdx = new List[attentionSize];

        LinkedList<UrPoint> r = new LinkedList<>();

        long lastIdx = agents.stream().mapToLong(a-> a.getRelations().getLast().getPosition()).max().getAsLong();

        double s = dataSet.getTextTokens().size();
        for (int i = 0; i < attentionSize; i++) {
            HashMap<Integer, Double> newIdx = new HashMap<>();
            for (UrAgent agent : agents)
                if (!agent.getIdx().isEmpty()){
                    HashMap<Integer, Double> newAIdx = new HashMap<>();
                    BatchedIterator iterator = new BatchedIterator(agent.getIdx());
                    int agentLast = agent.getRelations().getLast().getPosition();
                    while (iterator.hasNext()){
                        long idx = iterator.next() + lastIdx - agentLast + i + 1;
                        if (idx < dataSet.getTextTokens().size()) {
                            int newToken = dataSet.getTextTokens().get(idx);
                            newAIdx.compute(newToken, (k, v) -> (v == null ? 1 : v + 1));
                        }
                    }
                    newAIdx.entrySet().forEach(e -> newIdx.compute(e.getKey(), (k,v)->{
//                        double d = agent.getMr() + log(e.getValue()/agent.getF()) - log(agent.getP())-log(dataSet.getFreqs()[k]/s);
                        double d = log(e.getValue()/agent.getF()) - log(dataSet.getFreqs()[k]/s);
                        return v == null ? d : v + d;
                    }));

                }
            maxIdx[i] = newIdx.entrySet().stream().sorted((e1, e2) -> e1.getValue() < e2.getValue()? 1: e1.getValue() > e2.getValue()? -1:0).limit(variants).map(e->e.getKey()).collect(Collectors.toList());
            //r.add(new UrPoint(i+(int)lastIdx, maxIdx[i].get(0)));
            int finalI = i;
            r.addAll(maxIdx[i].stream().map(e-> new UrPoint(finalI +(int)lastIdx, e)).collect(Collectors.toList()));
        }

        return r;
    }


    //сила влияния агента на это месте
    private double dp(long distance, UrAgent agent, double lp){
        return (agent.getMr())/(agent.getF()*distance) + lp;
    }


}
