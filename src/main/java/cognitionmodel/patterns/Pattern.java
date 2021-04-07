package cognitionmodel.patterns;

public class Pattern {
    private byte[] ipattern;

    public Pattern(byte[] pattern) {
        ipattern = new byte[pattern.length/8 + (pattern.length/8 == 0?1:0) + 1];

        for (int j = 0; j < pattern.length; j++) {
            for (int i = 0; i < 8; i++)
                ipattern[j / 8] = (byte) ((byte) (ipattern[j / 8] << 1) + (pattern[j] != 0?(byte) 1: (byte) 0));
        }

        this.ipattern = pattern;
    }

    public byte[] get() {

        byte[] p = new byte[ipattern.length*8];

        int j = 0;
        for (int i = 0; i < ipattern.length; i++) {
            byte b = ipattern[i / 8];
            for (int l = 0; l < 8; l++) {
                b = (byte) (b >> 1);
                p[j++] = (byte) (b % 2);
            }
        }

        return ipattern;
    }
}
