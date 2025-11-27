package cognitionmodel.models.upright;

import cognitionmodel.models.upright.generators.UrGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

public class UrGeneratorTest {

    @Test
    public void allCombinations() {

        assertTrue(UrGenerator.allCombinations(new int[]{1,2,3}, 5).size() == 7);
/*
                .forEach(a-> {
            //System.out.println(Arrays.stream(a).collect(Collectors.toList()));
        });
*/

 //       System.out.println();
        assertTrue(UrGenerator.allCombinations(new int[]{1,2,3,4,5, 10,11,12,13}, 5).size() == 46);
/*
                .forEach(a-> {
           // System.out.println(Arrays.stream(a).collect(Collectors.toList()));
        });
*/


    }
}