package gameoflife;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.Timer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Conway's Game of Life.
 * 
 * @author Chris Mayfield
 * @version 7.1.0
 */
public class Conway {

    private static final Logger LOGGER = Logger.getLogger(Conway.class.getName());
    private GridCanvas grid;

    public Conway(String path) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<>();
        try (Scanner scan = new Scanner(new File(path))) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                if (!line.startsWith("!") && !line.isEmpty()) {
                    lines.add(line);
                }
            }
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Pattern file contains no cell rows: " + path);
        }

        int rows = lines.size();
        int cols = lines.get(0).length();
        if (cols == 0) {
            throw new IllegalArgumentException("Pattern rows must not be empty: " + path);
        }
        for (int r = 0; r < rows; r++) {
            String line = lines.get(r);
            if (line.length() != cols) {
                throw new IllegalArgumentException("Pattern row " + (r + 1)
                        + " has " + line.length() + " cells; expected " + cols);
            }
            for (int c = 0; c < cols; c++) {
                char value = line.charAt(c);
                if (value != '.' && value != 'O') {
                    throw new IllegalArgumentException("Invalid cell '" + value
                            + "' at row " + (r + 1) + ", column " + (c + 1));
                }
            }
        }

        grid = new GridCanvas(rows, cols, 20);
        for (int r = 0; r < rows; r++) {
            String line = lines.get(r);
            for (int c = 0; c < cols; c++) {
                if (line.charAt(c) == 'O') {
                    grid.turnOn(r, c);
                }
            }
        }
    }

    /**
     * Creates a grid with two Blinkers.
     */
    public Conway() {
        grid = new GridCanvas(5, 10, 20);
        grid.turnOn(2, 1);
        grid.turnOn(2, 2);
        grid.turnOn(2, 3);
        grid.turnOn(1, 7);
        grid.turnOn(2, 7);
        grid.turnOn(3, 7);
    }

    /**
     * Counts the number of live neighbors around a cell.
     * 
     * @param r row index
     * @param c column index
     * @return number of live neighbors
     */
    private int countAlive(int r, int c) {
        int count = 0;
        count += grid.test(r - 1, c - 1);
        count += grid.test(r - 1, c);
        count += grid.test(r - 1, c + 1);
        count += grid.test(r, c - 1);
        count += grid.test(r, c + 1);
        count += grid.test(r + 1, c - 1);
        count += grid.test(r + 1, c);
        count += grid.test(r + 1, c + 1);
        return count;
    }

    /**
     * Apply the update rules of Conway's Game of Life.
     * 
     * @param cell the cell to update
     * @param count number of live neighbors
     */
    private static void updateCell(Cell cell, int count) {
        if (cell.isOn()) {
            if (count < 2 || count > 3) {
                // Any live cell with fewer than two live neighbors dies,
                // as if by underpopulation.
                // Any live cell with more than three live neighbors dies,
                // as if by overpopulation.
                cell.turnOff();
            }
        } else {
            if (count == 3) {
                // Any dead cell with exactly three live neighbors
                // becomes a live cell, as if by reproduction.
                cell.turnOn();
            }
        }
    }

    /**
     * Counts the neighbors before changing anything.
     * 
     * @return number of neighbors for each cell
     */
    private int[][] countNeighbors() {
        int rows = grid.numRows();
        int cols = grid.numCols();

        int[][] counts = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                counts[r][c] = countAlive(r, c);
            }
        }
        return counts;
    }

    /**
     * Updates each cell based on neighbor counts.
     * 
     * @param counts number of neighbors for each cell
     */
    private void updateGrid(int[][] counts) {
        int rows = grid.numRows();
        int cols = grid.numCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid.getCell(r, c);
                updateCell(cell, counts[r][c]);
            }
        }
    }

    /**
     * Simulates one round of Conway's Game of Life.
     */
    public void update() {
        int[][] counts = countNeighbors();
        updateGrid(counts);
    }

    private void show(String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(grid);
        frame.pack();
        frame.setVisible(true);

        Timer timer = new Timer(500, event -> {
            System.out.println("Live cells: " + grid.countOn());
            update();
            grid.repaint();
        });
        timer.start();
    }

    /**
     * Creates and runs the simulation.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        String title = "Conway's Game of Life";
        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            File localPattern = new File("gliders.cells.txt");
            path = localPattern.isFile()
                    ? localPattern.getPath()
                    : new File("gameoflife", "gliders.cells.txt").getPath();
        }

        final Conway game;
        try {
            game = new Conway(path);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Could not read pattern file: " + path, e);
            return;
        }
        EventQueue.invokeLater(() -> game.show(title));
    }

}
