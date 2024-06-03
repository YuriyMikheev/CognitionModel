package cognitionmodel.models.upright;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class UprightTextDataSetTest {

    @Test
    public void start() throws IOException {

        UprightTextDataSet uprightTextDataSet = new UprightTextDataSet("E:\\Idx\\2.txttkz");
        System.out.println(uprightTextDataSet.getTextTokens().size());

    }

    @Test
    public void makeTokenizedData() throws IOException {
        UprightTextDataSet.makeTokenizedData("E:\\Idx\\");
    }

    @Test
    public void makeTokenizedDataFromOneFolder() throws IOException {
        UprightTextDataSet.makeTextFolderTokenizedData("E:\\Idx\\2", "E:\\Idx\\02.txttkz");
    }
}