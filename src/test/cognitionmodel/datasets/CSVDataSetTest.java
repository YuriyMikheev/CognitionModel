package cognitionmodel.datasets;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class CSVDataSetTest {

    @Test
    public void CSVtest() throws IOException {
        CSVDataSet csvDataSet = new CSVDataSet( new ByteArrayInputStream("Name,Age,Temperature\r\nJhon, 20, 35.5, \"\"".getBytes()),new CSVParser(",","\r\n"));

       // System.out.println(csvDataSet.getHeader().getTuples().get(0).toString());

        assertTrue(csvDataSet.getHeader().getTuples().get(0).toString().equals("Tuple{data=Name, type=String}"));
        assertTrue(csvDataSet.getRecords().get(0).getTuples().get(0).type == Tuple.Type.String);
        assertTrue(csvDataSet.getRecords().get(0).getTuples().get(1).type == Tuple.Type.Int);
        assertTrue(csvDataSet.getRecords().get(0).getTuples().get(2).type == Tuple.Type.Double);


/*

        for(Tuples ts: csvDataSet.getRecords()) {
            for (Tuple tuple: ts.getTuples())
                System.out.print(tuple+"\t");
            System.out.println();
        }
*/


    }

}