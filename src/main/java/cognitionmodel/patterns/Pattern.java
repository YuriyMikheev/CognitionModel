package cognitionmodel.patterns;

import java.util.BitSet;

public class Pattern {
    private BitSet bitSet;

    public Pattern(byte[] pattern) {

        bitSet = BitSet.valueOf(pattern);
    }

    public byte[] get() {

        return bitSet.toByteArray();
    }
}
