package org.cognitionmodel.patterns;

import java.util.ArrayList;

public abstract class PatternSet {
    ArrayList<Pattern> patterns = new ArrayList<Pattern>();

    public PatternSet() {
    }

    public PatternSet(ArrayList<Pattern> patterns) {
        this.patterns = patterns;
    }

    public ArrayList<Pattern> getPatterns() {
        return patterns;
    }

    public void generate(){

    }

}
