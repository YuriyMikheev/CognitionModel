package cognitionmodel.utils;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class MaximalGraphPathsTest {


    private static int[][] generateRandomSparseMatrix(int n, double density, Random rnd) {
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) { // upper-triangle only
                if (rnd.nextDouble() < density + 2.0/n) {
                    matrix[i][j] = 1;
                }
            }
        }
        return matrix;
    }

    @Test
    public void graphTest(){
        int[] sizes = {3, 5, 10, 20};
        double density = 0.1;
        int repetitions = 1000;
        Random rnd = new Random(42);

        for (int size : sizes) {
            long totalTime = 0;
            int totalPathCount = 0;
            for (int k = 0; k < repetitions; k++) {
                int[][] matrix = generateRandomSparseMatrix(size, density, rnd);
                long start = System.currentTimeMillis();
                MaximalGraphPaths mgp = new MaximalGraphPaths(matrix);
                List<List<Integer>> paths = mgp.findMaximalPaths();
                long end = System.currentTimeMillis();
                totalTime += (end - start);
                totalPathCount += paths.size();
                // option: System.out.println("Paths found: " + paths.size());
            }
            System.out.printf("SIZE: %3d | Avg time: %6.2f micros | Avg paths: %d%n", size, 1000*totalTime / (double)repetitions, totalPathCount / repetitions);
        }

    }

    @Test
    public void paths() {
        int[][] matrix = {
                {0, 1, 1, 0},
                {0, 0, 1, 1},
                {0, 0, 0, 1},
                {0, 0, 0, 0}
        };

        MaximalGraphPaths amp = new MaximalGraphPaths(matrix);
        List<List<Integer>> paths = amp.findMaximalPaths();

        for (List<Integer> path : paths) {
            for (int i = 0; i < path.size(); i++) {
                System.out.print((path.get(i) + 1) + (i < path.size() - 1 ? "->" : ""));
            }
            System.out.println();
        }
    }


}