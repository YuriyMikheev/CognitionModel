package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import cognitionmodel.models.inverted.index.TextIndex;

import java.io.IOException;
import java.util.*;

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

    public LinkedList<Integer> newTokens(List<UrAgent> agents, int attentionSize){
        //Integer maxToken = (Integer) textIndex.getIdx(textIndex.getTextField()).lastKey();
        int[] maxIdx = new int[attentionSize];

        LinkedList<Integer> r = new LinkedList<>();

        long lastIdx = agents.stream().mapToLong(a-> a.getRelations().getLast().getPosition()).max().getAsLong();

        double s = textIndex.size();
        for (int i = 0; i < attentionSize; i++) {
            HashMap<Integer, Double> newIdx = new HashMap<>();
            for (UrAgent agent : agents) {
                BatchedIterator iterator = new BatchedIterator(agent.getIdx());
                int agentLast = agent.getRelations().getLast().getPosition();
                while (iterator.hasNext()){
                    long idx = iterator.next() + lastIdx - agentLast + i + 1;
                    double d = dp(lastIdx - agentLast + i + 1, agent, dataSet.getFreqs()[dataSet.getTextTokens().get(idx)])*s;
                    newIdx.compute(dataSet.getTextTokens().get(idx), (k,v) -> (v == null? d : v + d));
                }
            }
            maxIdx[i] = newIdx.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).orElseThrow().getKey();
            r.add(maxIdx[i]);
        }

        return r;
    }


    //сила влияния агента на это месте
    private double dp(long distance, UrAgent agent, int freq){
        return (agent.getMr()/(distance))/freq;
    }


}
