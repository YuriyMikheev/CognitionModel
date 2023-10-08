package cognitionmodel.models.inverted;

import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.decomposers.PatternDecomposer;
import cognitionmodel.patterns.FullGridIterativePatterns;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PatternDecomposerTest {


    @Test
    public void initTest() throws IOException {
        InvertedTabularModel tabularModel = new InvertedTabularModel(
                new TableDataSet(new FileInputStream(new File("D:\\works\\Data\\Census\\census-income.data")),
                        new CSVParser(",","\n")),
                (" AHGA, AWKSTAT, CAPLOSS, TAXINC, CAPGAIN").split(","));


        PatternDecomposer patternDecomposer = new PatternDecomposer(new FullGridIterativePatterns(tabularModel.invertedIndex.getFields().size(), 2).getPatterns(), tabularModel, " TAXINC", false);

        patternDecomposer.decompose(tabularModel.getDataSet().getRecords().get(0), " TAXINC");

        System.out.println(patternDecomposer.getPatterns().size());

    }

}