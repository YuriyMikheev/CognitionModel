package cognitionmodel.models;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.relations.LightRelation;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.log;

public class InvertedTabularModel1 extends TabularModel {

    private HashMap<String, TreeMap<Object, HashSet<Integer>>> invertedIndex = new HashMap();// = new HashMap<>();
    private TableDataSet dataSet;
    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Agent> agents = new ArrayList<>();
    private HashMap<String, Agent> agentsindex =  new HashMap<>();

    /**
     * Creates TabularModel object with inverted indexes of the dataset
     *
     * @param dataSet    - data for model
     */
/*
    public InvertedTabularModel(TableDataSet dataSet) {
        super(dataSet);
    }
*/

    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     * @param relationInstance - the instance of relation for this model
     */
    public InvertedTabularModel1(TableDataSet dataSet, LightRelation relationInstance, String... enabledFieldsNames){
        super(dataSet, relationInstance, enabledFieldsNames);
        this.dataSet = (TableDataSet) dataSet;
        indexInit();
    }



    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     */
    public InvertedTabularModel1(TableDataSet dataSet, String... enabledFieldsNames) {
        this(dataSet, new LightRelation(), enabledFieldsNames);
    }

    private HashSet<Integer> addToSet(Object set, Integer value){
        ((HashSet<Integer>)set).add(value);
        return ((HashSet<Integer>)set);
    }

    protected void indexInit() {
        //super.setDataSet(dataSet);

       // this.dataSet = (TableDataSet) dataSet;
      //  datapoints = new TreeMap[this.dataSet.getHeader().size()];

        for (int i = 0; i < dataSet.getHeader().size(); i++)
            if (getEnabledFields()[i] == 1)
                invertedIndex.put(dataSet.getHeader().get(i).getValue().toString(), new TreeMap<Object, HashSet<Integer>>());


        int i = 0;
        for (Tuple tuple: dataSet) {
            int j = 0;
            for (TupleElement tupleElement: tuple){
                if (getEnabledFields()[j] == 1) {
                    String fieldName = dataSet.getHeader().get(j).getValue().toString();
                    HashSet<Integer> integers;
                    if (invertedIndex.get(fieldName).containsKey(tupleElement.getValue()))
                        integers = invertedIndex.get(fieldName).get(tupleElement.getValue());
                    else {
                        integers = new HashSet<>();
                        invertedIndex.get(fieldName).put(tupleElement.getValue(), integers);
                    }
                    integers.add(i);
                }
                j++;
            }
            i++;
        }
    }

    public void initPoints(){
        for (String field: invertedIndex.keySet())
            for (Map.Entry<Object, HashSet<Integer>> entry: invertedIndex.get(field).entrySet()) {
                points.add(new Point(field,entry.getKey()));
              //  agents.add(agent);
             //   datapoints[i].put(entry.getKey(), agent);
            }
    }

    public class Dist {
        int i;
        int j;
        double distance;

        public Dist(int i, int j, double distance) {
            this.i = i;
            this.j = j;
            this.distance = distance;
        }
    }

