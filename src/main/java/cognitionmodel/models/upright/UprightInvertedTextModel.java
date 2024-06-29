package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.BatchedIterator;
import cognitionmodel.models.inverted.index.TextIndex;
import org.fusesource.jansi.Ansi;
import org.roaringbitmap.RoaringBitmap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.fusesource.jansi.Ansi.ansi;

public class UprightInvertedTextModel {


    private TextIndex textIndex;
    private String indexFile;


    UrGenerator generator;
    public UprightInvertedTextModel(String indexFile) throws IOException, ClassNotFoundException {
        textIndex = new TextIndex(null, "text", null, "");//textModel.getTextIndex();
        this.indexFile = indexFile;
        textIndex.load(new FileInputStream(indexFile));
        generator = new UrGenerator(textIndex, indexFile.substring(0, indexFile.length() - 3)+"tkz");
    }

    public String getIndexFile() {
        return indexFile;
    }

    public TextIndex getTextIndex() {
        return textIndex;
    }

    public void setTextIndex(TextIndex textIndex) {
        this.textIndex = textIndex;
    }


    public String generate(String text, int attentionSize) throws IOException {
        List<Integer> in = (textIndex.getEncoder().encode(text));
        String r = "";
        long t = System.currentTimeMillis();

        int minF = 10;

/*        double tf = Arrays.stream(generator.getDataSet().freqs).sum();
        double infd = Arrays.stream(generator.getDataSet().freqs).filter(f->f != 0).mapToDouble(f->-f*(1.0*f/tf)*log(1.0*f/tf)).sum();

        System.out.println(infd+" dataset information amount");
        System.out.println(tf+" tokens in dataset");*/

        System.out.println(in.size()+" tokens in text");

        UrDecomposer decomposer = new UrDecomposer(1, 10000000, round(textIndex.getDataSetSize()), new int[]{UrRelation.RELATION_POINTED});

        LinkedList<UrAgent> al = new LinkedList<>(decomposer.decompose(in, attentionSize, textIndex.getIdx(textIndex.getTextField())));

        HashMap<Integer, UrAgent> agentHashMap = new HashMap<>();
        for (UrAgent a: al)
            if (a.getPoints().size() == 1)
                agentHashMap.put(a.getPoints().getFirst().getPosition(), a);

        System.out.println(al.stream().mapToDouble(a->a.getMr()*a.getP()).sum() + " information");
        System.out.println(al.stream().mapToDouble(a->a.getMr()).sum() + " Mr accumulative");

//       al = new ArrayList<>(al.stream().filter(a->(a.getF() > minF) || a.getPoints().size() == 1).toList());

        System.out.println(al.size() + " agents");


        t = (System.currentTimeMillis() - t);
        r = r + String.format("Decomposer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t)))+"\n";
   /*     List<UrComposition> compositions;

        UprightTextComposer composer = new UprightTextComposer(in.size() * 2, in.size());

        t = System.currentTimeMillis();

        compositions = composer.composeToSortedList(al);


        t = (System.currentTimeMillis() - t);

        r = r + String.format("Composer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t)))+"\n";

        UrComposition composition = compositions.get(0);*/
/*        if (composition.getFields().cardinality() < in.size()) {
            for (int i = -1; (i = composition.getFields().nextClearBit(i + 1)) < in.size(); ) {
                UrAgent a = agentHashMap.get(i);
                if (a != null) {
                    if (!composition.add(a))
                        System.err.println(i + " can't add 1-point agent");
                }
                else
                    System.err.println(i+" agent is null");
            }
        }*/


/*
        for (int i = 0; i < (min(1, compositions.size())); i++)
            r = r + compositionToColourString(compositions.get(i), in.size())+"; "+compositions.get(i).getUrAgents().size()+"; "+compositions.get(i).getP()+"\n";
*/

       // al = new ArrayList<>(decomposer.decompose(composition.getUrAgents(), attentionSize));

        LinkedList<UrAgent> nada = new LinkedList<>();


        int depth = 10;

        List<UrPoint> newTokens = generator.newTokens(al, attentionSize, depth);

        LinkedList<UrAgent> nal = decomposer.makeAgentList(in,textIndex.getIdx(textIndex.getTextField()));

        for (UrPoint p : newTokens) {
            nada.add(new UrAgent(p, textIndex.getIdx(textIndex.getTextField()).get(p.getToken()), round(textIndex.getDataSetSize())));
        }

        nal.addAll(nada);

        al = (LinkedList<UrAgent>) decomposer.decompose(nal, attentionSize);

        UprightTextComposer composer = new UprightTextComposer(al.size(), in.size() + al.size());

        List<UrComposition>  compositions = composer.composeToSortedList(al);

        for (int i = 0; i < (min(3, compositions.size())); i++)
            r = r + compositionToColourString(compositions.get(i))+"; "+compositions.get(i).getUrAgents().size()+"; "+compositions.get(i).getP()+"\n";

     //   r = text + "\n" + r + "\n" + textIndex.getEncoder().decode(newTokens.stream().map(p->(int)p.getToken()).collect(Collectors.toList()));

        return r;
    }



    public String compositionToColourString(UrComposition composition){
        String cs = "";
        int length = composition.getUrAgents().stream().mapToInt(a-> a.getPoints().getLast().getPosition()).max().getAsInt()+1;

        String[] agentColors = new String[composition.getUrAgents().size()];
        for (int i = 0; i < agentColors.length; i++) {
            if (i < Ansi.Color.values().length - 1)
                agentColors[i] = ansi().fg(Ansi.Color.values()[i % 8]).toString();
            else if (i < (Ansi.Color.values().length - 1) * 2)
                agentColors[i] = ansi().fgBright(Ansi.Color.values()[i % 8]).toString();
            else if (i < (Ansi.Color.values().length - 1) * 3)
                agentColors[i] = ansi().bg(Ansi.Color.values()[i % 8]).toString();
            else if (i < (Ansi.Color.values().length - 1) * 4)
                agentColors[i] = ansi().bgBright(Ansi.Color.values()[i % 8]).toString();
        }

        String[] sa = new String[length];
        Arrays.fill(sa,"");

        int i = 0;
        List<Integer> ll = new LinkedList<>();
        for (UrAgent a: composition.getUrAgents()){
            for (UrPoint p: a.getPoints()) {
                if (p.getToken() instanceof UrAgent)
                    ll.add((int)((UrAgent)p.getToken()).getPoints().getFirst().getToken());
                else
                    ll.add((int)p.getToken());
                sa[p.getPosition()] = agentColors[i % 32] + textIndex.getEncoder().decode(ll) + ansi().reset();
                ll.clear();
            }
            i++;
        }

        for (String s: sa)
            cs = cs + s;

        return composition.getMr()+" \t " + composition.getS()+" \t "+ cs;
    }


    public long dataConsistencyCheck(){

        TreeMap<Object, RoaringBitmap> idx =  textIndex.getIdx(textIndex.getTextField());
        long err = 0;

        for (Map.Entry<Object, RoaringBitmap> e: idx.entrySet()){
            for (Integer i : e.getValue()) {
                if ((Integer) e.getKey() != generator.getDataSet().getTextTokens().get(i))
                    err++;
            }
        }

        return err;
    }

}
