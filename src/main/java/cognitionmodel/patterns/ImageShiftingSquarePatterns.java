package cognitionmodel.patterns;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Math.random;
import static java.lang.Math.round;

/**
 * Set of patterns for image data processing represented by table
 * consists from recursive descent process generation outcome.
 * Every recursion step calculates pattern for quadrants and goes deeper until gets 2*2 pixels quadrant.
 *
 */

public class ImageShiftingSquarePatterns extends PatternSet {

    /**
     * No arguments constructor creates empty set of patterns
     */

    public ImageShiftingSquarePatterns(){
    }

    /**
     * Creates square patterns shifted from 0,0 to imageHeight, imageWidth
     *
     * @param labelindex - predicting variable index
     * @param height - square height
     * @param width - square width
     * @param imageHeight - height of the image
     * @param imageWidth - width of the image
     * @param siftsCount - amount of shifts
     * @param step - length of step that square shifted
     */

    public ImageShiftingSquarePatterns(int labelindex, int height, int width, int imageHeight, int imageWidth, int siftsCount, int step) {

        int[] points = new int[height * width + 1];
        points[0] = labelindex;
        int c = 1;
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                points[c++] = getBubbleX(i*imageWidth + j, imageHeight * imageWidth + 1, labelindex);

        patterns.add(new Pattern(points.clone()));

        for (c = 0; c < siftsCount; c++){
            for (int i = 1; i < points.length; i++)
                points[i] = getBubbleX(points[i] + step, imageHeight * imageWidth + 1, labelindex);
            patterns.add(new Pattern(points.clone()));
        }
    }

    private static int getBubbleX(int x, int length, int labelindex) {
        if (x >= labelindex) x++;
        int y =  (x < 0 ? (length + x - 1) % length : x > length - 1 ? x % length: x);
        return y;
    }


    @Override
    public void forEach(Consumer<? super Pattern> action) {

    }

    @Override
    public Spliterator<Pattern> spliterator() {
        return null;
    }
}
