package cognitionmodel.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Класс для поиска всех максимальных путей в ориентированном ацикличном графе,
 * заданном в виде матрицы смежности (верхнетреугольная часть).
 * Максимальные пути — это такие пути, которые нельзя продолжить дальше
 * (то есть заканчиваются в вершине, из которой нет рёбер к большим номерам).
 */
public class MaximalGraphPaths {
    // Матрица смежности графа
    private int[][] adjacencyMatrix;

    // Количество вершин
    private int n;

    // Список найденных максимальных путей (каждый путь — список индексов вершин)
    private List<List<Integer>> maximalPaths = new ArrayList<>();

    /**
     * Конструктор.
     * @param matrix Матрица смежности графа (int[n][n]), где matrix[i][j] == 1 —
     * значит есть ребро из i в j (только для j > i, так как граф треугольный).
     */
    public MaximalGraphPaths(int[][] matrix) {
        this.adjacencyMatrix = matrix;
        this.n = matrix.length;
    }

    /**
     * Находит все максимальные пути от вершины 1 (индекс 0) к вершинам
     * с возрастающими номерами. Возвращает только те пути, которые
     * нельзя продлить вперёд — не являются подотрезками других (более длинных) путей.
     *
     * @return Список максимальных путей (каждый путь — список индексов вершин)
     */
    public List<List<Integer>> findMaximalPaths() {
        // Для каждой вершины искать максимальные пути из неё
        for (int startVertex = 0; startVertex < n; startVertex++) {
            Stack<List<Integer>> stack = new Stack<>();
            List<Integer> startPath = new ArrayList<>();
            startPath.add(startVertex);
            stack.push(startPath);

            while (!stack.isEmpty()) {
                List<Integer> path = stack.pop();
                int last = path.get(path.size() - 1);

                boolean extended = false;
                for (int next = last + 1; next < n; next++) {
                    if (adjacencyMatrix[last][next] == 1) {
                        List<Integer> newPath = new ArrayList<>(path);
                        newPath.add(next);
                        stack.push(newPath);
                        extended = true;
                    }
                }
                if (!extended) {
                    maximalPaths.add(new ArrayList<>(path));
                }
            }
        }
        return maximalPaths;
    }




}