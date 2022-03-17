package cognitionmodel.models.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;
import cognitionmodel.models.inverted.Point;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class MonteCarloDecomposer extends BasicDecomposer {

    private int mostProbablePointsAmount = 3; //amount of most probable points found for generating new relations
    private Random random = new Random();

    public MonteCarloDecomposer(InvertedTabularModel model) {
        super(model);
    }

    public MonteCarloDecomposer(InvertedTabularModel model, double gamma, double epsilon, double tau, int d, int minFreq) {
        super(model, gamma, epsilon, tau, d, minFreq);
    }

    public LinkedList<Agent> doDecompose(LinkedList<Agent> agents, String predictingfield) {

        /*
            1. инициализируем точки (+интервалы + инвертированные?)
            2. создаем надор агентов помещаем в него агентов для каждой точки predicinigfield
            3. берем агента из набора и считаем для него K наиблоее вероятных точек
            4. создаем агентов на базе взятого агента и всех комбинаций найденных точек если для новых агентов соблюдены граничные условия
            5. если были созданые новые агенты повтояем с п 3.
         */

        LinkedList<Agent> dagents = new LinkedList<>();

        for (Iterator<Agent> agentIterator = agents.descendingIterator(); agentIterator.hasNext(); ) {
            Agent a = agentIterator.next();
            if (!a.relationByField.get(predictingfield).isEmpty()) {
                dagents.add(a);
                agentIterator.remove();
            }
        }

        int cn = 0;
        do {
            LinkedList<Agent> newAgents = new LinkedList<>();

            for (Agent da: dagents){
                newAgents.add(da.clone());
                for (Point p: mostProbablePoints(da)){

                }


            }

            dagents = newAgents;
        } while (cn != 0);

        return agents;
    }

    private LinkedList<Point> mostProbablePoints(Agent agent){
        LinkedList<Point> r = new LinkedList<>();

        for (int i = 0; i < mostProbablePointsAmount; i++){
            //int ri = agent.getRecords().random.nextInt(agent.getLength());

        }


        return r;
    }
}