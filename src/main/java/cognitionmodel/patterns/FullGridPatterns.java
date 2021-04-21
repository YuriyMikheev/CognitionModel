package cognitionmodel.patterns;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.TabularModel;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Set of patterns for table data processing consists from
 * a full set of all values combinations.
 */

public class FullGridPatterns extends PatternSet {

    /**
     * No arguments constructor creates empty set of patterns
     */

    public FullGridPatterns(){
    }

    /**
     * Generates brute force pattern set that consists enumerating of all possible combinations values of all variables.
     * Length of the combination is restricted by @param maxDepth
     *
     * @param length - amount of values
     * @param maxDepth - max number of variables in relation
     */

    public FullGridPatterns(int length, int maxDepth) {
        byte[] enabled = new byte[length];
        Arrays.fill(enabled, (byte) 1);
        int d = 0;
        generate(length, -1, new byte[length], maxDepth, d,  enabled);
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

    public FullGridPatterns(int length, int maxDepth, byte[] enabled){
        int d = 0;

        if (enabled.length < length) {
            int l = enabled.length;
            enabled = Arrays.copyOf(enabled, length);
            Arrays.fill(enabled,l, length, (byte) 1);
        }

        generate(length, -1, new byte[length], maxDepth, d,  enabled);
    }

    /**
     * Generates brute force pattern set that consists enumerating of all possible combinations values of all enabled variables in the model.
     * Length of the combination is restricted by @param maxDepth
     *
     * @param tabularModel - the model for which the patternSet is generated
     * @param maxDepth - max number of variables in relation
     */

    public FullGridPatterns(TabularModel tabularModel, int maxDepth){
        this(((TableDataSet)tabularModel.getDataSet()).getHeader().getTupleElements().size(), maxDepth, tabularModel.getEnabledFields());
    }

    private void generate(int length, int start, byte[] actual, int maxDepth,  int depth,  byte[] enabled) {
        depth++;
        if (start != -1) patterns.add(new Pattern(actual));

        if (depth <= maxDepth)
            for (int i = start + 1; i < length ; i++)
                if (enabled[i] != 0) {
                    actual[i] = 1;
                    generate(length, i, actual.clone(),  maxDepth, depth, enabled);
                    actual[i] = 0;
            }
    }


    @Override
    public void forEach(Consumer<? super Pattern> action) {

    }

    @Override
    public Spliterator<Pattern> spliterator() {
        return null;
    }
}
