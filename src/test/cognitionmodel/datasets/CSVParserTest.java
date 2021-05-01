package cognitionmodel.datasets;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class CSVParserTest {

    @Test
    public void parseTest(){
        CSVParser cp = new CSVParser(",", "\r\n");
        LinkedList<Tuple> tp = (LinkedList<Tuple>) cp.get("values,41,0,0.0,0,0,0,0,0,0,0,0,0,0,0,0,y".getBytes());

        assertTrue(cp.getHeader().get(0).type == TupleElement.Type.String);
        assertTrue(cp.getHeader().get(3).type == TupleElement.Type.Double);
        assertTrue(cp.getHeader().get(1).type == TupleElement.Type.Int);
        assertTrue(new String(cp.getHeader().get(0).data.toString()).equals("values"));
        assertTrue(new String(cp.getHeader().get(1).data.toString()).equals("41"));
        assertTrue(cp.getHeader().get(16).type == TupleElement.Type.Char);


/*        for(Tuple ts: tp) {
            for (TupleElement tupleElement: ts)
                System.out.print(tupleElement+"\t");
            System.out.println();
        }*/


    }

}