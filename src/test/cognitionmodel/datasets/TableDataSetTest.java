package cognitionmodel.datasets;

import cognitionmodel.models.inverted.InvertedTabularModel;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;
import static org.junit.Assert.*;

public class TableDataSetTest {

    @Test
    public void splitTest() throws IOException {
        TableDataSet tableDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\house\\HOUSE_16H.arff")),
                        new ArffParser());

        boolean t = true;
        for (int i = 1; i < 100; i++) {
            TableDataSet[] ts = TableDataSet.split(tableDataSet, 1.0/i);
            t = t & abs(ts[1].size() - tableDataSet.size() * 1.0/i) < 1;
            if (!t) {
                System.err.println(i + "\t"+ts[1].size()+"\t"+tableDataSet.size()*1.0/i);
            }
        }

        assertTrue(t);

    }

    @Test
    public void splitBalancedTest() throws IOException {

        TableDataSet tableDataSet = new TableDataSet(null, null);

        for (int i=0; i<10000; i++)
            tableDataSet.getRecords().add(new Tuple().add((int)floor(random()*10)));

        TableDataSet d[] = TableDataSet.split(tableDataSet, 0.1, 0, 1);

        System.out.println(d[0].size()+"\t"+d[1].size());

        HashMap<String, Integer> fr = new HashMap<>(), frs = new HashMap<>();

        for(Tuple t: d[1]){
            fr.compute(t.get(0).getValue().toString(), (k, v) -> (v == null) ? 1: v + 1);
        }

        for(Tuple t: tableDataSet){
            frs.compute(t.get(0).getValue().toString(), (k, v) -> (v == null) ? 1: v + 1);
        }

        for (Map.Entry<String, Integer> e: fr.entrySet())
            System.out.println(e.getValue()+"\t"+frs.get(e.getKey())+"\t"+(0.1-1.0*e.getValue()/frs.get(e.getKey())));

        TableDataSet arrfDataSet = new TableDataSet(new FileInputStream(new File("E:\\Weka-3-8\\data\\KDDCup99_full.arff")),
                new ArffParser());//false, "duration", "src_bytes", "dst_bytes", "count", "srv_count"));

        TableDataSet[] dataSets = TableDataSet.split(arrfDataSet, 0.1, "label", 100);
        dataSets = TableDataSet.split(dataSets[0], 0.25, "label", 5);

        int fi = arrfDataSet.getFieldIndex("label");
        fr.clear(); frs.clear();

        for(Tuple t: dataSets[1]){
            fr.compute(t.get(fi).getValue().toString(), (k, v) -> (v == null) ? 1: v + 1);
        }

        for(Tuple t: arrfDataSet){
            frs.compute(t.get(fi).getValue().toString(), (k, v) -> (v == null) ? 1: v + 1);
        }

        for (Map.Entry<String, Integer> e: fr.entrySet())
            System.out.println(e.getKey()+"\t"+e.getValue()+"\t"+frs.get(e.getKey())+"\t"+(1.0*e.getValue()/frs.get(e.getKey())));

    }
}