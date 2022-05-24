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

        bitSet = new BitSet();
        int c = 0;
        indices = new int[pattern.length];
        for (int i = 0; i < pattern.length; i++)
            if (pattern[i] != 0) {
                bitSet.set(i);
                indices[c++] = i;
            }
        indices = Arrays.copyOf(indices, c);
    }


    /**
     * Creates pattern form int array of set bytes indices
     * @param patternIndices - array with indices
     */

    public Pattern(int[] patternIndices) {

        byte[] b = new byte[Arrays.stream(indices).max().getAsInt()+1];

        for (int i: indices) {
            bitSet.set(i);
        }

        indices = new int[bitSet.cardinality()];
        int n = 0;
        for (int i = 0; i < indices.length; i++){
            n = indices[i] = bitSet.nextSetBit(n+1);
        }

        //bitSet = BitSet.valueOf(b);
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

        return bitSet.toString();
    }

    /**
     * Gets amount of set bytes in pattern
     * @return
     */

    public int getSetAmount(){
        return bitSet.cardinality();
    }

    /**
     * Adds index to pattern
     * @param index
     */

    public Pattern addIndex(int index){
        bitSet.set(index);
        indices = Arrays.copyOf(indices, indices.length + 1);
        indices[indices.length - 1] = index;
        indices = Arrays.stream(indices).sorted().toArray();
        return this;
    }


    public BitSet getBitSet() {
        return bitSet;
    }
}
