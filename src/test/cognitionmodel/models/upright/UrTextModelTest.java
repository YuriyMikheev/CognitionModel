package cognitionmodel.models.upright;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UrTextModelTest {

    @Test
    public void generate() throws IOException {

        UrTextModel textModel = new UrTextModel("E:\\Idx\\2.txttkz");
        System.out.println("Dataset is loaded");
       // System.out.println(textModel.generate("Hello! How are you? I want you to help me! I'm down Hello! How are you? I want you to help me! I'm down Hello! How are you? I want you to help me! I'm down Hello! How are you? I want you to help me! I'm down", 7));
        System.out.println(textModel.generate("Hello! How are you? I want you to help me! I'm down", 16));


    }

    @Test
    public void makeAgentsList() {

        UrTextDataSet dataSet = new UrTextDataSet();
        dataSet.getTextTokens().addAll(new int[]{1,2,3,0,0,0,1,0,2,3,0,0,0,2,3});

        UrTextModel textModel = new UrTextModel(dataSet);

        List<Integer> in = new ArrayList<>();

        for (int i: new int[]{0,1,2,3, 0,1,2,3,})
            in.add(i);

        textModel.makeAgentsList(in,5).forEach(System.out::println);

    }
}