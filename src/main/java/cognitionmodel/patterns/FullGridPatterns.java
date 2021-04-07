package cognitionmodel.patterns;

/**
 * Set of patterns for table data processing consists from
 * a full set of all values combinations.
 */

public class FullGridPatterns extends PatternSet {


    /**
     * Generates brute force pattern set that consists enumerating of all possible combinations values of all variables.
     * Length of the combination is restricted by @param maxDepth
     *
     * @param length - amount of values
     * @param maxDepth - max number of variables in relation
     */

    public FullGridPatterns(int length, int maxDepth){
        int d = 0;
        generate(length, -1, new byte[length], maxDepth, d);
    }

    private void generate(int length, int start, byte[] actual, int maxDepth,  int depth) {
        depth++;
        if (start != -1) patterns.add(new Pattern(actual));

        if (depth < maxDepth)
            for (int i = start + 1; i < length ; i++) {
                actual[i] = 1;
                generate(length, i, actual.clone(),  maxDepth, depth);
                actual[i] = 0;
            }
    }


}
