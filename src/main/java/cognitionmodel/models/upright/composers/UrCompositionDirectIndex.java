package cognitionmodel.models.upright.composers;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UrCompositionDirectIndex implements UrCompositionIndexInterface {
    ArrayList<UrComposition> urCompositions = new ArrayList<>();
    BitSet[] index;

    private boolean changed = true;

    public UrCompositionDirectIndex(int length) {
        index = new BitSet[length];
        reindex();
    }

    private void reindex() {
        if (!changed) return;
        index = new BitSet[index.length];

        for (int i = 0; i < index.length; i++)
            index[i] = new BitSet();

        urCompositions.sort(Comparator.comparing(UrComposition::getMr).reversed());

        int idx = 0;
        for (UrComposition urComposition : urCompositions) {
            for (int i : urComposition.getFields().stream().toArray())
                index[i].set(idx);
            idx++;
        }

        changed = false;
    }

    @Override
    public void add(UrComposition urComposition) {
        urCompositions.add(urComposition);
        changed = true;
    }

    @Override
    public List<UrComposition> get(@NotNull BitSet fields) {
        reindex();
        LinkedList<UrComposition> result = new LinkedList<>();

        BitSet rset = new BitSet();
        rset.set(0, urCompositions.size());

        for (int nextbit = fields.nextSetBit(0); nextbit >= 0 & nextbit < index.length; nextbit = fields.nextSetBit(nextbit + 1))
            rset.andNot(index[nextbit]);

        for (int nextbit = rset.nextSetBit(0); nextbit >= 0 & nextbit < urCompositions.size(); nextbit = rset.nextSetBit(nextbit + 1))
            result.add(urCompositions.get(nextbit));

        return result;
    }

    @Override
    public UrComposition getMax(@NotNull BitSet fields) {
        reindex();

        BitSet rset = new BitSet();
        rset.set(0, urCompositions.size());

        for (int nextbit = fields.nextSetBit(0); nextbit >= 0 & nextbit < index.length; nextbit = fields.nextSetBit(nextbit + 1))
            rset.andNot(index[nextbit]);

        int nextbit = rset.nextSetBit(0);
        return nextbit < 0 ? null : urCompositions.get(nextbit);
    }


}
