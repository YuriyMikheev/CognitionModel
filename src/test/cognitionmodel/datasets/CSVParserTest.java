package cognitionmodel.datasets;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class CSVParserTest {

    @Test
    public void parseTest(){
        CSVParser cp = new CSVParser(",", "\r\n");
        LinkedList<Tuples> tp = (LinkedList<Tuples>) cp.get("values,41,0,0.0,0,0,0,0,0,0,0,0,0,0,0,0".getBytes());

        assertTrue(tp.getFirst().getTuples().get(0).type == Tuple.Type.String);
        assertTrue(tp.getFirst().getTuples().get(3).type == Tuple.Type.Double);
        assertTrue(tp.getFirst().getTuples().get(1).type == Tuple.Type.Int);
        assertTrue(new String(tp.getFirst().getTuples().get(0).data).equals("values"));
        assertTrue(ByteBuffer.allocate(Integer.BYTES).put(tp.getFirst().getTuples().get(1).data).position(0).getInt() == 41);


/*        for(Tuples ts: tp) {
            for (Tuple tuple: ts.getTuples())
                System.out.print(tuple+"\t");
            System.out.println();
        }*/


    }

}