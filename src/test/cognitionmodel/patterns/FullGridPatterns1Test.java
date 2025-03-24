package cognitionmodel.patterns;

import org.junit.Test;

import static org.junit.Assert.*;

public class FullGridPatterns1Test {

    private FullGridIterativePatterns patterns;

    @Test
    public void  generateTest(){
        patterns = new FullGridIterativePatterns(5, 4);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 30);

        patterns = new FullGridIterativePatterns(4, 3);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 14);

        patterns = new FullGridIterativePatterns(5, 2);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 15);

        patterns = new FullGridIterativePatterns(10, 6);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 847);


        patterns = new FullGridIterativePatterns(6, 3, new byte[]{1,0,1});
       // System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 25);
    }

    @Test
    public void  generateTestview(){
        patterns = new FullGridIterativePatterns(5, 4);
          System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 30);

        patterns = new FullGridIterativePatterns(4, 3);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 14);

        patterns = new FullGridIterativePatterns(5, 2);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 15);

        patterns = new FullGridIterativePatterns(10, 6);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 847);


        patterns = new FullGridIterativePatterns(6, 3, new byte[]{1,0,1});
        // System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 25);
    }

    @Test
    public void  generateRangeTestview(){
        patterns = new FullGridIterativePatterns(5, 4, 3);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 15);

        patterns = new FullGridIterativePatterns(4, 3, 2);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 7);

        patterns = new FullGridIterativePatterns(5, 2, 2);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 9);

        patterns = new FullGridIterativePatterns(10, 6, 4);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 63);


        patterns = new FullGridIterativePatterns(6, 3, new byte[]{1,0,1});
        // System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 25);
    }

}