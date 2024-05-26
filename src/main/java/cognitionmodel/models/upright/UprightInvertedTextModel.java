package cognitionmodel.models.upright;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.InvertedTextModel;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.fusesource.jansi.Ansi.ansi;

public class UprightInvertedTextModel {


    private TextIndex textIndex;
    private InvertedTextModel textModel;
    private String indexFile;

    private int[] tokenFreqs;

    double minMrDelta = 1;
    long batchSize = 1000000, minF = 10, startF = minF -1;
    UrGenerator generator;
    public UprightInvertedTextModel(String indexFile) throws IOException, ClassNotFoundException {
        textModel = new InvertedTextModel((TableDataSet) null, "text", "");
        textIndex = textModel.getTextIndex();
        this.indexFile = indexFile;
        textIndex.load(new FileInputStream(indexFile));
        tokenFreqs = new int[textIndex.getIdx(textIndex.getTextField()).keySet().stream().mapToInt(a->(int)a).max().getAsInt()+1];
        for (Map.Entry<Object, RoaringBitmap> e: textIndex.getIdx(textIndex.getTextField()).entrySet())
            tokenFreqs[(Integer) e.getKey()] = e.getValue().getCardinality();
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


    public double getMinMrDelta() {
        return minMrDelta;
    }

    public void setMinMrDelta(double minMrDelta) {
        this.minMrDelta = minMrDelta;
    }

    public long getMinF() {
        return minF;
    }

    public void setMinF(long minF) {
        this.minF = minF;
        startF = minF - 1;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }

    public String generate(String text, int attentionSize) throws IOException {
        List<Integer> in = (textIndex.getEncoder().encode(text));
        String r = "";
        long t = System.currentTimeMillis();

        System.out.println(in.size()+" tokens in text");

        minMrDelta = 1;
        batchSize = 10000000;

        ArrayList<UrAgent> al = new ArrayList<>(makeAgentsList(in, attentionSize));

        al = new ArrayList<>(al.stream().filter(a->a.getF() > minF).toList());

        System.out.println(al.size() + " agents");
        System.out.println(al.stream().mapToDouble(a->a.getMr()*a.getF()/a.getDatasize()).sum() + " information");

        t = (System.currentTimeMillis() - t);
        r = r + String.format("Decomposer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t)))+"\n";

        UprightTextComposer composer = new UprightTextComposer(in.size()*2, in.size());

        t = System.currentTimeMillis();

        List<UrComposition> compositions = composer.composeToSortedList(al);
        t = (System.currentTimeMillis() - t);

        r = r + String.format("Composer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t)))+"\n";


/*
        for (int i = 0; i < (min(10, compositions.size())); i++)
            r = r + compositionToColourString(compositions.get(i), in.size())+"; "+compositions.get(i).getUrAgents().size()+"\n";
*/


        LinkedList<Integer> newTokens = generator.newTokens(al, attentionSize);

        r = text + "\t" + textIndex.getEncoder().decode(newTokens);

        return r;
    }

    private class IdxPoint{
        int postion, token;
        long idx;
        Iterator<Long> iterator;

        public IdxPoint(int postion, int token, Iterator<Long> iterator) {
            this.postion = postion;
            this.token = token;
            this.iterator = iterator;
            idx = nextIdx();
        }

        public long nextIdx(){
            if (iterator.hasNext()) return idx = iterator.next();
                else
                    return idx = -1;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }

        public long getIdx() {
            return idx;
        }

        public int getToken() {
            return token;
        }

        public void setToken(int token) {
            this.token = token;
        }

        public int getPostion() {
            return postion;
        }

        public void setPostion(int postion) {
            this.postion = postion;
        }

        public Iterator<Long> getIterator() {
            return iterator;
        }

        public void setIterator(Iterator<Long> iterator) {
            this.iterator = iterator;
        }


    }

    public List<UrAgent> makeAgentsList(List<Integer> in, int attentionSize){
        if (in.isEmpty()) return new ArrayList<>();
        ConcurrentHashMap<String, UrAgent> agents = new ConcurrentHashMap<>();

        TreeMap<Object, RoaringBitmap> index =  textIndex.getIdx(textIndex.getTextField());

        int step = attentionSize*2/3;


        LinkedList<CompletableFuture<Integer>> cfl = new LinkedList<>();
        final long[] nn = {0};


        for (int k = 0; k < in.size(); k += step) {
            int finalK = k;
            cfl.add(CompletableFuture.supplyAsync(() -> {
                final double[] dmr = {0};

                PriorityQueue<IdxPoint> tokens = new PriorityQueue<>(Comparator.comparing(IdxPoint::getIdx));

                for (int i = finalK; i < min(finalK +attentionSize, in.size()); i++) {
                    if (index.containsKey(in.get(i)))
                        tokens.add(new IdxPoint(i, in.get(i), new BatchedIterator(index.get(in.get(i)))));
                    else System.err.println(in.get(i) + " token unknown");
                }

                LinkedList<UrAgent> nlist = new LinkedList<>();
                HashSet<String> cset = new HashSet<>();
                long  n = 0;


                for (; !tokens.isEmpty(); ) {

                    IdxPoint idxPoint = tokens.poll();
                    long ti = idxPoint.getIdx();
                    idxPoint.nextIdx();
                    if (ti != -1) tokens.add(idxPoint);
                        else continue;

                    n++;
                    LinkedList<UrAgent> nnlist = new LinkedList<>();
                    HashSet<String> nset = new HashSet<>();

                    boolean skip = false;

                    if (!nlist.isEmpty()) {
                        for (UrAgent a : nlist) {
                            if (ti - a.getStartpos() < attentionSize) {
                                if (!nset.contains(a.getTokens())) {
                                    nnlist.add(a);
                                    nset.add(a.getTokens());
                                }
                                if (a.getRelations().getLast().getPosition() < idxPoint.getPostion())
                                    if ((idxPoint.getPostion() - a.getRelations().getFirst().getPosition()) < attentionSize) {
                                        nset.remove(a.getTokens());
                                        a.addPoint(new UrPoint(idxPoint.getPostion(), idxPoint.getToken()));
                                        nset.add(a.getTokens());
                                        skip = true;
                                    }
                            } else {
                                if (!agents.containsKey(a.getTokens()))
                                    agents.put(a.getTokens(), new UrAgent(a.getRelations(), startF, tokenFreqs, round(textIndex.getDataSetSize()), ti));
                                else
                                    incAgentF(agents.get(a.getTokens()), 1, a.getStartpos());

                                if (!cset.contains(a.getTokens())) cset.add(a.getTokens());
                            }
                        }
                    }

                    if (!skip) {
                        nnlist.add(new UrAgent(new UrPoint(idxPoint.getPostion(), idxPoint.getToken()), startF, tokenFreqs, round(textIndex.getDataSetSize()), ti));
                        nset.add(nnlist.getLast().getTokens());
                    }
                    nlist = nnlist;

                    if (n % batchSize == 0) {
//                        double ldmr = agents.values().stream().mapToDouble(a->a.getMr()>0? a.getMr():0).sum();
                        double ldmr = cset.stream().mapToDouble(a->agents.get(a).getMr()>0? agents.get(a).getMr():0).sum();
                        if (ldmr - dmr[0] < minMrDelta)
                            break;
                        dmr[0] = ldmr;
                    }

                }
                nn[0] +=n;
                return null;
            }));

/*            if (cfl.size() % 10 == 0 || cfl.size() == in.size()-1)
            {
                // System.out.print(".");
                cfl.stream().map(m -> m.join()).collect(Collectors.toList());
                cfl.clear();
            }*/
        }
        cfl.stream().map(m -> m.join()).collect(Collectors.toList());

        List<UrAgent> al = new ArrayList<>(agents.values());

        al.sort((a1,a2) -> a1.getRelations().size() == a2.getRelations().size()? 0: a1.getRelations().size() > a2.getRelations().size()? 1:-1);

        System.out.println(nn[0] + " tokens analyzed");

        return al;
    }

    private void incAgentF(UrAgent agent, long f, long index){
        if (agent.getRelations().size() > 1){
            agent.incF(f);
            agent.addIndex(index);
        }
    }

    public String compositionToColourString(UrComposition composition, int length){
        String cs = "";

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
            for (UrPoint p: a.getRelations()) {
                ll.add(p.getToken());
                sa[p.getPosition()] = agentColors[i % 32] + textIndex.getEncoder().decode(ll) + ansi().reset();
                ll.clear();
            }
            i++;
        }

        for (String s: sa)
            cs = cs + s;


        return composition.getMr()+" \t " + composition.getS()+" \t "+ cs;
    }



}
