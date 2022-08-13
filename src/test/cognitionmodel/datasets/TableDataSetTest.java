package cognitionmodel.datasets;

import cognitionmodel.models.inverted.InvertedTabularModel;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static java.lang.Math.abs;
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

}