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
        generator = new UrGenerator(textIndex, indexFile.substring(0, indexFile.length() - 3)+"tkz", new int[]{UrRelation.RELATION_POINTED});
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
        System.out.println(in.size()+" tokens in text");
/*        double tf = Arrays.stream(generator.getDataSet().freqs).sum();
        double infd = Arrays.stream(generator.getDataSet().freqs).filter(f->f != 0).mapToDouble(f->-f*(1.0*f/tf)*log(1.0*f/tf)).sum();

        System.out.println(infd+" dataset information amount");
        System.out.println(tf+" tokens in dataset");*/

/*
        UrDecomposer decomposer = new UrDecomposer(0.1, 1000000, round(textIndex.getDataSetSize()), new int[]{UrRelation.RELATION_POINTED});

        LinkedList<UrAgent> al = new LinkedList<>(decomposer.decompose(in, attentionSize, textIndex.getIdx(textIndex.getTextField())));

        HashMap<Integer, UrAgent> agentHashMap = new HashMap<>();
        for (UrAgent a: al)
            if (a.getPoints().size() == 1)
                agentHashMap.put(a.getPoints().getFirst().getPosition(), a);

        System.out.println(al.stream().mapToDouble(a->a.getMr()*a.getP()).sum() + " structural information");
        System.out.println(al.stream().mapToDouble(a->a.getMr()).sum() + " Mr accumulative");

        System.out.println(al.size() + " agents");


        t = (System.currentTimeMillis() - t);
        r = r + String.format("Decomposer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t)))+"\n";*/
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

/*        LinkedList<UrAgent> nada = new LinkedList<>();

        //al = new LinkedList<>(al.stream().filter(a-> a.getPoints().size()>1).collect(Collectors.toList()));

        int depth = 30;

        List<UrPoint> newTokens = generator.newTokens(al, new int[]{7}, depth);
        System.out.println(tokensToStrings(newTokens));

        LinkedList<UrAgent> nal = decomposer.makeAgentList(in,textIndex.getIdx(textIndex.getTextField()));

        HashSet<Integer> nth = new HashSet<>(newTokens.stream().map(UrPoint::getPosition).collect(Collectors.toSet()));
        nal = new LinkedList<>(nal.stream().filter(a->a.getPoints().stream().filter(point -> nth.contains(point.getPosition())).collect(Collectors.toSet()).isEmpty()).collect(Collectors.toList()));

        for (UrPoint p : newTokens) {
            nada.add(new UrAgent(p, textIndex.getIdx(textIndex.getTextField()).get(p.getToken()), round(textIndex.getDataSetSize())));
        }


        nal.addAll(nada);

        al = (LinkedList<UrAgent>) decomposer.decompose(nal, attentionSize);*/

       // UrGenerator generator1 = new UrGenerator(textIndex, generator.getDataSet(), new int[]{UrRelation.RELATION_ORDER});

        generator.setBatchSize(1000000);

        for (int k = 0; k < 10; k++) {


            t = System.currentTimeMillis();

            List<UrAgent> alf = generator.newAgents(makeAgentList(in, textIndex.getIdx(textIndex.getTextField())), attentionSize, 10, new int[]{in.size()});//, in.size()+1, in.size()+2});//, in.size()+3, in.size()+4});//,in.size()+5, in.size()+6});
            System.out.println(alf.size() + " agents found");

/*
        alf = generator1.newAgents(alf, attentionSize,10,new int[]{});//, in.size()+3, in.size()+4,in.size()+5, in.size()+6});
        System.out.println(alf.size()+" agents found");
*/


            t = (System.currentTimeMillis() - t);
            r = r + String.format("Generator working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                    TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))) + "\n";

            //  List<UrAgent> alf = al.stream().filter(agent -> agent.getF() > minF).filter(agent -> agent.getMr() > 5).collect(Collectors.toList());
            // System.out.println(alf.size()+" agents filtered");

            UprightTextComposer composer = new UprightTextComposer(alf.size(), in.size() + 10);

            t = System.currentTimeMillis();

            List<UrComposition> compositions = composer.composeToSortedList(alf);
            try {
                UrAgent na = compositions.get(0).getUrAgents().stream().filter(a -> a.getFields().get(in.size())).findFirst().get();
                in.add(na.getTokens().get(na.getTokens().size() - 1));
            } catch (NoSuchElementException e){

            }

            t = (System.currentTimeMillis() - t);
            r = r + String.format("Composer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                    TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))) + "\n";

            for (int i = 0; i < (min(3, compositions.size())); i++)
                r = r + compositionToColourString(compositions.get(i)) + "; " + compositions.get(i).getUrAgents().size() + "; " + compositions.get(i).getP() + "\n";

            //   r = text + "\n" + r + "\n" + textIndex.getEncoder().decode(newTokens.stream().map(p->(int)p.getToken()).collect(Collectors.toList()));
        }
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

        return composition.getMr()+" \t " + composition.getP()*composition.getMr()+" \t " + composition.getS()+" \t "+ cs;
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

    public String tokensToStrings(List<UrPoint> points){
        LinkedList<Integer> t = new LinkedList<>();
        for (UrPoint p : points) {
            t.add((Integer) p.getToken());
        }

        return textIndex.getEncoder().decode(t);
    }

    public LinkedList<UrAgent> makeAgentList(List<Integer> in, Map<Object, RoaringBitmap> index){
        int i = 0;
        LinkedList<UrAgent> list = new LinkedList<>();
        for (int t: in){
            if (index.containsKey(t))
                list.add(new UrAgent(new UrPoint(i, t), index.get(t), round(textIndex.getDataSetSize())));
            else
                System.err.println(in.get(i) + " token unknown");
            i++;
        }

        return list;
    }


}