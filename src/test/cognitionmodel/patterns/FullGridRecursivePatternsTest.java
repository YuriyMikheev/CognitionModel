package cognitionmodel.patterns;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FullGridRecursivePatternsTest {

    private FullGridRecursivePatterns patterns;

    @Test
    public void  generateTest(){
        patterns = new FullGridRecursivePatterns(5, 4);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 30);

        patterns = new FullGridRecursivePatterns(4, 3);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 14);

        patterns = new FullGridRecursivePatterns(5, 2);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 15);

        patterns = new FullGridRecursivePatterns(10, 6);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 847);


        patterns = new FullGridRecursivePatterns(6, 3, new byte[]{1,0,1});
      //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 25);


    }
}
