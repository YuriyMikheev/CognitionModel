package cognitionmodel.patterns;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class PatternTest {


    @Test
    public void pattermTest(){
        Pattern pattern = new Pattern(new byte[]{1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,1,1,1});

        byte[] pr = pattern.get();

     //   System.out.println(Arrays.toString(pr));

        assertTrue(Arrays.equals(pr, new byte[]{1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,1,1,1}));
    }

}