package cognitionmodel.models.upright.generators;

import cognitionmodel.models.upright.UrTextDataSet;
import cognitionmodel.models.upright.agent.UrAgent;

import java.util.List;

public interface UrGeneratorInterface {
    UrTextDataSet getDataSet();

    void setBatchSize(int i);

    List<UrAgent> newAgents(List<UrAgent> alf, int attentionSize, int i, int[] pas);
}
