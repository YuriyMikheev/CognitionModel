package cognitionmodel.models;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.TabularParser;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.PatternSet;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.util.Arrays;

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
    public void setMaps() {
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
                .maxBloatFactor(10)
                .averageKeySize(50)
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

    public byte[] getEnabledFields() {
        return enabledFields;
    }

    /**
     *
     * @param patternSet
     */

    public void setPatternSet(PatternSet patternSet) {
        patternSet.singleClean();
        this.patternSet = patternSet;
    }

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
