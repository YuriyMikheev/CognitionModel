package cognitionmodel.models.upright;

import cognitionmodel.models.inverted.index.TextIndex;
import cognitionmodel.models.upright.agent.UrAgent;
import cognitionmodel.models.upright.agent.UrPoint;
import cognitionmodel.models.upright.composers.UrTextComposer;
import cognitionmodel.models.upright.composers.UrComposition;
import cognitionmodel.models.upright.decomposers.UrArrayDecomposer;
import cognitionmodel.models.upright.decomposers.UrDirectDecomposer;
import cognitionmodel.models.upright.generators.UrGeneratorByAgentsIndexAndPattern;
import cognitionmodel.models.upright.generators.UrGeneratorInterface;
import cognitionmodel.models.upright.relations.UrRelationInterface;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.fusesource.jansi.Ansi.ansi;

public class UrInvertedTextModel {


    private TextIndex textIndex;
    private String indexFile;


    UrGeneratorInterface generator;

    public UrInvertedTextModel(String indexFile) throws IOException, ClassNotFoundException {
        textIndex = new TextIndex(null, "text", null, "");//textModel.getTextIndex();
        this.indexFile = indexFile;
        textIndex.load(new FileInputStream(indexFile));
        generator = new UrGeneratorByAgentsIndexAndPattern(textIndex, indexFile.substring(0, indexFile.length() - 3)+"tkz", new int[]{UrRelationInterface.RELATION_POINTED});
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

        System.out.println(in.size()+" tokens in text");

        generator.setBatchSize(1000000000);
        UrArrayDecomposer decomposer = new UrArrayDecomposer(generator.getDataSet(), UrRelationInterface.RELATION_POINTED, UrRelationInterface.RELATION_ORDER);
   //     UrDecomposer decomposer = new UrDecomposer(0.1, 1000000000, round(textIndex.getDataSetSize()), UrRelationInterface.RELATION_POINTED);

        List<UrAgent> alf = new LinkedList<>();// = makeAgentList(in, textIndex.getIdx(textIndex.getTextField()));



        for (int k = 0; k < 1; k++) {

            t = System.currentTimeMillis();

            if (alf.isEmpty()) {

                alf.addAll(makeAgentList(in, textIndex.getIdx(textIndex.getTextField())));
                alf = decomposer.decompose(alf, attentionSize, 1);

                t = (System.currentTimeMillis() - t);System.out.println(alf.size() + " agents found");
                r = r + String.format("Decomposer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                        TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))) + "\n";
                System.err.println(alf.stream().mapToLong(UrAgent::getF).sum());
                System.err.println(alf.stream().mapToDouble(UrAgent::getMr).sum());

            } else {

                int[] pas = new int[5];
                for (int l = 0; l < pas.length; l++)
                    pas[l] = in.size()+(k-1)*pas.length+l;

                alf = generator.newAgents(alf, 3, 3, pas);

                t = (System.currentTimeMillis() - t);
                System.out.println(alf.size() + " agents generated");
                r = r + String.format("Generator working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                        TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))) + "\n";
            }


            if (alf.isEmpty()) break;


/*            UrComposition composition = new UrComposition();
            for (UrAgent agent: alf)
                composition.add(agent);

            String s = compositionToColourString(composition) + "; " + composition.getUrAgents().size() + "; " + composition.getP() + "\n";
            //System.out.println(s);
            r = r + s;*/

            UrTextComposer composer = new UrTextComposer(alf.size(), in.size() + 100);
           // composer.setMaxN(10000);

            t = System.currentTimeMillis();

            List<UrComposition> compositions = composer.composeToSortedList(alf, attentionSize, 3);

            t = (System.currentTimeMillis() - t);
            r = r + String.format("Composer working time %02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(t), TimeUnit.MILLISECONDS.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)),
                    TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))) + "\n";

            System.out.println(compositions.size()+" compositions made");

            for (int i = 0; i < (min(3, compositions.size())); i++) {
                String s = UrComposition.compositionToColourString(compositions.get(i)) + "; " + compositions.get(i).getUrAgents().size() + "; " + compositions.get(i).getP() + "\n";
                //System.out.println(s);
                r = r + s;
            }

            //alf = alf.stream().filter(a->a.getMr()>0).collect(Collectors.toList());

            alf = new ArrayList<>(new HashSet<>(compositions.stream().limit(15).flatMap(c -> c.getUrAgents().stream()).collect(Collectors.toSet())));
            System.out.println(alf.size()+" distinct agents");
            //attentionSize = attentionSize+attentionSize;
        }
        return r;
    }

    private long orAmount(@NotNull List<UrAgent> agents){

        return RoaringBitmap.or(agents.stream().filter(agent -> agent.getPoints().size() > 1).map(UrAgent::getIdx).collect(Collectors.toList()).listIterator()).getLongCardinality();

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

    public String tokensToStrings(@NotNull List<UrPoint> points){
        LinkedList<Integer> t = new LinkedList<>();
        for (UrPoint p : points) {
            t.add((Integer) p.getToken());
        }

        return textIndex.getEncoder().decode(t);
    }

    public List<UrAgent> makeAgentList(@NotNull List<Integer> in, Map<Object, RoaringBitmap> index){
        int i = 0;
        LinkedList<UrAgent> list = new LinkedList<>();
        for (int t: in){
            if (index.containsKey(t)) {
                list.add(new UrAgent(new UrPoint(i, t), index.get(t), round(textIndex.getDataSetSize())));
                i++;
            }
            else
                System.err.println(in.get(i) + " token unknown");
        }

        return list;
    }


}
