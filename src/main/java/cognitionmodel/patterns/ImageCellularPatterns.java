package cognitionmodel.patterns;

import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.random;
import static java.lang.Math.round;

/**
 * Set of patterns for image data processing represented by table
 * consists from recursive descent process generation outcome.
 * Every recursion step calculates pattern for quadrants and goes deeper until gets 2*2 pixels quadrant.
 *
 */

public class ImageCellularPatterns extends PatternSet {

    /**
     * No arguments constructor creates empty set of patterns
     */

    public ImageCellularPatterns(){
    }

    /**
     * Creates cellular pattern #184 according to Stiven Walphram works
     *
     * @param labelindex - predicting variable index
     * @param length - pattern length
     * @param count - amount of shifts
     * @param pointscounts - array if numbers of points in pattern randomly generated
     */

    public ImageCellularPatterns(int labelindex, int length, int count, int[] pointscounts) {
        for (int pointscount: pointscounts)
            patterns.addAll(cellularPatterns184(labelindex,length,count, pointscount));
       // addShorter();
    }

    /**
     * Creates cellular pattern #184 according to Stiven Walphram works from random start array
     *
     * @param labelindex - predicting variable index
     * @param length - pattern length
     * @param count - amount of shifts
     * @param pointscounts - array if numbers of points in pattern randomly generated
     * @param numofvariants - amount of start random states
     */

    public ImageCellularPatterns(int labelindex, int length, int count, int[] pointscounts, int numofvariants) {
        for (int pointscount: pointscounts)
            for (int i = 0; i < numofvariants; i++)
                patterns.addAll(cellularPatterns184(labelindex,length,count, pointscount));

    }

    private void addShorter(){
        LinkedList<Pattern> newP = new LinkedList<>();

        for (Pattern pattern: patterns){
            for (int i = pattern.getSetAmount(); i >= 4; i--) {
                int[] ip = Arrays.copyOf(pattern.getSet(), pattern.getSetAmount() - 1);
                newP.add(new Pattern(ip));
            }
        }

        patterns.addAll(newP);
    }

    private static int getBubbleX(int x, int length) {
        return (x < 0 ? (length + x - 1) % length : x > length - 1 ? x % length: x);
    }

    public static LinkedList<Pattern> cellularPatterns184(int labelindex, int length, int count, int pointscount){

        return celularPatterns(labelindex, count,randomNewState(length, pointscount), (byte)184);

    }

    public static LinkedList<Pattern> celularPatterns(int labelindex, int count, byte[] start, byte rule) {
        LinkedList<Pattern> res = new LinkedList<>();
        byte[] newState = start;
        byte[] oldState = new byte[newState.length];

        HashMap<Byte, Byte> rules = new HashMap<>();

        for (byte c = 7; c >= 0; c--) {
            rules.put(c,  (byte)((rule &  (1 << c) ) == (1 << c)?1:0));
        }


        for (int i = 0; i < count; i++) {

            LinkedList<Integer> r = new LinkedList<>();
            r.add(labelindex);

            for (int x = 0; x < newState.length; x++) {
                oldState[x] = newState[x];

                if (newState[x] == 1)
                    r.add(x);
            }

            int[] ra = new int[r.size() ];
            for (int j = 0; r.size() > 0; j++)
                ra[j] = r.poll();

            res.add(new Pattern(ra));

            for (int x = 0; x < newState.length; x++) {
                byte state = 0;
                for (int j = -1; j <= 1; j++)
                    state = (byte) (state + (oldState[getBubbleX(x+j, newState.length)] << (1-j)));
                newState[getBubbleX(x, newState.length)] = rules.get(state);
            }
        }

        return res;
    }


    private static int randomint(int min, int max){
        return (int) round(random()*(max - min)) + min;
    }


    private static byte[] randomNewState(int length, int pointscount){
        byte[] newState = new byte[length];

        for (int i = 0; i < pointscount ; i++){
            newState[randomint(0, newState.length-1)] = 1;
        }

        return newState;
    }

    @Override
    public void forEach(Consumer<? super Pattern> action) {

    }

    @Override
    public Spliterator<Pattern> spliterator() {
        return null;
    }
}
