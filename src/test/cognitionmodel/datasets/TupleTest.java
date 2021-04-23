package cognitionmodel.datasets;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class TupleTest {


    @Test
    public void addTupleTest(){

        Tuple tuple = new Tuple();

       // System.out.println(tuple.add(1).add("!").add(10.001).add("WTF"));
        assertTrue(tuple.add(1).add("!").add(10.001).add("WTF").toString().equals("{data=1, type=Int},{data=!, type=Char},{data=10.001, type=Double},{data=WTF, type=String}"));


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
       // System.out.println(new Tuple().add("1").add(1.0).add("!!!!").findFirstIndex("!!!!"));

    }
}