package cognitionmodel.patterns;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Math.*;

/**
 * Set of patterns for image data processing represented by table
 * consists from recursive descent process generation outcome.
 * Every recursion step calculates pattern for quadrants and goes deeper until gets 2*2 pixels quadrant.
 *
 */

public class ImageRandomPatterns extends PatternSet {

    /**
     * No arguments constructor creates empty set of patterns
     */

    public ImageRandomPatterns(){
    }

    /**
     * Creates random pattern according
     *
     * @param labelindex - predicting variable index
     * @param length - pattern length
     * @param count - amount of shifts
     * @param pointscounts - array if numbers of points in pattern
     */

    public ImageRandomPatterns(int labelindex, int length, int[] pointscounts, int count) {

        for (int pointscount: pointscounts)
            for (int i = 0; i < count; i++)
                patterns.add(new Pattern(randomNewState(length, pointscount)).addIndex(labelindex));
    }


    /**
     * Creates cellular pattern #184 according to Stiven Walphram works
     *
     * @param labelindex - predicting variable index
     * @param length - pattern length
     * @param count - amount of shifts
     * @param pointcounts - number of amounts of points in pattern
     */

    public ImageRandomPatterns(int labelindex, int length, int count, int pointcounts) {

        int[] pointscounts = new int[pointcounts];

        for (int i = 0; i < pointscounts.length; i++)
            pointscounts[i] = 3 + randomint(0, length/10);

        for (int pointscount: pointscounts)
            for (int i = 0; i < count; i++)
                patterns.add(new Pattern(randomNewState(length, pointscount)).addIndex(labelindex));
    }




    private static int randomint(int min, int max){
        return (int) round(random()*(max - min)) + min;
    }


    private static byte[] randomNewState(int length, int pointscount){
        byte[] newState = new byte[length];

        for (int i = 0; i < pointscount ; i++){
            newState[randomint(0, newState.length-1)] = 1;

        }


        return newState;
    }

    @Override
    public void forEach(Consumer<? super Pattern> action) {

    }

    @Override
    public Spliterator<Pattern> spliterator() {
        return null;
    }
}
