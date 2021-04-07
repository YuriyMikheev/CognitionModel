package cognitionmodel.datasets;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class CSVParserTest {

    @Test
    public void parseTest(){
        CSVParser cp = new CSVParser(",", "\r\n");
        LinkedList<Tuples> tp = (LinkedList<Tuples>) cp.get("41,0,0,0,0,0,0,0,0,0,0,0,0,0,0".getBytes());

        assertTrue(tp.getFirst().getTuples().get(0).type == Tuple.Type.String);
        assertTrue(new String(tp.getFirst().getTuples().get(0).data).equals("41"));


/*        for(Tuples ts: tp) {
            for (Tuple tuple: ts.getTuples())
                System.out.print(tuple.type+" "+new String(tuple.data) + "\t");
            System.out.println();
        }*/
    }

}