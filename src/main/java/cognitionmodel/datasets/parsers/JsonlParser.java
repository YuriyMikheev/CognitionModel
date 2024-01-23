package cognitionmodel.datasets.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import cognitionmodel.datasets.Tuple;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.json.simple.parser.JSONParser;

import static java.lang.Math.max;


public class JsonlParser implements TabularParser{

    private String endofline = "\n";
    private ArrayList<HashSet<String>> terminalsByfieldIndex = new ArrayList();
    private HashSet<String>  fields = new HashSet<>();
    private JSONParser jsonParser = new JSONParser();

    private Tuple header = new Tuple();
    private String ignoreRegex;

    public JsonlParser(String ignoreRegex) {
        this.ignoreRegex = ignoreRegex;
    }
    public JsonlParser(String ignoreRegex, String endofline) {
        this.ignoreRegex = ignoreRegex;
        this.endofline = endofline;
    }

    public String getEndofline() {
        return endofline;
    }


    @Override
    public List<Tuple> parse(InputStream inputStream) throws IOException {
        return parse(inputStream.readAllBytes());
    }

    /**
     * Returns set of @Link Tuples (set of @Link Tuple) representing data from jsonl stream
     * @param data - read data from stream
     * @return - list of parsed tuples
     */

    @Override
    public List parse(byte[] data) {

        String in = new String(data);
        String[] lines = in.split(endofline, -1);
        LinkedList<Tuple> r = new LinkedList<>();
        LinkedList<CompletableFuture<List<Tuple>>> cfl = new LinkedList<>();

        JSONParser parser = new JSONParser();

        int jl = 0;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                int finalI = i;
                cfl.add(CompletableFuture.supplyAsync(() -> {
                    final Tuple[] tuple = {new Tuple()};
                    LinkedList<Tuple> tl = new LinkedList<>();

                    try {
                        //JSONObject jsonObject = (JSONObject) parser.parse(lines[finalI].replaceAll("\\\\",""));

                        Map<String, Object> flattenedJsonMap = JsonFlattener.flattenAsMap(lines[finalI]);
                        HashSet<String> tf = new HashSet<>();

                        flattenedJsonMap.forEach((k, v) -> {
                            if (ignoreRegex != null)
                                k = k.replaceAll(ignoreRegex, "");
                            int vid = header.findFirstIndex(k);
                            if (vid == -1) synchronized (this) {
                                vid = header.size();
                                header.add(k);
                                fields.add(k);
                            }
                            if (tf.contains(k)) {
                                tl.add(tuple[0]);
                                tuple[0] = new Tuple();
                                tf.clear();
                            }
                            while (tuple[0].size() <= vid) tuple[0].add("");
                            tuple[0].set(vid, v);
                            tf.add(k);
                        });
                        tl.add(tuple[0]);

                        while (terminalsByfieldIndex.size() <= fields.size())
                            terminalsByfieldIndex.add(new HashSet<>());

                        for (int j = 0; j < tuple[0].size(); j++)
                            terminalsByfieldIndex.get(j).add(tuple[0].get(j).getValue().toString());
                    } catch (com.eclipsesource.json.ParseException e) {
                        System.err.println("Missing Json Object at line " + finalI);
                    }
                    return tl;
                }));
            }

            if (i % (int) (max(lines.length * 0.01 + 1, 100)) == 0 || i == lines.length)
            {
                cfl.stream().forEach(t -> r.addAll(t.join()));//map(m -> m.join()).collect(Collectors.toList());
                cfl.clear();
                jl = i;
            }
        }

        return r;
    }

    @Override
    public String[] terminals(int fieldIndex) {
        return terminalsByfieldIndex.get(fieldIndex).toArray(new String[]{});
    }

    @Override
    public Tuple getHeader() {
        return header;
    }


}
