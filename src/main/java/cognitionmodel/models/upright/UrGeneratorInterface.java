package cognitionmodel.models.upright;

import java.util.List;

public interface UrGeneratorInterface {
    UprightTextDataSet getDataSet();

    void setBatchSize(int i);

    List<UrAgent> newAgents(List<UrAgent> alf, int attentionSize, int i, int[] pas);
}
