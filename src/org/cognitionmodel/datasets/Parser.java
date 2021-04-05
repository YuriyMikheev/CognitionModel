package org.cognitionmodel.datasets;

import org.cognitionmodel.Relation;

import java.util.List;

public interface Parser {

    public List<Tuple> get(byte[] data);
}
