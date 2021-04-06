package cognitionmodel.patterns;

public class Pattern {
    private byte[] pattern;

    public Pattern(byte[] pattern) {
        this.pattern = pattern;
    }

    public byte[] get() {
        return pattern;
    }
}
