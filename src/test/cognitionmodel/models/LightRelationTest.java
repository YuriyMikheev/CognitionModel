package cognitionmodel.models;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.datasets.TupleElement;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class LightRelationTest {

    @Test
    public void signatureTest(){
        CSVParser cp = new CSVParser(",", "\r\n");
        LinkedList<Tuple> tp = (LinkedList<Tuple>) cp.get("values,41,0,0.0,0,0,0,0,0,0,0,0,0,0,0,0,y,,".getBytes());
        int[] s = new LightRelation().makeSignature(tp.getFirst());

        Tuple t = new LightRelation().getTerminals(s);

        for (int i = 0; i < t.size(); i++)
            assertTrue(t.getTupleElements().get(i).toString().equals(tp.getFirst().getTupleElements().get(i).toString()));

    }

}