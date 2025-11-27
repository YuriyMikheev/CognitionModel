package cognitionmodel.models.upright.composers;

import java.util.BitSet;
import java.util.List;

public interface UrCompositionIndexInterface {
    void add(UrComposition urComposition);

    List<UrComposition> get(BitSet fields);

    UrComposition getMax(BitSet fields);
}