    public void make(){
        LinkedList<Dist> distarr = new LinkedList<>();
        initPoints();

        double[] cardinalities = new double[points.size()];

        for (int i = 0; i < points.size(); i++)
            cardinalities[i] = getBitIndexes(points.get(i)).cardinality();

        for (int i = 0; i < points.size(); i++) {
            BitSet IbitSet =  getBitIndexes(points.get(i));
            double fi = cardinalities[i];

            if (fi >0)
             for (int j = i + 1; j < points.size(); j++) {
                BitSet JbitSet =  getBitIndexes(points.get(j));
                double fj = cardinalities[j];

                if (fj > 0) {
                    JbitSet.and(IbitSet);
                    double f = JbitSet.cardinality();
                    if (f > 0 & fi*fj > dataSet.size())
                   // if (f > 0 )
                        distarr.add(new Dist(i, j, log(f / (fi*fj)) + log(dataSet.size())));
                }
            }
        }

        List<Dist> selecteddistarr = distarr.stream().sorted(Comparator.comparingDouble(d -> -d.distance)).collect(Collectors.toList());
        LinkedList<Agent> newAgents = new LinkedList<>();

        Point jpoint, ipoint;
        for (Dist dist: selecteddistarr)
            if (((ipoint = points.get(dist.i)).agent == null) & ((jpoint = points.get(dist.j)).agent == null)) {
                Agent newAgent = new Agent(ipoint);
                newAgent.addPoint(jpoint);
                if (newAgent.getZ() > 0 & newAgent.isPossible()) {
                    newAgents.add(newAgent);
                    jpoint.agent = ipoint.agent = newAgent;
                }
        }

        double dz = 0, ddz = 0;
        int it = 0;

        while (newAgents.stream().mapToDouble(Agent::getZ).sum() > dz ) {
            dz = newAgents.stream().mapToDouble(Agent::getZ).sum();
            ddz = 0;
            it++;
            for (int i = 0; i < points.size(); i++) {
                Point point = points.get(i);

                double minusZ = 0, maxdz = -100000000;
                if (point.agent != null) {
                    minusZ = point.agent.getdZifRemove(point);
                    if (minusZ < 0) {
                        System.out.print("remove "+point.agent);
                        point.agent.remove(point);
                        System.out.println("\t"+point.agent+"\t"+minusZ);
                        if (point.agent.relation.isEmpty())// | point.agent.recordsByField.values().stream().filter(b -> !b.isEmpty()).count() < 2)
                            newAgents.remove(point.agent);
                        point.agent = null;
                        //if (Double.isFinite(minusZ)) ddz = ddz - minusZ;
                        //minusZ = 0;
                    }
                }


                Agent maxAgent = null;
                for (Agent agent: newAgents)
                    if (!agent.relation.containsKey(point.toString())){
                        double plusZ = agent.getdZifAdd(point);
                        if (plusZ > 0){
                            if (plusZ > maxdz){
                                maxdz = plusZ;
                                maxAgent = agent;
                            }
                        }
                }

                if (maxAgent != null & maxdz > minusZ) {
                    if (point.agent != null) {
                        System.out.print("remove "+point.agent);
                        point.agent.remove(point);
                        System.out.println("\t"+point.agent+"\t"+minusZ);
                        if (point.agent.relation.isEmpty() | !point.agent.isPossible()) {// | point.agent.recordsByField.values().stream().filter(b -> !b.isEmpty()).count() < 2)
                            newAgents.remove(point.agent);
                            for (Map.Entry<String, Point> ep: point.agent.relation.entrySet())
                                ep.getValue().agent = null;
                        }
                    }
                    System.out.print("add "+maxAgent);
                    maxAgent.addPoint(point);
                    point.agent = maxAgent;
                    System.out.println("\t"+maxAgent+"\t"+maxdz);

                    //System.out.println(point.toString()+"\t"+maxdz+"\t"+minusZ);

                    if (Double.isFinite(maxdz)) ddz = ddz + maxdz;
                }

                if (point.agent == null) {
                    Agent agent = new Agent(point);
                    point.agent = agent;

                    double pdz = 0;
                    Point pp = null;
                    for (Point op: points)
                        if (op.agent == null){
                            double tpdz = agent.getdZifAdd(op);
                            if (tpdz > pdz){
                                pp = op;
                                tpdz = pdz;
                            }
                        }
                    if (pp != null) {
                        agent.addPoint(pp);
                        if (agent.isPossible())
                            newAgents.add(agent);
                    }
                }

            }



            System.out.println("---------  "+dz);

        } ;

        System.out.println(newAgents.stream().mapToDouble(Agent::getZ).sum());

        agents.clear();
        agents.addAll(newAgents);

    }


    public void make1(){
        LinkedList<Dist> distarr = new LinkedList<>();
        initPoints();

        List<Dist> selecteddistarr = distarr.stream().sorted(Comparator.comparingDouble(d -> -d.distance)).collect(Collectors.toList());
        LinkedList<Agent> newAgents = new LinkedList<>();

        for (Point point: points) {
            newAgents.add(new Agent(point));
        }
        double dz = -1, ddz;
        int it = 0;

        while ((ddz = newAgents.stream().mapToDouble(Agent::getZ).sum()) > dz ) {
            dz = ddz;
            LinkedList<Agent> addAgents = new LinkedList<>();

            for (Agent a1: newAgents){
                if (a1.iteratiom == it)
                    for (Agent a2: newAgents)
                        if (a1 != a2) {
                            Agent na = merge(a1,a2);
                            if (!agentsindex.containsKey(na.signature) & na.isPossible() & na.getZ() > a1.getZ() + a2.getZ()) {
                                addAgents.add(na);
                                agentsindex.put(na.signature, na);
                                na.iteratiom = it+1;
                            }
                }
            }
            it++;
            newAgents.addAll(addAgents);


            System.out.println("---------  "+dz);

        }

        System.out.println(newAgents.stream().mapToDouble(Agent::getZ).sum());

        agents.clear();
        agents.addAll(newAgents);

    }





