package cognitionmodel.models;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.predictors.PredictionResults;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.NaN;
import static java.lang.Math.*;

public class InvertedTabularModel2 extends TabularModel {

    private HashMap<String, TreeMap<Object, BitSet>> invertedIndex = new HashMap();// = new HashMap<>();
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
    public InvertedTabularModel2(TableDataSet dataSet, LightRelation relationInstance, String... enabledFieldsNames){
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
    public InvertedTabularModel2(TableDataSet dataSet, String... enabledFieldsNames) {
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
                invertedIndex.put(dataSet.getHeader().get(i).getValue().toString(), new TreeMap<Object, BitSet>());


        int i = 0;
        for (Tuple tuple: dataSet) {
            int j = 0;
            for (TupleElement tupleElement: tuple){
                if (getEnabledFields()[j] == 1) {
                    String fieldName = dataSet.getHeader().get(j).getValue().toString();
                    BitSet idx;
                    if (invertedIndex.get(fieldName).containsKey(tupleElement.getValue()))
                        idx = invertedIndex.get(fieldName).get(tupleElement.getValue());
                    else {
                        idx = new BitSet();
                        invertedIndex.get(fieldName).put(tupleElement.getValue(), idx);
                    }
                    idx.set(i);
                }
                j++;
            }
            i++;
        }
    }

    public void initPoints(){
        for (String field: invertedIndex.keySet())
            for (Map.Entry<Object, BitSet> entry: invertedIndex.get(field).entrySet()) {
                points.add(new Point(field,entry.getKey()));
              //  agents.add(agent);
             //   datapoints[i].put(entry.getKey(), agent);
            }
    }

    public void initPoints(Tuple tuple, String predictingfield){
        int j = 0;
        for (TupleElement tupleElement: tuple) {
            if (getEnabledFields()[j] == 1) {
                String field = dataSet.getHeader().get(j).getValue().toString();
                if (!field.equals(predictingfield))
                    points.add(new Point(field, tupleElement.getValue()));
            }
            j++;
        }
    }

    private boolean canMerge(Agent a1, Agent a2){
        BitSet b = new BitSet();

        b.or(a1.fields);
        b.and(a2.fields);

        if (b.isEmpty())
            if (log(a1.getP()*a2.getP()*dataSet.size()) < (a1.getZ()+a2.getZ()))
                return false;

        return b.isEmpty();
    }

    public static double gamma = 0; //
    public static double epsilon = 0.00; //probability of confidential interval
    public static double tau =  0.9; // maximal conditional probability
    public static int d = 3; //max depth

    public void make(){
        predict(null, null);
    }

    public PredictionResults predict(List<Tuple> records, String predictingfield){

        int si = 0;
        for (TupleElement t:dataSet.getHeader())
            if (t.getValue().toString().equals(predictingfield)) break;
            else si++;

        if (si == dataSet.getHeader().size() | !getDataSet().getHeader().get(si).getValue().toString().equals(predictingfield)) {
            throw new IllegalArgumentException(predictingfield + " is not found in model data set");
        }

        int[] altTerminals = termsByField(si);
        String[] altTermNames = new String[altTerminals.length];

        for (int i = 0; i < altTerminals.length; i++)
            altTermNames[i] = getRelationMethods().getTerminalsArray().get(altTerminals[i]);

        PredictionResults r = new PredictionResults();
        r.addPredictedDataHeader(si,new Tuple().add(dataSet.getHeader().get(si).getValue()+" Predicted").add(dataSet.getHeader().get(si).getValue()+" From data").addAll(altTermNames));

        int recordIndex = 0;

        for (Tuple record: records)
         if (record.size() > si){

            points.clear();
            agentsindex.clear();
            if (record == null)
                initPoints();
            else
                initPoints(record, predictingfield);

            for (Map.Entry<Object, BitSet> entry : invertedIndex.get(predictingfield).entrySet())
                points.add(new Point(predictingfield, entry.getKey()));

            LinkedList<Object> predictingvalues = new LinkedList<>();
            predictingvalues.addAll(invertedIndex.get(predictingfield).keySet());

            LinkedList<Agent> newAgents = new LinkedList<>();

            for (Point point : points) {
                TreeMap<Object, BitSet> tr = invertedIndex.get(point.field);
                Agent na = new Agent(point), nr = null;
                if (tr.size() * epsilon > 1) {
                    Map.Entry<Object, BitSet> a = tr.ceilingEntry(point.value);
                    Map.Entry<Object, BitSet> b = tr.floorEntry(point.value);
                    while ((na.getConfP() > 1 - epsilon) & (a != null | b != null)) {
                        if (a != null) a = tr.lowerEntry(a.getKey());
                        if (b != null) b = tr.higherEntry(b.getKey());
                        if (a != null) {
                            na.addPoint(new Point(point.field, a.getKey()));
                        }
                        if (b != null) {
                            na.addPoint(new Point(point.field, b.getKey()));
                        }
                        if (na.getConfP() > 1 - epsilon)
                            nr = na;
                    }
                    if (nr != null)
                        if (!agentsindex.containsKey(nr.signature)) {
                            newAgents.add(nr);
                            agentsindex.put(nr.signature, nr);
                        }
                } else
                {
                    newAgents.add(na);
                    agentsindex.put(na.signature, na);
                }
            }
            double dz , ddz = -1;
            int it = 0, cn = 0;

            /*
                1. для значения predictingfield создаем  список значений других полей упорядоченный по веротяности возникновения со значением predictingfield
                2. просматриваем все ноды из списка нод
                    2.1 копируем ноду, добавляем новый агент с элементом, помещаем копию ноды в список нод
                    2.2 перебираем все агенты ноды, копируем ноду, копируем агента, добавляем элемент к копи ноды к агенту, помещаем копию ноды в список нод
                3  помещаем элемент в новую ноду, помещаем ноду в список нод
                4. берем следующий элемент повторяем с п. 2
                5. повторяем для следующего значения predictingfield с п.1
                6. сортируем ноды по количеству агентов
                7. находим ноду с наибольшим количеством агентов и суммарной условной вероятностью при условии возникновения значения  predictingfield равной вероятности этого значения
             */




            do {
                dz = ddz;
                cn = 0;
                LinkedList<Agent> addAgents = new LinkedList<>();

                for (Agent a1 : newAgents) {
                    if (a1.iteration == it & a1.relationByField.get(predictingfield).size() > 0 & a1.fields.cardinality() < d & (a1.relation.size() == 1 | a1.getCondP(predictingfield) < tau))// & a1.getCondP(predictingfield) > 1 - tau )
                      for (Agent a2 : newAgents)
                        if (a1 != a2 & a2.relationByField.get(predictingfield).size() == 0 & canMerge(a1, a2) & a2.fields.cardinality() < d & (a2.relation.size() == 1 | a2.getCondP(predictingfield) < tau)){// & a2.getCondP(predictingfield) > 1 - tau ) {
                            Agent na = merge(a1, a2);
                            if (!agentsindex.containsKey(na.signature) & (na.getZ() >= (a1.getZ() + a2.getZ()) * (1 + gamma)) & na.getConfP() >= 1 - epsilon) {
                                addAgents.add(na);
                                agentsindex.put(na.signature, na);
                                na.iteration = it + 1; cn++;
                            }
                        }
                }
                it++;
                newAgents.addAll(addAgents); //newAgents.sort(Comparator.comparing(a -> -a.iteration));

                //  System.out.println("---------  "+dz);
           // } while ((ddz = newAgents.stream().mapToDouble(Agent::getZ).sum()) > dz);//(cn != 0);
            } while (cn != 0);

            //System.out.println(newAgents.stream().mapToDouble(Agent::getZ).sum());

            agents.clear();
            agents.addAll(newAgents);

            double[] pa = new double[predictingvalues.size()];
            double[] pc = new double[predictingvalues.size()];
            int c[] = new int[predictingvalues.size()];

            for (Agent a : agents) {
                int i = 0;
                if (a.relation.size() > 1)// & a.iteration == it - 1)
                 for (Object v : predictingvalues) {
                    if (a.relation.containsKey(predictingfield + ":" + v)) {
                       //pa[i] += ( pow(a.getCondP(predictingfield), 15) * pow(a.getZ(),1));
                        //pa[i] += a.getZ();
                        pa[i] += 1/(a.getCondP(predictingfield));
                        pc[i] += (a.getConfP());
                        c[i]++;
                    }
                    i++;
                }
            }

           // System.out.print(predictingfield + "\t " + record.get(14).getValue() + "\t");

            int mi = 0;
            double[] pr = new double[c.length];
            for (int i = 0; i < c.length; i++)
                if ((pr[i] = pa[i]  ) > pa[mi] )
                    mi = i;

            r.put(recordIndex, si, new Tuple().add((c[mi] == 0? "Prediction failed": predictingvalues.get(mi))).add(record.get(si).getValue()).addAll(pr));

            recordIndex++;
            //System.out.printf("%s\t%04f\t p <= %04f", predictingvalues.get(mi), pa[mi] / c[mi], 1 - pc[mi]);

            //System.out.println();
        }

        return r;

    }





    private BitSet getIndexes(Point point){
        TreeMap<Object, BitSet> pointinvertedindex = invertedIndex.get(point.field);
        return pointinvertedindex.get(point.value);
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

        for (Point p: a1.relation.values()) {
            r.relation.put(p.toString(), p);
            r.relationByField.get(p.field).add(p.toString());
        }

        for (Point p: a2.relation.values()) {
            r.relation.put(p.toString(), p);
            r.relationByField.get(p.field).add(p.toString());
        }

        for (String f: invertedIndex.keySet()) {
            BitSet bs = r.recordsByField.get(f);
            bs.or(a1.recordsByField.get(f));
            bs.or(a2.recordsByField.get(f));
        }

        r.fields.or(a1.fields);
        r.fields.or(a2.fields);

        int i=0;
        for (BitSet bs: (r.recordsByField.values()))
            if (!bs.isEmpty())
                if (i++ == 0) r.records.or(bs);
                else
                    r.records.and(bs);

        r.dZ = r.getZ() - a1.getZ() - a2.getZ();

        r.resign();
        return r;
    }



    /**
     * Class Agent performs Z increasing by changing agent's relation
     */

    public class Agent implements Cloneable {
        HashMap<String, Point> relation = new HashMap<String, Point>();
        HashMap<String, HashSet<String>> relationByField = new HashMap<String, HashSet<String>>();
        HashMap<String, BitSet> recordsByField = new HashMap<String, BitSet>(); // records common for all points from relation
        BitSet records = new BitSet(); // records common for all points from relation
        HashMap<Integer, Integer> orRecords = new HashMap<Integer, Integer>(); // records pointed in any any point from relation
        String signature = "";
        public int iteration = 0;
        private double z = NaN, p = NaN, cp = NaN;
        public BitSet fields = new BitSet();
        public double dZ = NaN;

        /**
         * Creates Agent for starting point
         * @param startPoint - {integer field index, object representing field value}
         */

        public Agent(Point startPoint){
            for (Map.Entry<String, TreeMap<Object, BitSet>> entry: invertedIndex.entrySet()) {
                recordsByField.put(entry.getKey(), new BitSet());
                relationByField.put(entry.getKey(), new HashSet<>());
            }
            if (startPoint != null) {
                addPoint(startPoint);
            }
        }

        private void resign(){
            signature = relation.keySet().stream().sorted().collect(Collectors.joining("\t"));
/*            for (String s: relation.keySet().stream().sorted().collect(Collectors.toList()))
                signature = signature + "\t"+s;*/
            z = NaN; p = NaN; cp = NaN;
           // getZ(records);

        }

        public BitSet getRecords() {
            return records;
        }

        public Agent clone(){
            Agent c = new Agent(null);
            c.mergewith(this);
            return c;
        }


        public String toString(){
            return signature+"\t"+relation.size()+"; "+recordsByField.values().stream().filter(b -> !b.isEmpty()).count()+"; "+getZ();
        }

        /**
         * Gets confidential probability of the agents subspace that equal production of all confidential intervals probabilities included in agent
         * @return - confidential probability of the agent
         */

        public double getConfP(){
            double p = 0, f = 0 ;

            if(!Double.isNaN(cp)) return cp;

            for (Map.Entry<String, BitSet> e: recordsByField.entrySet()) {
                BitSet b = e.getValue();
                if (relationByField.get(e.getKey()).size() > 1) //& invertedIndex.get(e.getKey()).size()*epsilon > 1)
                    p = max(p, ((double) b.cardinality() / b.size()));
            }

            return cp = 1 - (p == 0?0:p);
        }

        /**
         * Gets probability of the relation
         * @return
         */
        public double getP(){
            return (Double.isNaN(p)?(p=(double)records.cardinality()/dataSet.size()): p);
        }

        /**
         * Gets conditional probability of field value if agent relation was appeared
         * @param field
         * @return
         */

        public double getCondP(String field){

            getP();

            BitSet rc  = new BitSet();

            rc.set(0, (int) round(dataSet.size()), true);

            for (Map.Entry<String, BitSet> e: recordsByField.entrySet()) {
                if (!field.equals(e.getKey()) & !e.getValue().isEmpty()) {
                    rc.and(e.getValue());
                }
            }

            return (double) records.cardinality() / rc.cardinality();
        }


        /**
         * Adds new point to agent
         * @param point  - new point
         * @return - set of records actual for agent
         */

        public BitSet addPoint(Point point){
            records.set(0, (int) round(dataSet.size()), true);
            relation.put(point.toString(), point);
            relationByField.get(point.field).add(point.toString());

            if (getIndexes(point) != null) {
                recordsByField.get(point.field).or(getIndexes(point));

                int i = 0;
                for (BitSet b: recordsByField.values()) {
                    if (!b.isEmpty()) {
                        if (records != b)
                            records.and(b);
                        fields.set(i, true);
                    }
                    i++;
                }
                resign();
            }
            return records;
        }



         /**
         * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
         * @return - Z value for the relation
         */

        public double getZ(){
            if (Double.isNaN(z))
                return z = getZ(this.records);
            else return z;
        }

        public double getZ(BitSet records){
            if (relation.size() < 1) return 0;
            if (records.isEmpty()) return 0;
            if (recordsByField.size() < 2) return 0;

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

        /**
         * Checks if the relation represented by agent is possible
         * Relation is impossibli if sum(log(p(cj)) <= log(1/N), N is number of records in dataset
         * @return true if relation is possible
         */

        public boolean isPossible(){
            if (relation.size() < 1) return false;
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

            records.set(0, (int) round(dataSet.size()), true);
            fields.or(agent.fields);
            for (Point point: agent.relation.values())
                if (!relation.containsKey(point.field))
                {

                    relation.put(point.toString(), point);
                    relationByField.get(point.field).add(point.toString());

                    if (getIndexes(point) != null) {
                        recordsByField.get(point.field).or(getIndexes(point));

                        for (BitSet b: recordsByField.values())
                            if (!b.isEmpty() & records != b)
                                records.and(b);
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
