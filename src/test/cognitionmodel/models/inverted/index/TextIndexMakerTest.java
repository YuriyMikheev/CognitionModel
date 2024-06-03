package cognitionmodel.models.inverted.index;

import org.junit.Test;

import java.io.IOException;

public class TextIndexMakerTest {

    @Test
    public void makeIndexesTest() throws IOException {

        TextIndexMaker.makeTextFoldersIndex("E:\\Idx");


    }

    @Test
    public void makeTextFolderIndex() throws IOException {

        TextIndexMaker.makeTextFolderIndex("E:\\Idx\\T1\\", "E:\\Idx\\T1.txtidx");

    }
}