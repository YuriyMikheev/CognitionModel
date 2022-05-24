package cognitionmodel.patterns;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.util.*;

/**
 * Class represents set of patterns reached by links among them. The links allow optimize pattern set during relation generating process
 *
 */

public class LinkedPatternSet extends PatternSet{

   // private HashMap<Long, Boolean> inclusion = new HashMap<>();
    private BitSet active = new BitSet();
    private HashMap<Pattern, Integer> patternIndex = new HashMap<>();
    private BitSet[] inclusion;


    /**
     * Constructor makes LinkedPatternSet based on PatternSet
     * @param patternSet - PatternSet previously generated
     */


    public LinkedPatternSet(PatternSet patternSet){
        this.patterns.addAll(patternSet.patterns);
        this.patternsType = patternSet.patternsType;
        init();
    }


    /**
     * Constructor makes LinkedPatternSet based on list of patterns
     * Link between patterns A and B means pattern A includes pattern B
     * @param patterns - list of patterns
     */


    public LinkedPatternSet(List<Pattern> patterns){
        this.patterns.addAll(patterns);
        init();
    }

    private void init(){
        patterns.sort(Comparator.comparing(Pattern::getSetAmount));


        inclusion = new BitSet[patterns.size()];

        for (long i = 0; i < patterns.size(); i++) {
            patternIndex.put(patterns.get((int)i), (int)i);
            inclusion[(int)i] = new BitSet();
            for (long j = 0; j < patterns.size(); j++)
                if (i != j) {
                    BitSet c = BitSet.valueOf(patterns.get((int)i).getBitSet().toByteArray());
                    c.and(patterns.get((int)j).getBitSet());
                    inclusion[(int)i].set((int)j, c.cardinality() == patterns.get((int)i).getBitSet().cardinality());
            }
        }

        reactivate();
    }


    /**
     * Sets all patterns to active
     */


    public void reactivate() {

        active.set(0, patterns.size(), true);
/*        for (int i = 0; i < patterns.size(); i++) {
            active.set(i);
        }*/
    }

/*    *//**
     * Sets pattern with index to inactive and all patterns linked with it
     * @param patternIndex - index of the pattern to inactivate
     *//*


    public void setInActive(Integer patternIndex, boolean withLinked){
        if (patternIndex < 0 | patternIndex >= patterns.size())
            throw new IllegalArgumentException("Pattern index "+ patternIndex+ " is out of range [0 - "+patterns.size()+")");

        active.set(patternIndex, false);

        if (withLinked)
           active.andNot(inclusion[patternIndex]);
    }*/

    /**
     * Sets pattern with index to inactive and all patterns linked with it
     * @param pattern -  pattern to inactivate
     */


    public void setInActive(Pattern pattern, boolean withLinked){
        if (!patternIndex.containsKey(pattern) )
            throw new IllegalArgumentException("Pattern "+ pattern+ " is absent in set ");


        active.set(patternIndex.get(pattern), false);

        if (withLinked)
            active.andNot(inclusion[patternIndex.get(pattern)]);
    }


    /**
     * Iterator for active patterns in the PatternSet
     * @return - iterator instance
     */

    @Override
    public Iterator<Pattern> iterator(){
        return new Iterator<Pattern>() {
            private int index = -1;
            @Override
            public boolean hasNext() {
                if (index < -1 | index >= patterns.size() - 1) return false;
                return active.nextSetBit(index + 1) != -1;
            }

            @Override
            public Pattern next() {
                index++;
                index = active.nextSetBit(index);
                return index != -1 ? patterns.get(index) : null;
            }
        };
    }

    public boolean isActive(int index){
        return active.get(index);
    }

    /**
     * Gets two patterns that composes the pattern
     * @param pattern - source pattern
     * @param size - size of minimal subpatern, should be lower then pattern.getSetAmount()-2
     * @param common - common elements of the both subpatterns, adds it if absent. if null nothing will be added
     * @return - array of two subpaterns
     */

     public Pattern[] subpatternsof(Pattern pattern, int size, int[] common){
         if (pattern.getSetAmount() - 2 <= size) throw new IllegalArgumentException("Minimal subpattern size bigger then "+(pattern.getSetAmount() - 2));

         TreeSet<Integer> ttee = new TreeSet<>();

         for (int i = 0; i < pattern.getSetAmount(); i++)
             ttee.add(pattern.getSet()[i]);

         if (common != null)
            for (int i = 0; i < common.length; i++)
                ttee.add(pattern.getSet()[i]);


         Integer p1 = patternIndex.get(ttee.toArray(new Integer[]{}));

         ttee.clear();

         for (int i = size; i < pattern.getSetAmount(); i++)
             ttee.add(pattern.getSet()[i]);

         if (common != null)
             for (int i = 0; i < common.length; i++)
                 ttee.add(pattern.getSet()[i]);

         Integer p2 = patternIndex.get(ttee.toArray(new Integer[]{}));

         return new Pattern[]{patterns.get(p1), patterns.get(p2)};
     }


}
