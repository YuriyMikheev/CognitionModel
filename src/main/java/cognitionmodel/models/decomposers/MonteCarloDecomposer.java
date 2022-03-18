package cognitionmodel.models.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.Model;
import cognitionmodel.models.inverted.BitAgent;
import cognitionmodel.models.inverted.InvertedBitTabularModel;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.Point;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class MonteCarloDecomposer implements Decomposer {

    private int mostProbablePointsAmount = 3; //amount of most probable points found for generating new relations
    private Random random = new Random();
    private InvertedTabularModel model;

    public MonteCarloDecomposer(InvertedTabularModel model) {
        this.model = model;
    }

    public MonteCarloDecomposer(InvertedTabularModel model, double gamma, double epsilon, double tau, int d, int minFreq) {
        this.model = model;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.tau = tau;
        this.d = d;
        this.minFreq = minFreq;
    }

    public double gamma = 000000000.0; //
    public double epsilon = 0.00; //probability of confidential interval
    public double tau =  0.9999; // maximal conditional probability
    public int d = 3; //max depth
    public int minFreq = 1; //minimal frequency to decade that we have enough data


    public LinkedList<BitAgent> doDecompose(LinkedList<BitAgent> agents, String predictingfield) {

        /*
            1. инициализируем точки (+интервалы + инвертированные?)
            2. создаем надор агентов помещаем в него агентов для каждой точки predicinigfield
            3. берем агента из набора и считаем для него K наиблоее вероятных точек
            4. создаем агентов на базе взятого агента и всех комбинаций найденных точек если для новых агентов соблюдены граничные условия
            5. если были созданые новые агенты повтояем с п 3.
         */

        LinkedList<BitAgent> dagents = new LinkedList<>();

        for (Iterator<BitAgent> agentIterator = agents.descendingIterator(); agentIterator.hasNext(); ) {
            BitAgent a = agentIterator.next();
            if (!a.relationByField.get(predictingfield).isEmpty()) {
                dagents.add(a);
                agentIterator.remove();
            }
        }

        int cn = 0;
        do {
            LinkedList<BitAgent> newAgents = new LinkedList<>();

            for (BitAgent da: dagents){
                newAgents.add(da.clone());
                for (Point p: mostProbablePoints(da)){

                }


            }

            dagents = newAgents;
        } while (cn != 0);

        return agents;
    }

    private LinkedList<Point> mostProbablePoints(BitAgent agent){
        LinkedList<Point> r = new LinkedList<>();

        for (int i = 0; i < mostProbablePointsAmount; i++){
            //int ri = agent.getRecords().random.nextInt(agent.getLength());

        }


        return r;
    }

    @Override
    public LinkedList<BitAgent> decompose(Tuple record, String predictingfield) {
        return null;
    }
}