    private HashSet<Integer> getIndexes(Point point){
        TreeMap<Object, HashSet<Integer>> pointinvertedindex = invertedIndex.get(point.field);
        return pointinvertedindex.get(point.value);
    }

    private BitSet getBitIndexes(Point point){

        BitSet bitSet = new BitSet();

        for (Integer recordindex: getIndexes(point)) {
            bitSet.set(recordindex);
        }

        return bitSet;
    }

    public ArrayList<Point> getPoints(){

        return points;
    }

    public ArrayList<Agent> getAgents(){

        return agents;
    }

    public class Point{
        String field;
        Object value;
        Agent agent;

        public Point(String field, Object value) {
            this.field = field;
            this.value = value;
        }

        public String toString(){
            return field+":"+value.toString();
        }
    }

    public Agent merge(Agent a1, Agent a2) {
        Agent r = new Agent(null);

        r.mergewith(a1);
        r.mergewith(a2);

        return r;
    }



    /**
     * Class Agent performs Z increasing by changing agent's relation
     */

    public class Agent implements Cloneable {
        HashMap<String, Point> relation = new HashMap<String, Point>();
        HashMap<String, BitSet> recordsByField = new HashMap<String, BitSet>(); // records common for all points from relation
        BitSet records = new BitSet(); // records common for all points from relation
        HashMap<Integer, Integer> orRecords = new HashMap<Integer, Integer>(); // records pointed in any any point from relation
        String signature = "";
        public int iteratiom = 0;

        /**
         * Creates Agent for starting point
         * @param startPoint - {integer field index, object representing field value}
         */

        public Agent(Point startPoint){
            //records.set(0, dataSet.getRecords().size(),false);

            if (startPoint != null) {
                for (Map.Entry<String, TreeMap<Object, HashSet<Integer>>> entry: invertedIndex.entrySet())
                    recordsByField.put(entry.getKey(), new BitSet());
                addPoint(startPoint);
            }
        }

        private void resign(){
            signature = "";
            for (String s: relation.keySet().stream().sorted().collect(Collectors.toList()))
                signature = signature + "\t"+s;
        }

        public BitSet getRecords() {
            return records;
        }

        public String toString(){
            return signature+"\t"+relation.size()+"; "+recordsByField.values().stream().filter(b -> !b.isEmpty()).count()+"; "+getZ();
        }


        /**
         * Adds new point to agent
         * @param point  - new point
         * @return - set of records actual for agent
         */

        public BitSet addPoint(Point point){
            records.clear();
            relation.put(point.toString(), point);

            HashSet<Integer> pointrecords = getIndexes(point);
            BitSet bitSet = recordsByField.get(point.field);

            for (Integer recordindex: pointrecords) {
                bitSet.set(recordindex);
                orRecords.compute(recordindex, (k,v) -> (v == null? 1: v+1));
            }

            int c = (int) recordsByField.values().stream().filter(bs -> !bs.isEmpty()).count();

            for (Map.Entry<Integer, Integer> orKey: orRecords.entrySet())
                if (orKey.getValue() == c) records.set(orKey.getKey());


            resign();
            return records;
        }



        /**
         * @return Cardinal P of agent's relation
         * Cardinal P is proportion of amount of records that common for all points and amount of unique records in all points
         */

        public double getPcardinal(){
            if (relation.size() < 1) return 0;
            if (orRecords.isEmpty()) return 0;

            double p = records.cardinality()/orRecords.size();
            return log(p);
        }

        /**
         * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
         * @return - Z value for the relation
         */

        public double getZ(){

            return getZ(this.records);
        }

        public double getZ(BitSet records){
            if (relation.size() < 1) return 0;
            if (orRecords.isEmpty()) return 0;
            if (records.isEmpty()) return 0;

            double z = records.cardinality(), f = 1;
            int c = 1, l = 0;
            for (BitSet fieldrecords: recordsByField.values())
              if (!fieldrecords.isEmpty()){
                f = f * fieldrecords.cardinality();
                l++;
                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / records.size();
                    c++;
                }
            }

            z = log(z / f) + (l - c) * log(dataSet.size());

            return z;
        }

        private void recalculateRecords(){

            records.clear();

            List<Point> sortedpoints = relation.values().stream().sorted(Comparator.comparing(point -> point.field)).collect(Collectors.toList());

            orRecords.clear();

            BitSet i1 = new BitSet();
            String oldfield = "";
            for (Point point: sortedpoints){
                String field = point.field;
                i1.clear();

                for (Integer recordindex: getIndexes(point)) {
                    i1.set(recordindex);
                    orRecords.compute(recordindex, (k,v) -> (v == null? 1: v+1));
                }

                if (oldfield == "" | field.equals(oldfield)) records.or(i1);
                    else  records.and(i1);

                oldfield = field;
            }
        }

