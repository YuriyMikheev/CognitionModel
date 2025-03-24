package cognitionmodel.patterns;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.TabularModel;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Math.abs;

/**
 * Set of patterns for table data processing consists from
 * a full set of all values combinations.
 */

public class FullGridIterativePatterns extends PatternSet {

    /**
     * No arguments constructor creates empty set of patterns
     */

    int maxRange = Integer.MAX_VALUE;

    public FullGridIterativePatterns(){
    }

    /**
     * Generates brute force pattern set that consists enumerating of all possible combinations values of all variables.
     * Length of the combination is restricted by @param maxDepth
     *
     * @param length - amount of values
     * @param maxDepth - max number of variables in relation
     */

    public FullGridIterativePatterns(int length, int maxDepth) {
        byte[] enabled = new byte[length];
        Arrays.fill(enabled, (byte) 1);
        int d = 0;
        generate(length, maxDepth, enabled);
    }

    /**
     * Generates brute force pattern set that consists enumerating of all possible combinations values of all @param enabled variables.
     * Length of the combination is restricted by @param maxDepth
     *
     *
     * @param length - amount of values
     * @param maxDepth - max number of variables in relation
     * @param enabled - {0,1} byte array 1 - field ia enabled, 0 - disabled
     */

    public FullGridIterativePatterns(int length, int maxDepth, byte[] enabled){
        int d = 0;

        if (enabled.length < length) {
            int l = enabled.length;
            enabled = Arrays.copyOf(enabled, length);
            Arrays.fill(enabled,l, length, (byte) 1);
        }

        generate(length,  maxDepth, enabled);
    }

    /**
     * Generates brute force pattern set that consists enumerating of all possible combinations values of all @param enabled variables.
     * Length of the combination is restricted by @param maxDepth
     *
     *
     * @param length - amount of values
     * @param maxDepth - max number of variables in relation
     * @param maxRange - max distance between points
     */

    public FullGridIterativePatterns(int length, int maxDepth, int maxRange){
        int d = 0;
        byte[] enabled = new byte[length];
        Arrays.fill(enabled, (byte) 1);

        this.maxRange = maxRange;

        generate(length,  maxDepth, enabled);
    }

    /**
     * Generates brute force pattern set that consists enumerating of all possible combinations values of all enabled variables in the model.
     * Length of the combination is restricted by @param maxDepth
     *
     * @param tabularModel - the model for which the patternSet is generated
     * @param maxDepth - max number of variables in relation
     */

    public FullGridIterativePatterns(TabularModel tabularModel, int maxDepth){
        this(((TableDataSet)tabularModel.getDataSet()).getHeader().getTupleElements().size(), maxDepth, tabularModel.getEnabledFields());
    }

    private static int resultlength(int recordlength, int depth){

        int  r = 0;


        for (int l = 1; l <= depth; l++) {
            int b = 1;
            for (int i = 1, m = recordlength; i <= l; i++, m--)
                b = b * m / i;
            r += b;
        }

        return r ;
    }



    private void generate(int length, int maxDepth,  byte[] enabled) {


        byte[][] res = new byte[resultlength(length, maxDepth)*100][];
        int[] nfilds = new int[res.length];
        int[] len = new int[res.length];
        int[] start = new int[res.length];

        int finish  = 0;

        for (; finish < length; finish++)
            if (enabled[finish] == 1){
                byte[] nr = new byte[length];
                Arrays.fill(nr, (byte) 0);
                nr[finish] = 1;
                res[finish] = nr;
                nfilds[finish] = finish;
                len[finish] = 1;
                start[finish] = finish;
        }

        for (int i = 0; i < finish; i++) {
            if (len[i] >= maxDepth) break;
            byte[] nr = res[i];

            if (nr != null  )
              for (int j = nfilds[i] + 1; j < length; j++)
                if (enabled[j] == 1 && j - start[i] < maxRange) {
                    byte [] tr = Arrays.copyOf(nr, nr.length);
                    tr[j] = 1;
                    len[finish] = len[i]+1;
                    nfilds[finish] = j;
                    res[finish] = tr;
                    start[finish] = start[i];
                    finish++;
            }
        }

        for (int i = 0; i < finish; i++)
            if (res[i] != null)
                patterns.add(new Pattern(res[i]));



    }


    @Override
    public void forEach(Consumer<? super Pattern> action) {

    }

    @Override
    public Spliterator<Pattern> spliterator() {
        return null;
    }
}
