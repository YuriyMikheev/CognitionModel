package cognitionmodel.patterns;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FullGridPatternsTest {

    private FullGridPatterns patterns;

    @Test
    public void  generateTest(){
        patterns = new FullGridPatterns(5, 4);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 30);

        patterns = new FullGridPatterns(4, 3);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 14);

        patterns = new FullGridPatterns(5, 2);
        //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 15);

        patterns = new FullGridPatterns(10, 6);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 847);


        patterns = new FullGridPatterns(6, 3, new byte[]{1,0,1});
      //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 25);


    }
}