        public void remove(Point point){
            relation.remove(point.toString());

            records.clear();

            BitSet rf = recordsByField.get(point.field);

            for (Integer recordindex: getIndexes(point)) {
                int f = orRecords.compute(recordindex, (k,v) -> (v-1));
                if (f == 0) orRecords.remove(recordindex);
                rf.set(recordindex, false);
            }

            int c = (int) recordsByField.values().stream().filter(bs -> !bs.isEmpty()).count();

            for (Map.Entry<Integer, Integer> orKey: orRecords.entrySet()) {
                if (orKey.getValue() == c) records.set(orKey.getKey());
            }
            resign();
        }

        /**
         * Get dZ if add some point to agent point set
         * @param point - adding point
         * @return value pf Z if add the point
         */

        public double getdZifAdd(Point point){

            HashSet<Integer> pointrecords = getIndexes(point);

            //BitSet r = new BitSet();
            int fc2 = 0, fc1 = records.cardinality(), co = 0, fcs1 = recordsByField.get(point.field).cardinality(), fcs2 = pointrecords.size();

            int c = co = (int) recordsByField.values().stream().filter(bs -> !bs.isEmpty()).count();
            if (recordsByField.get(point.field).isEmpty()) c++;

            for (Integer pidx: pointrecords)
                if (orRecords.containsKey(pidx)) {
                    if (orRecords.get(pidx) + 1 == c) fc2++;
                    //if (orRecords.get(pidx) == co) fc1++;
                }

            if (co == c)
                return log((double) (fc2 + fc1) * fcs1 / (fc1 * (fcs1 + fcs2)));
            else
                return log((double) fc2 * dataSet.size() / (fc1 * fcs2));

        }

        /**
         * Get dZ if remove the point from agent point set
         * @param point - removing point
         * @return value of Z if remove the point
         */


        public double getdZifRemove(Point point){

            HashSet<Integer> pointrecords = getIndexes(point);

          //  BitSet r = getBitIndexes(point);
            int fc2 = 0, fc1 = records.cardinality(), co = 0;

            int c = co = (int) recordsByField.values().stream().filter(bs -> !bs.isEmpty()).count();
            if (relation.values().stream().filter(f -> f.field.contains(point.field)).count() == 1)
                if (c-- == 2) return getZ();


            int fcs1 = recordsByField.get(point.field).cardinality(), fcs2 = pointrecords.size();

            for (Integer pidx: pointrecords)
                if (records.get(pidx)) fc2++;
                  //  if (orRecords.get(pidx) == co) fc1++;



            if (co == c) return -log((double) (fc1 - fc2) * fcs1 / (fc1 * (fcs1 - fcs2)));
                else
                    return log((double) fc2 * dataSet.size() / (/*fc1 **/ fcs2));
        }

        /**
         * Checks if the relation represented by agent is possible
         * Relation is impossibli if sum(log(p(cj)) <= log(1/N), N is number of records in dataset
         * @return true if relation is possible
         */

        public boolean isPossible(){
            if (relation.size() < 1) return false;
            if (orRecords.isEmpty()) return false;
            if (records.isEmpty()) return false;

            double f = 1;
            int c = 1, l = 0;
            for (BitSet fieldrecords: recordsByField.values())
                if (!fieldrecords.isEmpty()){
                    f = f * fieldrecords.cardinality();
                    l++;
                    if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                        f = f / records.size();
                        c++;
                    }
                }

            double ps = log(f) - (l - c) * log(dataSet.size());
            return ps >= 0;
        }

        public BitSet mergewith(Agent agent){

            records.clear();
            for (Point point: agent.relation.values())
                if (!relation.containsKey(point.field))
                {

                    relation.put(point.toString(), point);

                    HashSet<Integer> pointrecords = getIndexes(point);
                    BitSet bitSet = recordsByField.get(point.field);

                    for (Integer recordindex : pointrecords) {
                        bitSet.set(recordindex);
                        orRecords.compute(recordindex, (k, v) -> (v == null ? 1 : v + 1));
                    }
                }

            int c = (int) recordsByField.values().stream().filter(bs -> !bs.isEmpty()).count();

            for (Map.Entry<Integer, Integer> orKey: orRecords.entrySet())
                if (orKey.getValue() == c) records.set(orKey.getKey());


            resign();
            return records;
        }


    }


}
