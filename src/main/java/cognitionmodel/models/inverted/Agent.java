package cognitionmodel.models.inverted;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Double.NaN;
import static java.lang.Math.log;
import static java.lang.Math.round;

public class Agent {
    public HashMap<String, Point> relation = new HashMap<String, Point>();
    public HashMap<String, HashSet<String>> relationByField = new HashMap<String, HashSet<String>>();
    HashMap<String, HashSet<Integer>> recordsByField = new HashMap<String, HashSet<Integer>>(); // records common for all points from relation
    HashSet<Integer> records = new HashSet<Integer>(); // records common for all points from relation
    String signature = "";
    private double z = NaN, p = NaN, cp = NaN;
    public BitSet fields = new BitSet();

    private InvertedTabularModel model;

    public Agent(InvertedTabularModel model, Point point){
        this.model = model;
        if (point != null)
            addPoint(point);
    }

    public void addPoint(Point point){
        if (model.getIndexes(point) == null) return;
        relation.put(point.toString(), point);
        relationByField.get(point.field).add(point.toString());

        or(point.getField(), model.getIndexes(point));

        records.clear();

        String mf = ""; int mc = Integer.MAX_VALUE, fi = 0;
        for (String f: model.invertedIndex.keySet()) {
            if (recordsByField.get(f).size() < mc){
                mc = recordsByField.get(f).size();
                mf = f;
            }
            if (!recordsByField.get(f).isEmpty())
                fields.set(fi++, true);
        }

        for (Integer i: recordsByField.get(mf))
            records.add(i);

        for (String f: model.invertedIndex.keySet())
            if (!f.equals(mf))
                and(recordsByField.get(f));

        resign();

    }

    /**
     * Gets probability of the relation
     * @return
     */
    public double getP(){
        return (Double.isNaN(p)?(p=(double)records.size()/model.getDataSet().size()): p);
    }

    /**
     * Gets conditional probability of field value if agent relation was appeared
     * @param field
     * @return
     */

    public double getCondP(String field){

        getP();

        HashSet<Integer> rc  = new HashSet<>();

        for (Map.Entry<String, HashSet<Integer>> e: recordsByField.entrySet()) {
            if (!field.equals(e.getKey()) & !e.getValue().isEmpty()) {
                if (rc.isEmpty()) rc.addAll(e.getValue().stream().collect(Collectors.toList()));
                    else
                        if (e.getValue().size() < records.size()){
                            for (int i:e.getValue())
                                if (records.contains(i))
                                    rc.add(i);
                        } else {
                            for (int i:records)
                                if (e.getValue().contains(i))
                                    rc.add(i);
                        }
            }
        }

        return (double) records.size()/rc.size();
    }

    public String toString(){
        return signature+"\t"+relation.size()+"; "+recordsByField.values().stream().filter(b -> !b.isEmpty()).count()+"; "+getMR();
    }


    /**
     * Gets confidential probability of the agents subspace that equal production of all confidential intervals probabilities included in agent
     * @return - confidential probability of the agent
     */

    public double getConfP(){
        double p = 1, f = 0 ;

        if(!Double.isNaN(cp)) return cp;

        for (Map.Entry<String, HashSet<Integer>> e: recordsByField.entrySet()) {
            HashSet<Integer> b = e.getValue();
            if (relationByField.get(e.getKey()).size() > 1) //& invertedIndex.get(e.getKey()).size()*epsilon > 1)
                p = p * (1+((double) b.size() / model.getDataSet().size()));
        }

        return cp = 1 - (p == 0?0: p - 1);
    }


    /**
     * Calculates Z measure = ln(P(relation)/production of all Pj), P(relation) - probability of relation,  Pj - probability of value j
     * @return - Z value for the relation
     */

    public double getMR(){
        if (Double.isNaN(z))
            return z = getMR(this.records);
        else return z;
    }

    public double getMR(HashSet<Integer> records){
        if (relation.size() < 1) return 0;
        if (records.isEmpty()) return 0;
        if (recordsByField.size() < 2) return 0;

        double z = records.size(), f = 1;
        int c = 1, l = 0;
        for (HashSet<Integer> fieldrecords: recordsByField.values())
            if (!fieldrecords.isEmpty()){
                f = f * fieldrecords.size();
                l++;
                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / records.size();
                    c++;
                }
            }

        z = log(z / f) + (l - c) * log(model.getDataSet().size());

        return z;
    }


    protected void resign(){
        signature = relation.keySet().stream().sorted().collect(Collectors.joining("\t"));
        z = NaN; p = NaN; cp = NaN;
    }


    public void or(String field, HashSet<Integer> recordIndices){
        for (Integer i: recordIndices) {
            recordsByField.get(field).add(i);
            records.add(i);
        }
    }

    public void and(HashSet<Integer> recordIndices){
        HashSet<Integer> nr = new HashSet<>();
        if (recordIndices.size() < records.size()){
            for (int i:recordIndices)
                if (records.contains(i))
                    nr.add(i);
        } else {
            for (int i:records)
                if (recordIndices.contains(i))
                    nr.add(i);
        }

        records = nr;
    }


}
