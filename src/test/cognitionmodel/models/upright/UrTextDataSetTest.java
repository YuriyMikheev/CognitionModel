package cognitionmodel.models.upright;

import org.junit.Test;

import java.io.IOException;

public class UrTextDataSetTest {

    @Test
    public void start() throws IOException {

        UrTextDataSet urTextDataSet = new UrTextDataSet("E:\\Idx\\2.txttkz");
        System.out.println(urTextDataSet.getTextTokens().size());

    }

    @Test
    public void makeTokenizedData() throws IOException {
        UrTextDataSet.makeTokenizedData("E:\\Idx\\");
    }

    @Test
    public void makeTokenizedDataFromOneFolder() throws IOException {
        UrTextDataSet.makeTextFolderTokenizedData("E:\\Idx\\2", "E:\\Idx\\02.txttkz");
    }
}