package cognitionmodel.models;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.TabularParser;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.models.relations.LightRelation;
import cognitionmodel.patterns.PatternSet;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.util.Arrays;

import static java.lang.Math.log;
import static java.lang.Math.pow;

public class TabularModel extends Model<LightRelation>{

    private byte[] enabledFields;
    private int[][] termToFiled;


    /**
     * Creates TabularModel object
     *
     * @param dataSet    - data for model
     */

    public TabularModel(TableDataSet dataSet) {
        this(dataSet, null);
    }

    /**
     * Creates TabularModel object and sets fields from dataset are enabled for usage
     *
     * @param enabledFieldsNames - array of enabled fields names
     * @param dataSet    - data for model
     * @param relationInstance - the instance of relation for this model
     */


    public TabularModel(TableDataSet dataSet, LightRelation relationInstance, String... enabledFieldsNames) {
        super();

        setRelationMethods(relationInstance);
        setDataSet(dataSet);

        termToFiled = new int[dataSet.getHeader().size()][];

        TabularParser parser = (TabularParser) dataSet.getParser();

        for (int i = 0; i < dataSet.getHeader().size(); i++) {
            String[] terms = parser.terminals(i);
            termToFiled[i] = new int[terms.length];

            for (int j = 0; j < terms.length; j++)
                termToFiled[i][j] = relationInstance.getTerminalIndex(terms[j]);
        }

        if (enabledFieldsNames != null)
            if (enabledFieldsNames.length == 0) enabledFieldsNames = null;

        if (enabledFieldsNames == null) {
            this.enabledFields = new byte[dataSet.getHeader().size()];
            Arrays.fill(this.enabledFields, (byte) 1);
        }
        else {
            this.enabledFields = new byte[dataSet.getHeader().size()];
            setEnabledFields(enabledFieldsNames);
        }

    }

        /**
         * Creates TabularModel object and sets fields from dataset are enabled for usage
         *
         * @param enabledFieldsNames - array of enabled fields names
         * @param dataSet    - data for model
         */


    public TabularModel(TableDataSet dataSet, String... enabledFieldsNames) {
        this(dataSet, new LightRelation(), enabledFieldsNames);
    }


    @Override
    protected void setMaps() {
 /*       relationsMap = ChronicleMapBuilder.of(int[].class, LightRelation.class)
                .name("relationsMap")
                .entries(1000000)
                .maxBloatFactor(10)
                .averageKeySize(50)
                .create();
*/
        frequencyMap = ChronicleMapBuilder.of(int[].class, Integer.class)
                .name("frequencyMap")
                .entries(100000000)
                .maxBloatFactor(20.0)
                .averageKeySize(5)
                .create();
    }

    @Override
    public void addRecordToRelation(int[] signature, int tupleIndex) {

    }

    /**
     * Sets fields that are enabled in model making process
     * @param fields names of fields from table data header
     */

    private void setEnabledFields(String... fields){

        int i = 0;
        for (TupleElement t: (((TableDataSet)getDataSet()).getHeader())){
            enabledFields[i] = 0;

            for (String f: fields){
                if (f.equals(t.getValue().toString()))
                    enabledFields[i] = 1;
            }
            i++;
        }

    }


    /**
     * Calculates Z measure for dependent values = ln(P(relation)/production of all Pj) + ln(1 - P(relation)/production of all (1 - Pj)), P(relation) - probability of relation,  Pj - probability of value j
     *
     * @param signature - relation signature from map
     * @return - Z value for the relation
     */

    public double getZd(int[] signature){

        Integer zf = frequencyMap.get(signature);
        if (zf == null) return 0;

        double z = zf, f = 1, fi = 1;
        int c = 1, l = 0, ci = 1;


        for (int i = 0; i < signature.length; i++)
            if (signature[i] != 0){
                l++;
                f = f * termsfrequencies.get(i + ":" + signature[i]);// * termsfrequencies.get(i + ":" + signature[i]);
                fi = fi * (getDataSet().size() - termsfrequencies.get(i + ":" + signature[i]));

                if (f > Double.MAX_VALUE/1000000) { //prevents double value overloading
                    f = f / getDataSet().size();
                    c++;
                }
            }

        z = f * (log(z / f) + (l - c) * log(getDataSet().size()))/(pow(getDataSet().size(), l - c));// + log((getDataSet().size() - z) / fi) + (l - ci) * log(getDataSet().size());

        return z;

    }

    public byte[] getEnabledFields() {
        return enabledFields;
    }

    /**
     * Puts set of patterns into the model
     * @param patternSet
     */

    public void setPatternSet(PatternSet patternSet) {
        patternSet.singleClean();
        this.patternSet = patternSet;
    }

/*
    */
/**
     * Gets set of patterns
     * @return
     *//*


    public PatternSet getPatternSet(){
        return patternSet;
    }
*/


    @Override
    public TableDataSet getDataSet(){
        return (TableDataSet) dataSet;
    }


    public void close(){
        ((ChronicleMap) frequencyMap).close();
        if (relationsMap != null) ((ChronicleMap) relationsMap).close();

   }

    public int[] termsByField(int fieldIndex){
        return termToFiled[fieldIndex];
    }

}
