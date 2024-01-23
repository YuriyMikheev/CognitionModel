package cognitionmodel.datasets;

import cognitionmodel.datasets.parsers.JsonlParser;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class JsonlParserTest {

    @Test
    public void parse() throws IOException {

        JsonlParser jsonlParser = new JsonlParser("messages\\[\\d+\\].");

        FileInputStream inputStream = new FileInputStream(new File("C:\\Users\\Yuriy\\IdeaProjects\\CognitionModel\\src\\test\\resources\\total_result.jsonl"));

        byte[] in = inputStream.readAllBytes();

        List<Tuple> l = jsonlParser.parse(in);

        System.out.println(jsonlParser.getHeader());
        l.forEach(System.out::println);

    }
}