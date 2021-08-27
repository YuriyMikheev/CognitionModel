package cognitionmodel.datasets;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class TupleTest {


    @Test
    public void addTupleTest(){

        Tuple tuple = new Tuple();

       // System.out.println(tuple.add(1).add("!").add(10.001).add("WTF"));
        assertTrue(tuple.add(1).add(1).add("!").add(10.001).add("WTF").toString().equals("{data=1, type=Int},{data=1, type=Int},{data=!, type=Char},{data=10.001, type=Double},{data=WTF, type=String}"));


    }

    @Test
    public void addAllTest(){
        Tuple tuple = new Tuple();

        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < 10; i++)
            list.add(i);

        tuple.addAll(list);

        //System.out.println(tuple);
        assertTrue(tuple.size() == 10);

    }

    @Test
    public void findFirstIndex() {

        assertTrue(new Tuple().add("1").add(1.0).add("!!!!").findFirstIndex("!!!!") == 2);
      //  System.out.println(new Tuple().add("1").add(1.0).add("!!!!").findFirstIndex("!!!!"));

    }

    @Test
    public void asDoubletest(){

        Tuple t = new Tuple().add("1").add(10.0).add("11.25");

        assertTrue(t.get(0).asDouble() == 1.0);
        assertTrue(t.get(1).asDouble() == 10.0);
        assertTrue(t.get(2).asDouble() == 11.25);
        assertTrue(t.asDoubleArray()[2] == 11.25);
/*
        for (TupleElement te:t)
            System.out.println(te.asDouble());
*/

    }

    @Test
    public void asInttest(){

        Tuple t = new Tuple().add("1").add(10.0).add("11.25").add(3E9);

        assertTrue(t.get(0).asInt() == 1);
        assertTrue(t.get(1).asInt() == 10);
        assertTrue(t.get(2).asInt() == 11);
        assertTrue(t.get(3).asInt() == -1294967296);
/*
        for (TupleElement te:t)
            System.out.println(te.asInt());
*/

    }

}