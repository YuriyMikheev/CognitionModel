package cognitionmodel.patterns;

import java.util.Arrays;
import java.util.BitSet;

public class Pattern {
    private BitSet bitSet;

    public Pattern(byte[] pattern) {

        bitSet = BitSet.valueOf(pattern);
    }

    public byte[] get() {

        return bitSet.toByteArray();
    }

    public byte[] getSet() {

        return bitSet.toByteArray();
    }

    public String toString(){

        return Arrays.toString(get());
    }
}
