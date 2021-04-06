package cognitionmodel.patterns;

import cognitionmodel.datasets.Tuple;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FullGridPatternsTest {

    private FullGridPatterns patterns;

    @Test
    public void  generateTest(){
        patterns = new FullGridPatterns(5, 5);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 31);

        patterns = new FullGridPatterns(4, 4);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 15);

        patterns = new FullGridPatterns(5, 3);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 16);

        patterns = new FullGridPatterns(10, 7);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 848);
    }
}
