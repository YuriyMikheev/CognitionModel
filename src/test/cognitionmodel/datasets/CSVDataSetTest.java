package cognitionmodel.datasets;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class CSVDataSetTest {

    @Test
    public void CSVtest() throws IOException {
        TableDataSet csvDataSet = new TableDataSet( new ByteArrayInputStream("Name,Age,Temperature\r\nJhon, 20, 35.5, \"\"".getBytes()),new CSVParser(",","\r\n"));

       // System.out.println(csvDataSet.getHeader().getTuples().get(0).toString());

        assertTrue(csvDataSet.getHeader().getTupleElements().get(0).toString().equals("Tuple{data=Name, type=String}"));
        assertTrue(csvDataSet.getRecords().get(0).getTupleElements().get(0).type == TupleElement.Type.String);
        assertTrue(csvDataSet.getRecords().get(0).getTupleElements().get(1).type == TupleElement.Type.Int);
        assertTrue(csvDataSet.getRecords().get(0).getTupleElements().get(2).type == TupleElement.Type.Double);


/*

        for(Tuples ts: csvDataSet.getRecords()) {
            for (Tuple tuple: ts.getTuples())
                System.out.print(tuple+"\t");
            System.out.println();
        }
*/


    }

}