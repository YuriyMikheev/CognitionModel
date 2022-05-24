package cognitionmodel.patterns;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class LinkedPatternSetTest {


    @Test
    public void patternTest() {
        LinkedPatternSet linkedPatternSet = new LinkedPatternSet(new FullGridRecursivePatterns(5, 5).getPatterns());

        Iterator<Pattern> patternIterator = linkedPatternSet.iterator();

        while (patternIterator.hasNext())
            System.out.println(patternIterator.next());

        linkedPatternSet.setInActive(linkedPatternSet.getPatterns().get(1), true);
        linkedPatternSet.setInActive(linkedPatternSet.getPatterns().get(3), true);
        System.out.println("Inactivate 1");

        int c = 0;
        patternIterator = linkedPatternSet.iterator();
        while (patternIterator.hasNext()) {
            c++;
            Pattern s = patternIterator.next();
            System.out.println(s);
        }
            //System.out.println(patternIterator.next());

        System.out.println(c); //надо посчитать сколько должно быть

        linkedPatternSet.reactivate();
        System.out.println("Reactivate");

        c = 0;
        patternIterator = linkedPatternSet.iterator();
        while (patternIterator.hasNext()) {
            c++;
            patternIterator.next();
        }            //

        System.out.println(c);

    }



}