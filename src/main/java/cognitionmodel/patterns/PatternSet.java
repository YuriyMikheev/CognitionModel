package cognitionmodel.patterns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

public abstract class PatternSet implements Iterable<Pattern>{
    protected ArrayList<Pattern> patterns = new ArrayList<Pattern>();



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

    public void singleClean(){
        Iterator<Pattern> iterator = patterns.iterator();


        while (iterator.hasNext()){
            if (iterator.next().getSetAmount() == 1) iterator.remove();
        }
    }

    public PatternSet filter(Predicate<Pattern> predicate){
        patterns = new ArrayList<>(patterns.stream().filter(predicate).toList());
        return this;
    };

}
