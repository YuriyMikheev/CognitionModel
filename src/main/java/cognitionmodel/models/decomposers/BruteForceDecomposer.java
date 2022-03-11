package cognitionmodel.models.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.Point;

import java.util.*;

public class BruteForceDecomposer implements Decomposer{

    private InvertedTabularModel model;

    public BruteForceDecomposer(InvertedTabularModel model){
        this.model = model;
    }


    public static double gamma = 000000000.0; //
    public static double epsilon = 0.00; //probability of confidential interval
    public static double tau =  0.9999; // maximal conditional probability
    public static int d = 3; //max depth
    public static int minFreq = 1; //minimal frequency to decade that we have enough data

    @Override
    public LinkedList<Agent> decompose(Tuple record, String predictingfield) {

        if (record == null) return null;

        LinkedList<Agent> newAgents = new LinkedList<>();

        LinkedList<Point> points = new LinkedList<>();

        int j = 0;
        for (TupleElement tupleElement: record) {
            if (model.getEnabledFields()[j] == 1) {
                String field = model.getDataSet().getHeader().get(j).getValue().toString();
                if (!field.equals(predictingfield))
                    points.add(new Point(field, tupleElement.getValue()));
            }
            j++;
        }

        for (Map.Entry<Object, BitSet> entry : model.invertedIndex.get(predictingfield).entrySet())
            points.add(new Point(predictingfield, entry.getKey()));

        //LinkedList<Agent> agents = new LinkedList<>();


        for (Point point : points) {
            TreeMap<Object, BitSet> tr = model.invertedIndex.get(point.getField());
            Agent na = new Agent(point, model), nr = null;
            if (tr.size() * epsilon > 1) {
                Map.Entry<Object, BitSet> a = tr.ceilingEntry(point.getValue());
                Map.Entry<Object, BitSet> b = tr.floorEntry(point.getValue());
                while ((na.getConfP() > 1 - epsilon) & (a != null | b != null)) {
                    if (a != null) a = tr.lowerEntry(a.getKey());
                    if (b != null) b = tr.higherEntry(b.getKey());
                    if (a != null) {
                        na.addPoint(new Point(point.getField(), a.getKey()));
                    }
                    if (b != null) {
                        na.addPoint(new Point(point.getField(), b.getKey()));
                    }
                    if (na.getConfP() > 1 - epsilon)
                        nr = na;
                }
                if (nr != null)
                    if (!model.agentsindex.containsKey(nr.getSignature())) {
                        newAgents.add(nr);
                        model.agentsindex.put(nr.getSignature(), nr);
                    }
            } else
            {
                newAgents.add(na);
                model.agentsindex.put(na.getSignature(), na);
            }
        }
        double dz , ddz = -1;
        int it = newAgents.size(), cn = 0;

        HashMap<String, Agent> addagentindex = new HashMap<>();

        do {
            dz = ddz;
            cn = 0;
            LinkedList<Agent> addAgents = new LinkedList<>();

            for (Iterator<Agent> agentIterator = newAgents.descendingIterator(); agentIterator.hasNext() & (it--) > 0; ) {
                Agent a1 = agentIterator.next();
                if (a1.getP() > 1.0 / model.getDataSet().size())
                    if (a1.relationByField.get(predictingfield).size() > 0 & a1.getLength() < d & (a1.relation.size() == 1 | a1.getCondP(predictingfield) < tau))// & a1.getCondP(predictingfield) > 1 - tau )
                        for (Agent a2 : newAgents)
                            if (a2.getP() > 1.0 / model.getDataSet().size())
                                if (a1 != a2 & a2.relationByField.get(predictingfield).size() == 0 & model.canMerge(a1, a2) & a2.getLength() < d & (a2.relation.size() == 1 | a2.getCondP(predictingfield) < tau)){// & a2.getCondP(predictingfield) > 1 - tau ) {
                                    Agent na = model.merge(a1, a2);
                                    if (!addagentindex.containsKey(na.getSignature())  & (na.getZ() >= (a1.getZ() + a2.getZ()) * (1 + gamma)) & na.getConfP() >= 1 - epsilon) {
                                        addAgents.add(na);
                                        if (!model.agentsindex.containsKey(na.getSignature())) model.agentsindex.put(na.getSignature(), na);
                                        addagentindex.put(na.getSignature(), na);
                                        cn++;
                                    }
                                }
            }
            it = cn;
            newAgents.addAll(addAgents); //newAgents.sort(Comparator.comparing(a -> -a.iteration));

        } while (cn != 0);

        return newAgents;
    }
}