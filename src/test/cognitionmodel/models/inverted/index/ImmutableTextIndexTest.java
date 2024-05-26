package cognitionmodel.models.inverted.index;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.InvertedTextModel;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

public class ImmutableTextIndexTest {

    @Test
    public void load() throws IOException, ClassNotFoundException {
        InvertedTextModel textModel = new InvertedTextModel((TableDataSet) null, "text", "");

        ImmutableTextIndex index = new ImmutableTextIndex(textModel, "text");

        index.load(new FileInputStream("E:\\Idx\\2.txtidx"));
        textModel.setTextIndex(index);

        // textModel.generate("Hello! How are you?");
        System.out.println(textModel.generate("Hello! How are you? I want you to help me! I'm down"));
        //   textModel.generate("There is a list of some of the symptoms you may experience during the third trimester");
        System.out.println((long) textModel.getTextIndex().getDataSetSize());




    }
}