package edu.hsai.cellularautomata;

import edu.hsai.cellularautomata.fileio.FileIO;
import edu.hsai.cellularautomata.pair.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;

public class CellularAutomataApp {
    private final static Scanner scanner = new Scanner(System.in);

    private static CellularAutomata automata;

    private static final int minWidth = 2;
    private static final int minHeight = 2;
    private static final int maxWidth = 50;
    private static final int maxHeight = 50;
    private static final int defaultHeight = 3;
    private static final int defaultWidth = 3;
    private static final long defaultRule = 319520;
    private static final int statesToPrintCount = 10;

    private static final String regexForSizeSet = "([1-9]\\d*)x([1-9]\\d*)";
    private static final String regexForManualSet = "\\d+\\s+\\d+\\s+[01]";

    private static final String menuLines = """
            +----------------------------------------+
             Создать клеточный автомат            [1]
             Загрузить клеточный автомат из файла [2]
            +----------------------------------------+""";
    private static final String incorrectInputString = "Некорректный ввод!";

    public static void main(String[] args) {
        System.out.println(menuLines);
        System.out.println();

        String input = getMenuChoice();
        switch (input) {
            case "1" -> createAutomata();
            case "2" -> loadAutomata();
            default -> {
                System.out.println("Что-то пошло не так...");
                System.exit(1);
            }
        }
    }

    private static String getMenuChoice() {
        while (true) {
            System.out.println("Выберите пункт меню: ");
            String input = scanner.nextLine().trim();
            System.out.println();

            if (input.equals("1") || input.equals("2")) {
                return input;
            }

            System.out.println(incorrectInputString);
            System.out.println();
        }
    }

    private static void loadAutomata() {
        System.out.println("Введите путь до файла: (default: input-example.txt)");
        String input = scanner.nextLine().trim();
        System.out.println();

        if (input.isEmpty()) {
            input = "input-example.txt";
        }

        int[][] matrix;
        try {
            matrix = FileIO.loadFromFile(input);
        } catch (RuntimeException e) {
            System.out.println("Файл недоступен :(");
            return;
        }

        long rule = getRule();

        automata = CellularAutomata.buildCellularAutomata().setRule(rule).initFromArray(matrix);

        execAutomata();
    }

    private static void createAutomata() {
        var heightAndWidth = getHeightAndWidth();
        int height = heightAndWidth.key();
        int width = heightAndWidth.value();
        long rule = getRule();

        String fillChoice = getFillChoice();
        if (fillChoice.equals("1")) {
            automata = CellularAutomata.buildCellularAutomata()
                    .setHeight(height).setWidth(width).setRule(rule)
                    .initRandomly();
        } else {
            int[][] matrix = new int[height][width];
            printMatrix(matrix);
            System.out.println();
            fillManually(matrix);
            automata = CellularAutomata.buildCellularAutomata()
                    .setRule(rule).initFromArray(matrix);
        }

        System.out.println();
        execAutomata();
    }

    private static Pair<Integer, Integer> getHeightAndWidth() {
        while (true) {
            System.out.println("Введите размер автомата в формате {HxW}: (default: 3x3)\n");
            String input = scanner.nextLine().trim();
            System.out.println();

            if (input.isEmpty()) {
                return new Pair<>(defaultHeight, defaultWidth);
            }

            if (input.matches(regexForSizeSet)) {
                try {
                    int[] splitInput = Arrays.stream(input.split("x")).mapToInt(Integer::parseInt).toArray();

                    if (!(splitInput[0] < minHeight || splitInput[0] > maxHeight
                            || splitInput[1] < minWidth || splitInput[1] > maxWidth)) {
                        return new Pair<>(splitInput[0], splitInput[1]);
                    }
                } catch (NumberFormatException e) {

                }
            }

            System.out.println(incorrectInputString);
            System.out.println();
        }
    }

    private static long getRule() {
        while (true) {
            System.out.println("Введите правило: (default: 319520)\n");
            String input = scanner.nextLine().trim();
            System.out.println();

            if (input.isEmpty()) {
                return defaultRule;
            }

            if (input.matches("0|([1-9]\\d*)")) {
                try {
                    return Long.parseLong(input);
                } catch (NumberFormatException e) {

                }
            }

            System.out.println(incorrectInputString);
            System.out.println();
        }
    }

    private static String getFillChoice() {
        while (true) {
            System.out.println("Сгенерировать начальные условия автоматически [1] " +
                    "или ввести их вручную [2]: (default: 1)\n");
            String input = scanner.nextLine().trim();
            System.out.println();

            if (input.isEmpty()) {
                return "1";
            }

            if (input.equals("1") || input.equals("2")) {
                return input;
            }

            System.out.println(incorrectInputString);
            System.out.println();
        }
    }

    private static void fillManually(int[][] matrix) {
        do {
            System.out.println("Введите строку, столбец и значение в формате {i j value}:");
            String input = scanner.nextLine().trim();
            System.out.println();

            boolean isCorrect = true;

            if (input.matches(regexForManualSet)) {
                String[] splitInput = input.split("\\s+");
                int row = 0;
                int col = 0;
                int val = 0;
                try {
                    row = Integer.parseInt(splitInput[0]);
                    col = Integer.parseInt(splitInput[1]);
                    val = Integer.parseInt(splitInput[2]);
                } catch (Exception e) {
                    isCorrect = false;
                }

                if (isCorrect && !(row < 0 || row >= matrix.length
                        || col < 0 || col >= matrix[0].length)) {
                    matrix[row][col] = val;
                    printMatrix(matrix);
                    System.out.println();
                } else {
                    isCorrect = false;
                }
            } else {
                isCorrect = false;
            }

            if (!isCorrect) {
                System.out.println(incorrectInputString);
                System.out.println();
            }
        } while (isContinue());
    }

    private static boolean isContinue() {
        while (true) {
            System.out.println("Продолжить ввод? Y/N (default: Y)");
            String input = scanner.nextLine().trim();
            System.out.println();

            if (input.isEmpty()) {
                return true;
            }

            if (input.equals("Y") || input.equals("y")) {
                return true;
            }

            if (input.equals("N") || input.equals("n")) {
                return false;
            }

            System.out.println(incorrectInputString);
            System.out.println();
        }
    }

    private static void execAutomata() {
        try {
            Files.createDirectories(Path.of("output/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String outputPath = "output/output-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-HH-mm-ss"))
                + ".txt";

        for (int i = 0; i < statesToPrintCount; i++) {
            FileIO.writeToFile(automata, outputPath);
            automata.nextState();
        }

        System.out.println("Результат работы автомата находится в файле: " + outputPath);
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] rows : matrix) {
            for (int x : rows) {
                System.out.print(x + " ");
            }
            System.out.println();
        }
    }
}
