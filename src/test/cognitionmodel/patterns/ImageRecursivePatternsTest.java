package cognitionmodel.patterns;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImageRecursivePatternsTest {

    @Test
    public void  generateTest() {
        ImageRecursivePatterns patterns = new ImageRecursivePatterns(0, 20,20,400);
      //  System.out.println(patterns.getPatterns().size());

        assertTrue(patterns.getPatterns().size() == 1819);
    }

}