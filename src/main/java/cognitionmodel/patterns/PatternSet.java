package cognitionmodel.patterns;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class PatternSet implements Iterable<Pattern>{
    ArrayList<Pattern> patterns = new ArrayList<Pattern>();



    public enum PatternsType {
        FullCombinations,
        Recursive,
        Cellular
    }

    public PatternsType patternsType;

    public PatternSet() {

    }

    public PatternSet(ArrayList<Pattern> patterns) {
        this.patterns = patterns;
    }

    public ArrayList<Pattern> getPatterns() {
        return patterns;
    }


    @Override
    public Iterator<Pattern> iterator() {
        return patterns.iterator();
    }
}
