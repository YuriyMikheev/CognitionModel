package cognitionmodel.models.inverted;


import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.inverted.*;

import java.util.*;

import static java.lang.Math.log;

public class BasicDecomposer {

    private InvertedTabularModel model;

    public BasicDecomposer(InvertedTabularModel model) {
        this.model = model;
    }

    public BasicDecomposer(InvertedTabularModel model, double gamma, double epsilon, double tau, double mrdelta, int d, int minFreq) {
        this.model = model;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.tau = tau;
        this.d = d;
        this.minFreq = minFreq;
        this.mrdelta = mrdelta;
    }

    private double gamma = 000000000.0; //
    private double mrdelta = -Double.MAX_VALUE;
    private double epsilon = 0.00; //probability of confidential interval
    private double tau = 0.9999; // maximal conditional probability
    private int d = 5; //max depth
    private int minFreq = 1; //minimal frequency to decade that we have enough data

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public double getTau() {
        return tau;
    }

    public void setTau(double tau) {
        this.tau = tau;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public int getMinFreq() {
        return minFreq;
    }

    public void setMinFreq(int minFreq) {
        this.minFreq = minFreq;
    }

    private LinkedList<Agent> initAgents(Tuple record, String predictingfield) {
        if (record == null) return null;

        LinkedList<Agent> newAgents = new LinkedList<>();

        LinkedList<Point> points = new LinkedList<>();

        int j = 0;
        for (TupleElement tupleElement : record) {
            if (model.getEnabledFields()[j] == 1) {
                String field = model.getDataSet().getHeader().get(j).getValue().toString();
                if (!field.equals(predictingfield))
                    points.add(new Point(field, tupleElement.getValue()));
            }
            j++;
        }

        for (Object value : model.invertedIndex.getAllValues(predictingfield))
            points.add(new Point(predictingfield, value));

        for (Point point : points) {
            TreeMap tr = model.invertedIndex.getMap(point.getField());
            Agent na = new Agent(point, model), nr = null; na.setPerdictingField(na.relationByField.containsKey(predictingfield));
            if (tr.size() * epsilon > 1) {
                Map.Entry a = tr.ceilingEntry(point.getValue());
                Map.Entry b = tr.floorEntry(point.getValue());
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
/*                if (nr != null)
                    if (!model.agentsindex.containsKey(nr.getSignature())) {
                        newAgents.add(nr);
                        model.agentsindex.put(nr.getSignature(), nr);
                    }*/
            } else {
                newAgents.add(na);
           // model.agentsindex.put(na.getSignature(), na);
            }
        }

        return newAgents;
    }


    private boolean canMerge(Agent a1, Agent a2){
        if (a1.records == null | a2.records == null) return false;
        BitSet b = new BitSet();


        b.or(a1.fields);
        b.and(a2.fields);

/*        if (b.isEmpty())
            if (log(a1.getP()*a2.getP()*model.getDataSet().size()) < (a1.getMR()+a2.getMR()))
                return false;*/

        return b.isEmpty();
    }


    private LinkedList<Agent> doDecompose(LinkedList<Agent> agents, String predictingfield) {
        int it = agents.size(), cn = 0;

        HashMap<String, Agent> addagentindex = new HashMap<>();

        do {
            cn = 0;
            LinkedList<Agent> addAgents = new LinkedList<>();

            for (Iterator<Agent> agentIterator = agents.descendingIterator(); agentIterator.hasNext() & (it--) > 0; ) {
                Agent a1 = agentIterator.next();
                //if (a1.getP() > 1.0 / model.getDataSet().size())
                    if (a1.hasPerdictingField() & a1.getLength() < d)// & (a1.relation.size() == 1 | a1.getCondP(predictingfield) < tau))// & a1.getCondP(predictingfield) > 1 - tau )
                        for (Agent a2 : agents)
                         if (a1.relation.size() + a2.relation.size() <= d){
                            if (a1.relation.size() + a2.relation.size() > model.getIndexes().getEntrySet().size()) break;
                            //if (a2.getP() > 1.0 / model.getDataSet().size())
                             if (a1 != a2 & !a2.hasPerdictingField() & canMerge(a1, a2) & a2.getLength() < d) {// & (a2.relation.size() == 1 | a2.getCondP(predictingfield) < tau)) {// & a2.getCondP(predictingfield) > 1 - tau ) {
                                Agent na = Agent.merge(a1, a2, model);
                                na.setPerdictingField(a1.hasPerdictingField() | a2.hasPerdictingField());
                                if (!addagentindex.containsKey(na.getSignature()) & (na.getMR() >= (a1.getMR() + a2.getMR()) * (1 + gamma) + mrdelta) & na.getConfP() >= 1 - epsilon) {
                                    addAgents.add(na);
/*                                    if (!model.agentsindex.containsKey(na.getSignature()))
                                        model.agentsindex.put(na.getSignature(), na);*/
                                    addagentindex.put(na.getSignature(), na);
                                    cn++;
                                }
                            }
                        }
            }
            it = cn;
            agents.addAll(addAgents);

        } while (cn != 0);
        //System.out.println(agents.size());

        return agents;
    }


    public LinkedList<Agent> decompose(Tuple record, String predictingfield) {
        return doDecompose(initAgents(record, predictingfield), predictingfield);
    }
}