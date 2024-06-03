package cognitionmodel.models.inverted.index;

import cognitionmodel.datasets.TextDataSet;
import cognitionmodel.models.inverted.InvertedTextModel;
import org.roaringbitmap.RoaringBitmap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TextIndexMaker {

    public static Set<String> foldersList(String dir) throws IOException {
        if (dir.endsWith("\\")) dir = dir.substring(0, dir.length() - 1);
        Set<String> fileList =
            Files.walk(Paths.get(dir), 1).filter(p-> Files.isDirectory(p)).map(p->p.toAbsolutePath().toString()).collect(Collectors.toSet());
        fileList.remove(dir);
        return fileList;
    }


    public static Set<String> filesList(String dir) throws IOException {
        Set<String> fileList = new HashSet<>();
        Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!Files.isDirectory(file)) {
                    fileList.add(file.toAbsolutePath().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }

    public static void makeTextFoldersIndex(String sourcesFolder) throws IOException {
        for (String f: foldersList(sourcesFolder).stream().collect(Collectors.toList()))
            if (!Files.exists(Path.of(f+".txtidx")))
                makeTextFolderIndex(f, f+".txtidx");
            else
                System.err.println(f+".txtidx exists. Skip making index of "+f);
    }

    public static void makeTextFolderIndex(String sourceFolder, String destinationFile) throws IOException {
        InvertedTextModel textModel = new InvertedTextModel(new TextDataSet(sourceFolder), "text", sourceFolder);

        textModel.getTextIndex().optimize();

        textModel.getTextIndex().save(new FileOutputStream(destinationFile));
        System.out.println(sourceFolder + " index completed");
    }

    public TreeMap<String, Integer> getFrequencies(InvertedTextModel textModel){
        TreeMap<Object, RoaringBitmap> tm =  textModel.getTextIndex().getIdx(textModel.getTextIndex().getTextField()+"0");

        TreeMap<String, Integer> r = new TreeMap<>();

        for (Map.Entry<Object, RoaringBitmap> e : tm.entrySet()) {
            r.put(e.getKey().toString(), e.getValue().getCardinality());
        }

        return r;
    }

}



