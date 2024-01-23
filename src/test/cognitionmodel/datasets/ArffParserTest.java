package cognitionmodel.datasets;

import cognitionmodel.datasets.parsers.ArffParser;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ArffParserTest {

    @Test
    public void arffparsertest() throws IOException {
        TableDataSet tableDataSet = new TableDataSet(new FileInputStream(new File("D:\\works\\weka\\Weka-3-8-6\\data\\primary-tumor.arff")),
                new ArffParser());

        System.out.println(tableDataSet.size());


    }


}