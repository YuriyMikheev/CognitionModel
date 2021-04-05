package org.cognitionmodel;

import org.cognitionmodel.datasets.Tuples;

import java.util.LinkedList;

/**
 * Relation is a pair of pattern and set of tuples.
 *
 *
 */

public class Relation {

    private int patternIndex;
    private LinkedList<Integer> tuples = new LinkedList<>();

    public Relation(int patternIndex, int[] tuplesIndex) {
        this.patternIndex = patternIndex;
        for(int i: tuplesIndex)
            this.tuples.add(i);
    }

    public Relation(int patternIndex, LinkedList<Integer> tuples) {
        this.patternIndex = patternIndex;
        this.tuples = tuples;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public LinkedList<Integer>  getTupleIndex() {
        return tuples;
    }
}
