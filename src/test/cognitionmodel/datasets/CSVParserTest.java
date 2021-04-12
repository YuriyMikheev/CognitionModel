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

        assertTrue(tp.getFirst().getTupleElements().get(0).type == TupleElement.Type.String);
        assertTrue(tp.getFirst().getTupleElements().get(3).type == TupleElement.Type.Double);
        assertTrue(tp.getFirst().getTupleElements().get(1).type == TupleElement.Type.Int);
        assertTrue(new String(tp.getFirst().getTupleElements().get(0).data).equals("values"));
        assertTrue(ByteBuffer.allocate(Integer.BYTES).put(tp.getFirst().getTupleElements().get(1).data).position(0).getInt() == 41);
        assertTrue(tp.getFirst().getTupleElements().get(16).type == TupleElement.Type.Char);


/*        for(Tuple ts: tp) {
            for (TupleElement tupleElement: ts)
                System.out.print(tupleElement+"\t");
            System.out.println();
        }*/


    }

}