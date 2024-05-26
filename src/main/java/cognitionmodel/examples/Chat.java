package cognitionmodel.examples;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.InvertedTextModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Chat {


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Scanner scanner = new Scanner(System.in);
        InvertedTextModel textModel = new InvertedTextModel((TableDataSet) null, "text", "");
        textModel.getTextIndex().load(new FileInputStream("E:\\Idx\\2.txtidx"));
        System.out.println("Dataset loaded. Allocated  "+ textModel.getTextIndex().size() + " bytes. Go on!");

        do {
            System.out.print("> ");
            String request = scanner.nextLine();

            if (request.equals("/q")) break;
            if (request.startsWith("/load")){
                String f = request.substring("/load ".length());
                if (Files.exists(Path.of(f))){
                    textModel.getTextIndex().load(new FileInputStream(f));
                    System.out.println(f + " dataset loaded. Allocated  "+ textModel.getTextIndex().size() + " bytes. Go on!");
                    continue;
                } else
                    System.err.println(f+" file not found");
            }
            if ((request.startsWith("/depth"))){
                String f = request.substring("/depth ".length());
                int d = 0;
                try {
                    d = Integer.parseInt(f);
                    textModel.setDepth(d);
                    continue;
                } catch (Exception e){
                    System.err.println(d + " is not integer");
                }
            }

            if ((request.startsWith("/range"))){
                String f = request.substring("/range ".length());
                int d = 0;
                try {
                    d = Integer.parseInt(f);
                    textModel.setRange(d);
                    continue;
                } catch (Exception e){
                    System.err.println(d + " is not integer");
                }
            }

            if ((request.startsWith("/comp"))){
                String f = request.substring("/comp ".length());
                int d = 0;
                try {
                    d = Integer.parseInt(f);
                    textModel.setMaxComp(d);
                    continue;
                } catch (Exception e){
                    System.err.println(d + " is not integer");
                }
            }

            if ((request.startsWith("/minmr"))){
                String f = request.substring("/minmr ".length());
                double d = 0;
                try {
                    d = Double.parseDouble(f);
                    textModel.setMinMr(d);
                    continue;
                } catch (Exception e){
                    System.err.println(d + " is not double");
                }
            }

            if ((request.startsWith("/params"))){
                System.out.println("depth "+textModel.getDepth());
                System.out.println("range "+textModel.getRange());
                System.out.println("minMr "+textModel.getMinMr());
                System.out.println("maxComp "+textModel.getMaxComp());
                continue;
            }


            String answer = textModel.generate(request);
            System.out.println();
            System.out.println(answer);

        } while (true);
    }


}
