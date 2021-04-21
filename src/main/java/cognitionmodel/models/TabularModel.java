package cognitionmodel.models;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.patterns.PatternSet;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.util.Arrays;

public class TabularModel extends Model<LightRelation>{

    private int[] terminalsToField;
    private byte[] enabledFields;


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
     */


    public TabularModel(TableDataSet dataSet, String... enabledFieldsNames) {
        super(dataSet, new LightRelation());
        terminalsToField = new int[LightRelation.getTerminalsArray().size()];

        for (Tuple t: dataSet) {
            int[] sign = relationMethods.makeSignature(t);
            for (int i = 0; i < sign.length; i++) {
                terminalsToField[sign[i]] = i;
            }
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
                .entries(10000000)
                .maxBloatFactor(10)
                .averageKeySize(50)
                .create();
    }

    @Override
    public void addRecordToRelation(int[] signature, int tupleIndex) {

    }

    /**
     *
     * @param terminalIndex
     * @return field index in data set and signature
     */

    public int getFieldIndex(int terminalIndex){
        return terminalsToField[terminalIndex];
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
                if (f.equals(t.get().toString()))
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
        this.patternSet = patternSet;
    }

/*
   public void make(){
        ((Model)this).make();
    }
*/



}
