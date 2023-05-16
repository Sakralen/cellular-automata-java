package edu.hsai.cellularautomata;

import java.util.Arrays;
import java.util.Random;

public class CellularAutomata {
    private long rule;
    private int width;
    private int height;
    private int[][] grid;
    private boolean isDead = false;

    public CellularAutomata(int width, int height, long rule) {
        this.width = width;
        this.height = height;
        this.rule = rule;
        grid = new int[height][width];
    }

    private CellularAutomata() {
    }

    public static Builder buildCellularAutomata() {
        return new CellularAutomata().new Builder();
    }

    public void nextState() {
        int[][] nextGrid = new int[height][width];
        int deadCheckSum = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                nextGrid[i][j] = applyRule(i, j);
                deadCheckSum += nextGrid[i][j];
            }
        }

        if (deadCheckSum == 0) {
            isDead = true;
        }

        grid = nextGrid;
    }

    // Cellular automata cell states (represented by binary function with getVonNeumannCode)
    // are numbered from 0 to 2^5 and correspond with Nth bit of a rule; this bit's value
    // is the next state of a given cell.
    public int applyRule(int i, int j) {
        int shift = Integer.parseInt(genVonNeumannCode(i, j), 2);
        return (int) ((rule >>> shift) & 1);
    }

    // Returns "adjacency" code for a cell, which consists of
    // adjacent cells' values (von Neumann neighborhood) in following format:
    // {TOP}{LEFT}{SOURCE}{RIGHT}{BOTTOM}
    // getCyclicIndex methods allow array's boundaries to be closed.
    private String genVonNeumannCode(int i, int j) {
        StringBuilder code = new StringBuilder();

        code.append(grid[getColCyclicIndex(i, -1)][j]);
        for (int k = -1; k <= 1; k++) {
            code.append(grid[i][getRowCyclicIndex(j, k)]);
        }
        code.append(grid[getColCyclicIndex(i, 1)][j]);

        return code.toString();
    }

    // Pretty much Python's list[-1]
    private int getRowCyclicIndex(int j, int offset) {
        return ((j + offset) % width + width) % width;
    }

    private int getColCyclicIndex(int i, int offset) {
        return ((i + offset) % height + height) % height;
    }

    @Override
    public String toString() {
        if (isDead) {
            return "Automata has died :(";
        }

        StringBuilder sb = new StringBuilder();

        sb.append('+').append("-".repeat(width * 2 - 1)).append('+').append("\n");

        for (int[] row : grid) {
            sb.append("|");
            sb.append(String.join(" ", Arrays.stream(row)
                    .mapToObj(this::convertToSymbol).toArray(String[]::new)));
            sb.append("|");
            sb.append("\n");
        }

        sb.append('+').append("-".repeat(width * 2 - 1)).append('+').append("\n");

        return sb.toString();
    }

    private String convertToSymbol(int value) {
        return (value == 0) ? " " : "*";
    }

    public class Builder {
        private Builder() {
        }

        public Builder setHeight(int height) {
            CellularAutomata.this.height = height;
            return this;
        }

        public Builder setWidth(int width) {
            CellularAutomata.this.width = width;
            return this;
        }

        public Builder setRule(long rule) {
            CellularAutomata.this.rule = rule;
            return this;
        }

        public CellularAutomata initRandomly() {
            grid = new int[height][width];
            Random random = new Random();

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    grid[i][j] = random.nextInt(2);
                }
            }

            return CellularAutomata.this;
        }

        public CellularAutomata initFromArray(int[][] array) {
            height = array.length;
            width = array[0].length;
            grid = array;
            return CellularAutomata.this;
        }
    }
}
