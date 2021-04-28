package cognitionmodel.models;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.patterns.Pattern;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class SparseLightRelationTest {
    @Test
    public void signatureTest(){
        CSVParser cp = new CSVParser(",", "\r\n");
        LinkedList<Tuple> tp = (LinkedList<Tuple>) cp.get("values,41,0,0.0,0,0,0,0,0,0,0,0,0,0,0,0,y,,".getBytes());
        int[] s = new SparseLightRelation(0).makeSignature(cp.getHeader());

        Tuple t = new SparseLightRelation(0).getTerminals(s);

        //System.out.println(t);

        assertTrue((Integer) t.getTupleElements().get(1).getValue()==41);

    }

    @Test
    public void makeRelation() {
        CSVParser cp = new CSVParser(",", "\r\n");
        LinkedList<Tuple> tp = (LinkedList<Tuple>) cp.get("values,41,0,0.0,0,0,0,0,0,0,0,0,0,0,0,0,y,,".getBytes());
        int[] s = new SparseLightRelation(0).makeSignature(cp.getHeader());

        Pattern p = new Pattern(new int[]{1,4,16,18});

        int s1[] = new SparseLightRelation(0).makeRelation(s, p);

        //System.out.println(s1);
    }
}