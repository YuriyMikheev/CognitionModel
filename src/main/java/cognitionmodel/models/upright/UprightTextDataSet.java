package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.TextIndexMaker;
import cognitionmodel.models.inverted.index.TextTokens;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import jnr.ffi.annotations.In;
import org.apache.commons.math3.exception.OutOfRangeException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class UprightTextDataSet implements Iterable<Integer>{


    /**
     * Text dataset stores array of tokens for texts in data set
     */
    private TextTokens textTokens = new TextTokens();;
    private TreeMap<Long, String> indexFile = new TreeMap<>();
    ModelType modelType = ModelType.GPT_4_32K;
    Encoding encoder = Encodings.newDefaultEncodingRegistry().getEncoding(modelType.getEncodingType());

    int[] freqs = new int[1000000];

    public UprightTextDataSet(){

    }


    /**
     *
     * @param tokenfile - file with set of ints these are tokens
     * @throws IOException
     */
    public UprightTextDataSet(String tokenfile) throws IOException {
        load(new FileInputStream(tokenfile));
    }


    public void makeFromFolder(String folder) throws IOException {

        Set<String> files = TextIndexMaker.filesList(folder).stream().filter(f->!(f.contains(".txtidx")||f.contains(".txttkz"))).collect(Collectors.toSet());


        for (String f : files.stream().sorted().collect(Collectors.toList())) try {
            //System.out.println(f);
            String text = new String(new FileInputStream(f).readAllBytes());//Files.readString(Paths.get(f), StandardCharsets.UTF_8);
            List<Integer> tokens = encoder.encode(text);
            indexFile.put(textTokens.size(), f);
            for (Integer t : tokens) {
                textTokens.add(t);
                freqs[t]++;
            }
        } catch (Exception e){
            System.err.println("Reading file: "+f+" caused exception: "+e.toString());
        }

        freqs = Arrays.copyOfRange(freqs, 0, (int)textTokens.getMaxToken()+1);
    }

    public Encoding getEncoder() {
        return encoder;
    }

    public int[] getFreqs() {
        return freqs;
    }

    public TextTokens getTextTokens() {
        return textTokens;
    }

    public String getFileByIndex(long index){
        if (index < 0 || index > textTokens.size()) throw new OutOfRangeException(index, 0, textTokens.size());
        return indexFile.floorEntry(index).getValue();
    }

    @Override
    public Iterator<Integer> iterator() {
        return textTokens.iterator();
    }


    public List<Integer> getRange(long startIndex, long endIndex){
        LinkedList<Integer> r = new LinkedList<>();
        for (long i = startIndex; i <endIndex ; i++) {
            r.add(textTokens.get(i));
        }
        return r;
    }

    public String getRangeToString(long startIndex, long endIndex){
        LinkedList<Integer> r = new LinkedList<>();
        for (long i = startIndex; i <endIndex ; i++) {
            r.add(textTokens.get(i));
        }
        return encoder.decode(r);
    }

    public void save(OutputStream outputStream) throws IOException {

        ObjectOutputStream writer = new ObjectOutputStream(outputStream);
        writer.writeUTF(modelType.name());

        writer.writeLong(textTokens.size());

        for (int t: textTokens)
            writer.writeInt(t);

        writer.close();
        outputStream.close();
    }

    public void load(InputStream inputStream) throws IOException {

        ObjectInputStream reader = new ObjectInputStream(inputStream);

        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        String s = reader.readUTF();
        modelType = ModelType.valueOf(s);
        encoder = registry.getEncoding(modelType.getEncodingType());

        long size = reader.readLong();

        try {
            for (long i = 0; i < size; i++) {
                int t = reader.readInt();
                textTokens.add(t);
                freqs[t]++;
            }
        } catch (EOFException e){
            System.err.println("Unexpected EOF! Continue");
        };

        freqs = Arrays.copyOfRange(freqs, 0, (int)textTokens.getMaxToken()+1);

        reader.close();
        inputStream.close();
    }

    public static void makeTokenizedData(String sourcesFolder) throws IOException {
        for (String f: TextIndexMaker.foldersList(sourcesFolder).stream().sorted().collect(Collectors.toList()))
            if (!f.equals(sourcesFolder))
                if (!Files.exists(Path.of(f+".txttkz"))) {
                    makeTextFolderTokenizedData(f, f+".txttkz");
                } else
                    System.err.println(f+".txttkz exists. Skip making index of "+f);
    }

    public static void makeTextFolderTokenizedData(String folder, String destinationFile) throws IOException {
        UprightTextDataSet dataSet = new UprightTextDataSet();
        dataSet.makeFromFolder(folder);
        dataSet.save(new FileOutputStream(destinationFile));
    }

}
