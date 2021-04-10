package cognitionmodel;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.datasets.CSVParser;
import cognitionmodel.datasets.TupleElement;
import cognitionmodel.datasets.Tuple;
import cognitionmodel.patterns.FullGridPatterns;

import java.io.*;

public class Examples {

    public static void main(String[] args) throws IOException {

        TableDataSet csvDataSet = new TableDataSet(new BufferedInputStream(new FileInputStream(new File("D:\\works\\Data\\EMNIST\\emnist-mnist-test.csv"))),new CSVParser(",", "\n"));

        for (TupleElement tupleElement : csvDataSet.getHeader().getTupleElements())
            System.out.print(tupleElement +"\t");

        System.out.println();
        int c = 0;
        for(Tuple ts: csvDataSet.getRecords()) {
            for (TupleElement tupleElement : ts.getTupleElements())
                System.out.print(tupleElement +"\t");
            System.out.println();
            if (c++ > 10) break;
        }

        FullGridPatterns fullGridPatterns = new FullGridPatterns(700, 4);
        System.out.println(fullGridPatterns.getPatterns().size());

    }
}
