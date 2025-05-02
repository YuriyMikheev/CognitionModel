package cognitionmodel.models.upright;

import cognitionmodel.datasets.TextDataSet;
import cognitionmodel.models.inverted.InvertedTextModel;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class UprightInvertedTextModelTest {

    @Test
    public void generate() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\R.txtidx");
        System.out.println("Dataset is loaded");
        System.out.println(textModel.generate("Hello! How are you? I want you to help me! I'm down", 7));
        System.out.println(textModel.generate("Hello! How are you? I want you to help me! I'm down Hello! How are you? I want you to help me! I'm down", 7));
        System.out.println(textModel.generate("Hello! How are you? I want you to help me! I'm down Hello! How are you? I want you to help me! I'm down Hello! How are you? I want you to help me! I'm down Hello! How are you? I want you to help me! I'm down", 7));
    }

    @Test
    public void generate1() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\S.txtidx");
        System.out.println("Dataset is loaded");
        int a = 20;
        System.out.println(textModel.generate("The framework that has been laid out by negotiators", a));
        System.out.println(textModel.generate("The framework that has been laid out by negotiators says that during a first six-week pause in the fighting", a));
        System.out.println(textModel.generate("The framework that has been laid out by negotiators says that during a first six-week pause in the fighting, Hamas should release 40 of the remaining hostages, including all the women as well as sick and elderly men. In exchange, hundreds of Palestinian prisoners would be released from Israeli prisons.", a));
        System.out.println(textModel.generate("The framework that has been laid out by negotiators says that during a first six-week pause in the fighting, Hamas should release 40 of the remaining hostages, including all the women as well as sick and elderly men. In exchange, hundreds of Palestinian prisoners would be released from Israeli prisons. Hamas has told international mediators – which include Qatar and Egypt — it does not have 40 living hostages who match those criteria for release, both sources said.", a));
        System.out.println(textModel.generate("The framework that has been laid out by negotiators says that during a first six-week pause in the fighting, Hamas should release 40 of the remaining hostages, including all the women as well as sick and elderly men. In exchange, hundreds of Palestinian prisoners would be released from Israeli prisons. Hamas has told international mediators – which include Qatar and Egypt — it does not have 40 living hostages who match those criteria for release, both sources said. CNN’s record of the conditions of the hostages also suggests there are fewer than 40 living hostages who meet the proposed criteria.", a));
        System.out.println(textModel.generate("The framework that has been laid out by negotiators says that during a first six-week pause in the fighting, Hamas should release 40 of the remaining hostages, including all the women as well as sick and elderly men. In exchange, hundreds of Palestinian prisoners would be released from Israeli prisons. Hamas has told international mediators – which include Qatar and Egypt — it does not have 40 living hostages who match those criteria for release, both sources said. CNN’s record of the conditions of the hostages also suggests there are fewer than 40 living hostages who meet the proposed criteria. The inability — or unwillingness — of Hamas to tell Israel which hostages would be released, alive, is a major obstacle, the second source added. With Hamas appearing to be unable to reach 40 in the proposed categories, Israel has pushed for Hamas to fill out the initial release with younger male hostages, including soldiers, the Israeli official said. Throughout the months of negotiations since the last ceasefire, Israel has repeatedly asked for a list of the hostages and their conditions. Hamas has argued that they need a break in the fighting to be able to track and gather down the hostages, the same argument they made in November before a week-long pause that broke down after Hamas failed to deliver more hostages. The majority of the almost 100 hostages who remain alive are believed to be male IDF soldiers or men of military reserve age. Hamas is expected to try to use them in later phases to try to negotiate more significant concessions, including more high-level prisoners and a permanent end to the war. ", a));
        System.out.println(textModel.generate("Hamas has indicated it is currently unable to identify and track down 40 Israeli hostages needed for the first phase of a ceasefire deal, according to an Israeli official and a source familiar with the discussions, raising fears that more hostages may be dead than are publicly known. The framework that has been laid out by negotiators says that during a first six-week pause in the fighting, Hamas should release 40 of the remaining hostages, including all the women as well as sick and elderly men. In exchange, hundreds of Palestinian prisoners would be released from Israeli prisons. Hamas has told international mediators – which include Qatar and Egypt — it does not have 40 living hostages who match those criteria for release, both sources said. CNN’s record of the conditions of the hostages also suggests there are fewer than 40 living hostages who meet the proposed criteria. The inability — or unwillingness — of Hamas to tell Israel which hostages would be released, alive, is a major obstacle, the second source added. With Hamas appearing to be unable to reach 40 in the proposed categories, Israel has pushed for Hamas to fill out the initial release with younger male hostages, including soldiers, the Israeli official said. Throughout the months of negotiations since the last ceasefire, Israel has repeatedly asked for a list of the hostages and their conditions. Hamas has argued that they need a break in the fighting to be able to track and gather down the hostages, the same argument they made in November before a week-long pause that broke down after Hamas failed to deliver more hostages. The majority of the almost 100 hostages who remain alive are believed to be male IDF soldiers or men of military reserve age. Hamas is expected to try to use them in later phases to try to negotiate more significant concessions, including more high-level prisoners and a permanent end to the war. ", a));
        System.out.println(textModel.generate("Hamas has reported difficulty in identifying and locating 40 Israeli hostages needed for the first phase of a ceasefire deal, according to an Israeli official and a source familiar with the discussions. This raises concerns that more hostages may be deceased than publicly acknowledged. Negotiators have outlined a framework that calls for Hamas to release 40 of the remaining hostages, including all women, and sick and elderly men, during a six-week pause in the fighting. In return, hundreds of Palestinian prisoners would be released from Israeli prisons. However, Hamas has informed international mediators, including Qatar and Egypt, that they do not have 40 living hostages who fit these criteria, according to both sources. CNN's records also indicate fewer than 40 living hostages meet the proposed criteria. The inability or reluctance of Hamas to specify which hostages would be released alive is a significant barrier, the second source added. Since Hamas seems unable to meet the proposed number, Israel has urged Hamas to include younger male hostages, including soldiers, for the initial release, the Israeli official said. Throughout months of negotiations following the last ceasefire, Israel has consistently requested a list of hostages and their conditions. Hamas has argued that they need a break in the fighting to track and gather the hostages, similar to their argument in November before a week-long pause that ended when Hamas failed to deliver more hostages. Most of the nearly 100 remaining hostages are believed to be male IDF soldiers or men of military reserve age. Hamas is expected to use them in later phases to negotiate more significant concessions, including the release of high-level prisoners and a permanent end to the conflict.", a));
    }


    @Test
    public void generate1_1() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\S.txtidx");
        System.out.println("Dataset is loaded");
        int a = 7;
        System.out.println(textModel.generate("The framework that has been laid out by negotiators", a));
        System.out.println(textModel.generate("that during a first six-week pause in the fighting", a));
        System.out.println(textModel.generate("Palestinian prisoners would be released from Israeli prisons.", a));
        System.out.println(textModel.generate("who match those criteria for release, both sources said.", a));
        System.out.println(textModel.generate("40 living hostages who meet the proposed criteria.", a));
        System.out.println(textModel.generate("high-level prisoners and a permanent end to the war. ", a));
        System.out.println(textModel.generate("high-level prisoners and a permanent end to the conflict.", a));
    }

    @Test
    public void generate1_2() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\S.txtidx");
        System.out.println("Dataset is loaded");
        int a = 7;
        System.out.println(textModel.generate("The framework that has been laid out by negotiators says that during a first six-week pause in the fighting", a));
        System.out.println(textModel.generate("as well as sick and elderly men. In exchange, hundreds of Palestinian prisoners would be released from Israeli prisons.", a));
        System.out.println(textModel.generate("include Qatar and Egypt — it does not have 40 living hostages who match those criteria for release, both sources said.", a));
        System.out.println(textModel.generate("the conditions of the hostages also suggests there are fewer than 40 living hostages who meet the proposed criteria.", a));
        System.out.println(textModel.generate("to try to negotiate more significant concessions, including more high-level prisoners and a permanent end to the war. ", a));
    }
    @Test
    public void generateS() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\S.txtidx");
        System.out.println("Dataset is loaded");
        int a = 7;
        System.out.println(textModel.generate("It would have been unsettling to see such a human response in that gray, menacing Hell beast if not for the other demons Ines had met.", a));
    }

    @Test
    public void generate3() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\3.txtidx");
        System.out.println("Dataset is loaded");
        int a = 7;
        System.out.println(textModel.generate("It would have been unsettling to see such a human response in that gray, menacing Hell beast if not for the other demons Ines had met.", a));
    }


    @Test
    public void generate2() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\2.txtidx");
        System.out.println("Dataset is loaded");

        System.out.println(textModel.generate("Follow the armor on the ground nearby to inside the building and climb up. You will find a switch up here that restores power to this part of the city, activating the booster pads nearby.\n" +
                "\n" +
                "Drop down and get your super shotgun ready. This is a little platforming (not bad), so jump to the swing bar, hit the booster and let the air push you outwards. You can hit the shotgun hook from here to sling yourself to the other building, where the game showed us where to go.\n" +
                "\n" +
                "We're in a fight now (of course), so get ready. A Maykr appears at the start of this one with a couple of Whiplash enemies (freeze them). There are some boxes you can stand on to take the Maykr out easier. There's also a lot of booster pads (and a warp gate) in this area, including a pad that shoots you up some stairs that is a bit unique. Note that there is a Hammer Icon here as well.\n" +
                "\n" +
                "Kill those foes and be ready for a Dread Knight and a Cyber Mancubus that appears on the far upper platform. Stone Imps will show up to so use your auto-shotgun to help a ton here. There's a couple Riot Soldiers here as well.\n" +
                "\n" +
                "Once you've thinned most of them out, an Armored Baron will show up. This is a good time to use your hammer of course after you strip his armor, so try to do so and quickshoot rockets and your super shotgun at him to wipe him out quickly. From here you should be able to wipe out whatever is left and this fight will be over.\n" +
                "\n" +
                "OK, let's continue on. The side of this area opened up for you so go up the stairs to see muck with fireballs firing over it. There is land here so you can time a double jump to the land, then jump and dash over the fire to the climbable wall above the fire. From here you need to double jump past the fire to the upper floor, but time it well.\n" +
                "\n" +
                "You can find some zombies up here to get armor from, but further in an empowered Knight will attack. This is a one-off attack (although he does have a Riot Soldier), so grenade and fire belch him to death and collect the goodies here. Locate the booster nearby and use it to go kill a Cyber Mancubus up above (with more zombies to get armor from and another Riot Soldier) and we can continue on in peace.", 7));
    }

    @Test
    public void dataConsistencyCheck() throws IOException, ClassNotFoundException {

        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\2.txtidx");
        System.out.println("Dataset is loaded");

        TreeMap<Object, RoaringBitmap> idx =  textModel.getTextIndex().getIdx(textModel.getTextIndex().getTextField());
        long err = 0;

        LinkedList<Integer> tokenList = new LinkedList<>();
        LinkedList<Integer> itokenList = new LinkedList<>();

        int i = 0;
        for (int token: textModel.generator.getDataSet().getTextTokens()){
            tokenList.add(token);
            itokenList.add(textModel.generator.getDataSet().getTextTokens().get(idx.get(token).nextValue(i)));
            i++;
            if (i % 10 == 0){
                System.out.println(textModel.getTextIndex().getEncoder().decode(tokenList));
                System.out.println("_____________________________________________________________");
                System.out.println(textModel.getTextIndex().getEncoder().decode(itokenList));
                System.out.println("!!!!!!!!!!!!!!!!!!!!");
                tokenList.clear();
                itokenList.clear();
            }
            if (i > 100) break;
        }

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }


    @Test
    public void dataConsistencyCheck1() throws IOException, ClassNotFoundException {

        String sourceFolder = "E:\\Idx\\S";

        InvertedTextModel textModel = new InvertedTextModel(new TextDataSet(sourceFolder), "text", sourceFolder);

        textModel.getTextIndex().optimize();

        UprightTextDataSet dataSet = new UprightTextDataSet();
        dataSet.makeFromFolder(sourceFolder);


        System.out.println("Dataset is loaded");

        TreeMap<Object, RoaringBitmap> idx =  textModel.getTextIndex().getIdx(textModel.getTextIndex().getTextField());
        long err = 0;

        LinkedList<Integer> tokenList = new LinkedList<>();
        LinkedList<Integer> itokenList = new LinkedList<>();

        int i = 0;
        for (int token: dataSet.getTextTokens()){
            tokenList.add(token);
            itokenList.add(dataSet.getTextTokens().get(idx.get(token).nextValue(i)));
            i++;
            if (i % 10 == 0){
                System.out.println(textModel.getTextIndex().getEncoder().decode(tokenList));
                System.out.println("_____________________________________________________________");
                System.out.println(textModel.getTextIndex().getEncoder().decode(itokenList));
                System.out.println("!!!!!!!!!!!!!!!!!!!!");
                tokenList.clear();
                itokenList.clear();
            }
            if (i > 100) break;
        }

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    @Test
    public void freqs() throws IOException, ClassNotFoundException {
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\2.txtidx");
        TreeMap<Object, RoaringBitmap> idx = textModel.getTextIndex().getIdx(textModel.getTextIndex().getTextField());

        ArrayList<Object> lmaps = new ArrayList<>(idx.keySet());
        lmaps.sort((t1,t2) -> {
            long f1 = idx.get(t1).getCardinality();
            long f2 = idx.get(t2).getCardinality();

            return f1 > f2? 1: f1 < f2? -1: 0;
        });

        LinkedList<Integer> tokenList = new LinkedList<>();
        int i = 0;

        for (Object t: lmaps){
            tokenList.add((Integer) t);

            System.out.println(textModel.getTextIndex().getEncoder().decode(tokenList)+"\t"+idx.get(t).getCardinality());
            if (i++ > 100) break;
            tokenList.clear();
        }



    }
}