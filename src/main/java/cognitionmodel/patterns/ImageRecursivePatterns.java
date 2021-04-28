package cognitionmodel.patterns;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.TabularModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Math.round;

/**
 * Set of patterns for image data processing represented by table
 * consists from recursive descent process generation outcome.
 * Every recursion step calculates pattern for quadrants and goes deeper until gets 2*2 pixels quadrant.
 *
 */

public class ImageRecursivePatterns extends PatternSet {

    /**
     * No arguments constructor creates empty set of patterns
     */

    public ImageRecursivePatterns(){
    }

    /**
     * Creates set of recursive patterns with detailed parameters.
     *
     * @param labelindex - index of label in record
     * @param imageHeight - the height of images in dataset
     * @param imageWidth - the width of images in dataset
     * @param maxlength - max length of relations < imageHeight*imageWidth
     */

    public ImageRecursivePatterns(int labelindex, int imageHeight, int imageWidth, int maxlength) {
        this(labelindex, imageHeight, imageWidth, maxlength, new int[]{-3, -2, -1, 1, 2, 3}, new int[]{2,3} );
    }


    /**
     * Creates set of recursive patterns with detailed parameters.
     * shifts and steps increase amount of patterns and prediction accuracy
     *
     * @param labelindex - index of label in record
     * @param imageHeight - the height of images in dataset
     * @param imageWidth - the width of images in dataset
     * @param maxlength - max length of relations < imageHeight*imageWidth
     * @param shifts - array of started recursion points shifts. Shifts is needed for elimination effect of quadrant boarders. {-3, -2, -1, 1, 2, 3} is recommended
     * @param steps - array of steps that defines quadrant sizes
     */

    public ImageRecursivePatterns(int labelindex, int imageHeight, int imageWidth, int maxlength, int[] shifts, int[] steps) {

        for (int step : steps) {
            makePatternPart(labelindex, 0, 0, imageHeight, imageWidth, imageHeight, imageWidth, step, maxlength);
            for (int shift : shifts)
                for (int x : new int[]{0, 1})
                    for (int y : new int[]{0, 1})
                        if (!(x == 0 & y == 0))
                            makePatternPart( labelindex, x * shift, y * shift, imageHeight, imageWidth, imageHeight, imageWidth, step, maxlength);
        }
    }




    private int[] makePatternPart(int labelindex, int xoffset, int yoffset, int imageHeight, int imageWidth, int sourceImageHeight, int sourceImageWeight, int step, int lengthpart) {

        int[] p = new int[]{};

        if (imageHeight <= step && imageWidth <= step) {
            int idx = getImagePoint(xoffset, yoffset, sourceImageHeight, sourceImageWeight);
            if (idx >= 0) {
                p = new int[]{idx};
            }
            return p;
        } else {
            ArrayList<int[]> pset = new ArrayList<>();
            for (int i = 0; i < step; i ++)
                for (int j = 0; j < step; j ++) {
                    int[] p1 = makePatternPart(labelindex, xoffset + i*imageWidth/step, yoffset+j*imageHeight/step, imageHeight / step, imageWidth / step, sourceImageHeight, sourceImageWeight, step, lengthpart);
                    pset.add(p1);
                }

            for (int i = 0; i < pset.size(); i++) {
                if (p.length + pset.get(i).length < lengthpart)
                    p = mergeParts(pset.get(i), p);
                for (int j = i + 1; j < pset.size(); j++)
                    if (pset.get(j).length + pset.get(i).length > 2 & pset.get(j).length + pset.get(i).length < lengthpart)
                        patterns.add(new Pattern(mergeParts(new int[]{labelindex}, Arrays.stream(mergeParts(pset.get(i), pset.get(j))).sorted().toArray())));
            }
        }

        if (p.length != 0 & p.length < lengthpart) {
            patterns.add(new Pattern(mergeParts(new int[]{labelindex}, Arrays.stream(p).sorted().toArray())));
        }

        return p;
    }

    public int[] mergeParts(int[] p0, int[] p1) {

        if (p0.length == 0) return p1;
        if (p1.length == 0) return p0;

        int[] r = new int[p0.length + p1.length];

        int i = 0;
        for (; i < p0.length; i++)
            r[i] = p0[i];

        for (; i < p0.length + p1.length; i++)
            r[i] = p1[i - p0.length];

        return r;
    }

    private static int getImagePoint(int x, int y, int imageHeight, int imageWeght) {
        if (x >= imageWeght || y >= imageHeight || x < 0 || y < 0) return -1;
        return 1 + x * imageHeight + y;
    }


    @Override
    public void forEach(Consumer<? super Pattern> action) {

    }

    @Override
    public Spliterator<Pattern> spliterator() {
        return null;
    }
}
