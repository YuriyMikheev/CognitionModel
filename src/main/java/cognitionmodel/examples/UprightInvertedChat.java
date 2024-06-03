package cognitionmodel.examples;

import cognitionmodel.models.upright.UprightInvertedTextModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class UprightInvertedChat {


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Scanner scanner = new Scanner(System.in);
        UprightInvertedTextModel textModel = new UprightInvertedTextModel("E:\\Idx\\2.txtidx");

        System.out.println("Dataset loaded. Allocated  "+ textModel.getTextIndex().size() + " bytes. Go on!");
        int attention = 7;

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
                System.out.println("source "+textModel.getIndexFile());
                System.out.println("range "+attention);
                continue;
            }


            String answer = textModel.generate(request, attention);

            System.out.println();
            System.out.println(answer);

        } while (true);
    }


}
