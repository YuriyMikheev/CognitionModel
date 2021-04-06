package test.java;

import main.cognitionmodel.patterns.FullGridPatterns;
import main.cognitionmodel.patterns.Pattern;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

class FullGridPatternsTest extends Assert {

    private FullGridPatterns patterns;

/*    @Before
    public void init() { patterns = new FullGridPatterns(10,4); }

    @After
    public void tearDown() { patterns = null; }
*/
    @Test
    public void generate() {

        patterns = new FullGridPatterns(5,5);
      //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 31);

        patterns = new FullGridPatterns(4,4);
      //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 15);

        patterns = new FullGridPatterns(5,3);
      //  System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 16);

        patterns = new FullGridPatterns(10,7);
        //System.out.println(patterns.getPatterns().size());
        assertTrue(patterns.getPatterns().size() == 848);


/*        patterns = new FullGridPatterns(10,7);

        for (Pattern p: patterns){
            for (byte i: p.get()){
                System.out.print(i+",");
            }
            System.out.println();
        }

        System.out.println(patterns.getPatterns().size());*/

    }

    public static void main(String[] args) {
/*        FullGridPatternsTest ft = new FullGridPatternsTest();

        ft.generate();*/
        JUnitCore runner = new JUnitCore();
        Result result = runner.run(FullGridPatternsTest.class);
        System.out.println("run tests: " + result.getRunCount());
        System.out.println("failed tests: " + result.getFailureCount());
        System.out.println("ignored tests: " + result.getIgnoreCount());
        System.out.println("success: " + result.wasSuccessful());
    }


}