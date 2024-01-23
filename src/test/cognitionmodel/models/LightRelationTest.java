package cognitionmodel.models;

import cognitionmodel.datasets.parsers.CSVParser;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.relations.LightRelation;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class LightRelationTest {

    @Test
    public void signatureTest(){
        CSVParser cp = new CSVParser(",", "\r\n");
        LinkedList<Tuple> tp = (LinkedList<Tuple>) cp.parse("values,41,0,0.0,0,0,0,0,0,0,0,0,0,0,0,0,y,,".getBytes());
        int[] s = new LightRelation().makeSignature(cp.getHeader());

        Tuple t = new LightRelation().getTerminals(s);

        for (int i = 1; i < t.size(); i++)
            assertTrue(t.getTupleElements().get(i).toString().equals(cp.getHeader().get(i).toString()));
            //System.out.println(t.getTupleElements().get(i).toString());

    }

}