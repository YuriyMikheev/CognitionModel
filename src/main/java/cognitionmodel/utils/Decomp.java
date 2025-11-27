package cognitionmodel.utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Decomp {
    public static void main(String[] args) {
        List<int[]> apoints = Arrays.asList(
                new int[]{1, 2, 3},
                new int[]{2, 1, 4},
                new int[]{5, 6},
                new int[]{3, 7}
        );

        List<String> resultLines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        final int[] prev = {Integer.MIN_VALUE}; // tracking previous int value

        apoints.stream()
                .flatMapToInt(Arrays::stream)
                .forEach(i -> {
                    if (i < prev[0]) {
                        // next int less than previous, start a new line
                        resultLines.add(currentLine.toString());
                        currentLine.setLength(0); // clear current line
                    }
                    if (currentLine.length() > 0) currentLine.append(",");
                    currentLine.append(i);
                    prev[0] = i;
                });

        if (currentLine.length() > 0) {
            resultLines.add(currentLine.toString());
        }

        resultLines.forEach(System.out::println);
    }
}