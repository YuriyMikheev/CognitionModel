package cognitionmodel.models.inverted.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.Point;
import cognitionmodel.patterns.LinkedPatternSet;
import cognitionmodel.patterns.Pattern;

import java.util.*;
import java.util.function.Function;

public class DeductiveDecomposer implements Decomposer {

    private InvertedTabularModel model;
    private String fields[];
    private String predicttingField;
    private int predictingFieldIndex;
    private HashMap<String, Agent> agentMap = new HashMap<>();
    private Function<Agent, Boolean> agentFilter;
    private  int nVariations, maxDepth, minFreq;

    public DeductiveDecomposer(InvertedTabularModel model, String predictingField, Function<Agent, Boolean> agentFilter) {
        this(model, predictingField, agentFilter, 1, 1, 1);
    }

    public DeductiveDecomposer(InvertedTabularModel model, String predictingField, Function<Agent, Boolean> agentFilter, int maxDepth, int nVariations, int minFreq){

        this.model = model;
        this.predicttingField = predictingField;
        predictingFieldIndex = model.getDataSet().getFieldIndex(predictingField);
        this.agentFilter = agentFilter;
        this.maxDepth = maxDepth;
        this.minFreq = minFreq;
        this.nVariations = nVariations;
    }

    @Override
    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield) {

        HashMap<Object, LinkedList<Agent>> r = new HashMap<>();
        HashMap<String, Point> pl = new HashMap<>();

        int j = 0;
        for (TupleElement tupleElement : record) {
            if (model.getEnabledFields()[j] == 1 & j != predictingFieldIndex) {
                String field = model.getDataSet().getHeader().get(j).getValue().toString();
                pl.put(field, new Point(field, tupleElement.getValue()));
            }
            j++;
        }

        Agent agent = new Agent(pl.values(), model.getInvertedIndex());

        for (Object pv: model.getInvertedIndex().getAllValues(predictingfield)) {
            LinkedList<Agent> al = new LinkedList<>();
            Agent a = Agent.merge(agent, new Agent(new Point(predictingfield, pv), model.getInvertedIndex()), model.getInvertedIndex());
            r.put(pv, al);

            if (a.getFr() < minFreq)
                deduct(al, predictingfield, pv, a, 0);
            else
               al.add(a);
        }

        return r;
    }

    Random random = new Random();

    private void deduct(LinkedList<Agent>  resultList, String predictingField, Object pv, Agent agent, int depth){

        if (depth > maxDepth) return;
        ArrayList<Point> pl = new ArrayList<>(agent.getRelation().values());

        for (int j = 0; j < nVariations; j++) {
            int i = 0;
            do {
                i = random.nextInt(pl.size());
                if (!pl.get(i).getField().equals(predictingField)) {
                    pl.remove(i);
                    break;
                }
            } while (true);

            Agent na = new Agent(pl, model.getInvertedIndex());

            if (na.getFr() < minFreq)
                deduct(resultList, predictingField, pv, na, depth + 1);
            else
                resultList.add(na);
        }
    }

}
