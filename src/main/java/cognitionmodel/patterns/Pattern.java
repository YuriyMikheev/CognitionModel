package cognitionmodel.patterns;

import java.util.Arrays;
import java.util.BitSet;
import java.util.OptionalInt;

public class Pattern {
    private BitSet bitSet;
    private int[] indices;

    /**
     * Create pattern from {0,1} byte array
     * @param pattern
     */

    public Pattern(byte[] pattern) {

        bitSet = BitSet.valueOf(pattern);
        int c = 0;
        indices = new int[bitSet.stream().toArray().length];
        for (int i = 0; i < pattern.length; i++)
            if (pattern[i] != 0) indices[c++] = i;
    }


    /**
     * Creates pattern form int array of set bytes indices
     * @param patternIndices - array with indices
     */

    public Pattern(int[] patternIndices) {

        indices = patternIndices;
        byte[] b = new byte[Arrays.stream(indices).max().getAsInt()+1];

        for (int i: indices)
            b[i] = 1;

        bitSet = BitSet.valueOf(b);
    }



    /**
     * Gets array of bytes where 1 - the byte that analyzed and 0 - the byte should be ignored
     * @return - byte array {0,1}
     */

    public byte[] get() {

        return bitSet.toByteArray();
    }

    /**
     * Gets array of indices
     * @return
     */


    public int[] getSet() {
        return indices;
    }

    public String toString(){

        return Arrays.toString(get());
    }

    /**
     * Gets amount of set bytes in pattern
     * @return
     */

    public int getSetAmount(){
        return indices.length;
    }
}
