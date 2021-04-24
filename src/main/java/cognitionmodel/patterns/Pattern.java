package cognitionmodel.patterns;

import java.util.Arrays;
import java.util.BitSet;

public class Pattern {
    private BitSet bitSet;
    private int[] indices;

    public Pattern(byte[] pattern) {

        bitSet = BitSet.valueOf(pattern);
        int c = 0;
        indices = new int[bitSet.stream().toArray().length];
        for (int i = 0; i < pattern.length; i++)
            if (pattern[i] != 0) indices[c++] = i;
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
