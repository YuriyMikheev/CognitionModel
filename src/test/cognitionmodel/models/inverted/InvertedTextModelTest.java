package cognitionmodel.models.inverted;

import cognitionmodel.datasets.TextDataSet;
import cognitionmodel.datasets.parsers.JsonlParser;
import cognitionmodel.datasets.TableDataSet;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class InvertedTextModelTest {

    @Test
    public void index() throws IOException {
        InvertedTextModel textModel = new InvertedTextModel(new TableDataSet(new FileInputStream(new File("C:\\Users\\Yuriy\\IdeaProjects\\CognitionModel\\src\\test\\resources\\total_result.jsonl")),
                new JsonlParser("messages\\[\\d+\\].")),"content", "");


        textModel.getTextIndex().makeShiftedIndexes(10, 100);


        System.out.println(textModel.getTextIndex().getInvertedIndex().get("content").size());
    }

    @Test
    public void generate() throws IOException {
        InvertedTextModel textModel = new InvertedTextModel(new TableDataSet(new FileInputStream(new File("C:\\Users\\Yuriy\\IdeaProjects\\CognitionModel\\src\\test\\resources\\total_result.jsonl")),
                new JsonlParser("messages\\[\\d+\\].")),"content", "");

        textModel.generate("Hello! How are you? I want you to help me! I'm down");
     //   textModel.generate("There is a list of some of the symptoms you may experience during the third trimester");
    }
    @Test
    public void generateFolder() throws IOException {
        InvertedTextModel textModel = new InvertedTextModel(new TextDataSet("F:\\Pile\\clean\\books3\\books3\\the-eye.eu\\public\\Books\\Bibliotik\\E\\"), "text", "");

       // textModel.generate("Hello! How are you?");
        textModel.generate("Hello! How are you? I want you to help me! I'm down");
        //   textModel.generate("There is a list of some of the symptoms you may experience during the third trimester");
        System.out.println((long) textModel.getTextIndex().getDataSetSize());
    }

//1.7, 19, 46, 67, 274
//0.7, 3, 5, 7, 20
//153 966 938, 752 336 265, 1 242 683 689, 1 716 001 536, 4 294 967 295
//421, 777, 967 ,1091, 1496   ///проверить композер!  делает дубли
}