package cognitionmodel.utils;



import java.util.*;

public class StrictIterativeIncreasingSequences {
    public static List<List<Integer>> findStrictIncreasingSequences(int[][] arr) {
        List<List<Integer>> sequences = new ArrayList<>();
        // Стартуем с одной пустой последовательности
        sequences.add(new ArrayList<>());

        for (int[] choices : arr) {
            List<List<Integer>> newSequences = new ArrayList<>();
            for (List<Integer> seq : sequences) {
                for (int num : choices) {
                    // Добавляем только если порядок возрастания соблюдается
                    if (seq.isEmpty() || num > seq.get(seq.size() - 1)) {
                        List<Integer> newSeq = new ArrayList<>(seq);
                        newSeq.add(num);
                        newSequences.add(newSeq);
                    }
                }
            }
            // После каждого шага оставляем только построенные последовательности
            sequences = newSequences;
            // Если на каком-то шаге не осталось ни одной последовательности — досрочно выходим
            if (sequences.isEmpty()) {
                break;
            }
        }
        return sequences;
    }

    private static void show(int[][] arr){
        List<List<Integer>> result = findStrictIncreasingSequences(arr);
        for (List<Integer> seq : result) {
            System.out.println(seq);
        }
    }

    public static void main(String[] args) {

        show(new int[][]{{0},{1,3}, {3,1}, {4,6},{5}, {6,4}});
        show(new int[][]{{1,3,5},{1,3,5}, {4,6},{1,3,5}, {6,4}});

    }
}
