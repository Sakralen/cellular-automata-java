package edu.hsai.cellularautomata.fileio;

import edu.hsai.cellularautomata.CellularAutomata;

import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;


public class FileIO {
    private static final int minRowsCount = 2;
    private static final int minRowsLength = 2;
    private static final Set<Integer> allowedValues = Set.of(0, 1);
    public static int[][] loadFromFile(String path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int[][] matrix =  lines.stream()
                            .map(line -> Stream.of(line.split(" "))
                                    .mapToInt(Integer::parseInt).toArray())
                            .toArray(int[][]::new);

        if (!checkMatrix(matrix)) {
            throw new RuntimeException();
        }

        return matrix;
    }

    public static boolean checkMatrix(int[][] matrix) {
        if (matrix.length < minRowsCount) {
            return false;
        }

        int previousLength = matrix[0].length;
        for (int[] row : matrix) {
            if (row.length < minRowsLength || row.length != previousLength) {
                return false;
            }

            previousLength = row.length;

            for (int x : row) {
                if (!allowedValues.contains(x)) {
                    return false;
                }
            }
        }

        return true;
    }

    // TODO: rework
    public static void writeToFile(CellularAutomata automata, String path) {
        try {
            Files.writeString(Path.of(path), automata.toString() + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
