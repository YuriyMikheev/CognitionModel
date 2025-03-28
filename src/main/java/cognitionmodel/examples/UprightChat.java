package cognitionmodel.examples;

import cognitionmodel.datasets.TableDataSet;
import cognitionmodel.models.inverted.InvertedTextModel;
import cognitionmodel.models.upright.UprightInvertedTextModel;
import cognitionmodel.models.upright.UprightTextModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UprightChat {


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Scanner scanner = new Scanner(System.in);
        UprightTextModel textModel = new UprightTextModel("E:\\Idx\\2.txttkz");

        long size = textModel.getDataSet().getTextTokens().size();

        System.out.println("Dataset loaded. Allocated  "+ size + " bytes. Go on!");
        int attention = 7;

        do {
            System.out.print("> ");
            String request = scanner.nextLine();

            if (request.equals("/q")) break;
            if (request.startsWith("/load")){
                String f = request.substring("/load ".length());
                if (Files.exists(Path.of(f))){
                    textModel.getDataSet().load(new FileInputStream(f));
                    System.out.println(f + " dataset loaded. Allocated  "+ size + " bytes. Go on!");
                    continue;
                } else
                    System.err.println(f+" file not found");
            }
            if ((request.startsWith("/attention"))){
               String f = request.substring("/attention ".length());
                try {
                    attention = Integer.parseInt(f);
                    continue;
                } catch (Exception e){
                    System.err.println(f + " is not integer");
                }
            }


            if ((request.startsWith("/params"))){
                System.out.println("range "+attention);
                continue;
            }


            String answer = textModel.generate(request, attention);

            System.out.println();
            System.out.println(answer);

        } while (true);
    }


}
