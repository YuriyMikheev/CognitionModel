package cognitionmodel.models.inverted.index;

import com.sun.source.tree.AssertTree;
import org.junit.Test;

import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.Assert.*;

public class TextTokensTest {

    @Test
    public void get() {

        TextTokens textTokens = new TextTokens();

        long maxi = 2l* Integer.MAX_VALUE;

        for (long i = 0; i < maxi; i++) {
            textTokens.add((int) i);
            if (i % 10000000 == 0) System.out.println(i);
        }


        System.out.println(textTokens.size());

        Random random = new Random();
        for (int i = 0; i < 100; i++){
            long x = (long)random.nextLong(0, maxi-1);
            assertTrue(textTokens.get(x) == (int) x);
        }


    }

    @Test
    public void iterator() {

        TextTokens textTokens = new TextTokens();

        long maxi = Integer.MAX_VALUE + 100l + Integer.MAX_VALUE  + Integer.MAX_VALUE;
        System.out.println(maxi);

        for (long i = 0; i < maxi; i++) {
            textTokens.add((int) i);
            if (i % 10000000 == 0) System.out.println(i);
        }

        long j = 0;
        for (Integer i: textTokens){
            if (i != (int)j)
                assertTrue(j+"", i == (int)j);
            j++;
            if (j % 10000000 == 0) System.out.println(j);

        }

        Random random  = new Random();

        for (int  i = 0; i < 10000; i++ ){
            j = abs(random.nextLong()) % maxi;
            if (textTokens.get(j) != (int)j)
                assertTrue(j+"", textTokens.get(j) == (int)j);
        }


    }
}