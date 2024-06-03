package cognitionmodel.datasets;

import cognitionmodel.datasets.parsers.JsonlParser;
import cognitionmodel.datasets.parsers.TabularParser;
import cognitionmodel.models.inverted.index.TextIndex;
import cognitionmodel.models.inverted.index.TextIndexMaker;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides text data support for text models and indexes
 *
 */

public class TextDataSet extends TableDataSet{

    /**
     * Creates data set and reads texts from input stream.
     * Store parsed data from input stream.
     *
     * @param inputStream - data form input stream
     * @param parser - tabular parser object
     * @throws IOException
     */

    public TextDataSet(InputStream inputStream, TabularParser parser) throws IOException {
        super(inputStream, parser);
    }


    /**
     * Creates data set and reads texts from folder that may contain files and subfolders with texts.
     *
     * @param folder - data form input stream
     * @throws IOException
     */

    public TextDataSet(String folder) throws IOException {
        super(null,  new JsonlParser(null, new String(new byte[]{0})));
        Set<String> fileList = TextIndexMaker.filesList(folder);


        for (String fl: fileList.stream().sorted().collect(Collectors.toList())){
           // String content = "{\"text\":\""+new String(new FileInputStream(fl).readAllBytes()).replace("\"","\\\"").replace("\n","\\\n")+"\",\"file\":\""+fl+"\"}";

            HashMap<String, String> js = new HashMap<>();
            js.put("text", new String(new FileInputStream(fl).readAllBytes()));
            js.put("fileName", fl);
            JSONObject jo = new JSONObject(js);

            getRecords().addAll(getParser().parse(jo.toString().getBytes()));
        }
        System.out.println(folder + " dataset loaded");
    }



    public Tuple getHeader() {
        return ((TabularParser)getParser()).getHeader();
    }

    public int getFieldIndex(String field){
        return getHeader().findFirstIndex(field);
    }




}

