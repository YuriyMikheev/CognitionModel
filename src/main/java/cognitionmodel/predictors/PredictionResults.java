package cognitionmodel.predictors;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.Model;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.util.ArrayList;

/**
 * Consists results of prediction in map
 */
public class PredictionResults {

    private ChronicleMap<int[],Tuple> datamap;

    private Tuple header;

    public PredictionResults(Tuple header){
        this.header = header;

        datamap = ChronicleMapBuilder.of(int[].class, Tuple.class)
                .name("predictionsMap")
                .entries(1000000)
                .maxBloatFactor(10)
                .averageKeySize(50)
                .create();
    }

    /**
     * Get data form map
     * @param recordIndex - record number in data set
     * @param elementIndex - index of data, for example field number in dataset
     * @return - saved data
     */


    public Tuple get(int recordIndex, int elementIndex){
        return datamap.get(new int[]{recordIndex, elementIndex});
    }

    /**
     * Puts data to  map
     * @param recordIndex - record number in data set
     * @param elementIndex - index of data, for example field number in dataset
     * @param data - saving data
     */

    public void Put(int recordIndex, int elementIndex, Tuple data){
        datamap.put(new int[]{recordIndex, elementIndex}, data);
    }

    public Tuple getHeader() {
        return header;
    }
}
