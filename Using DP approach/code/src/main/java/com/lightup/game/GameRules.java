package com.lightup.game;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all game validation logic and win condition checking.
 */
public class GameRules {
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    
    /**
     * Checks if placing a light at the given position is allowed.
     */
    public boolean isPlacementAllowed(GameBoard board, int row, int col) {
        // Check visibility constraint
        if (wouldConflictWithExistingLight(board, row, col)) {
            return false;
        }
        
        // Check numbered cell constraints
        for (int[] d : DIRECTIONS) {
            int nr = row + d[0], nc = col + d[1];
            if (isInBounds(board, nr, nc)) {
                char ch = board.getCellType(nr, nc);
                if (ch >= '0' && ch <= '4') {
                    int maxLights = ch - '0';
                    int currentLights = countAdjacentLights(board, nr, nc);
                    if (currentLights >= maxLights) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Checks if placing a light at this position would conflict with existing lights.
     */
    public boolean wouldConflictWithExistingLight(GameBoard board, int row, int col) {
        if (board.hasLightAt(row, col)) {
            return true;
        }
        
        // Temporarily place light to check
        boolean[][] hasLight = new boolean[board.getGridSize()][board.getGridSize()];
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                hasLight[r][c] = board.hasLightAt(r, c);
            }
        }
        hasLight[row][col] = true;
        
        boolean conflict = seesOtherLight(board, hasLight, row, col);
        return conflict;
    }
    
    /**
     * Checks if a light at the given position sees another light.
     */
    public boolean seesOtherLight(GameBoard board, int row, int col) {
        boolean[][] hasLight = new boolean[board.getGridSize()][board.getGridSize()];
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                hasLight[r][c] = board.hasLightAt(r, c);
            }
        }
        return seesOtherLight(board, hasLight, row, col);
    }
    
    private boolean seesOtherLight(GameBoard board, boolean[][] hasLight, int row, int col) {
        for (int[] d : DIRECTIONS) {
            int nr = row + d[0], nc = col + d[1];
            while (isInBounds(board, nr, nc) && board.getCellType(nr, nc) == '.') {
                if (hasLight[nr][nc]) {
                    return true;
                }
                nr += d[0];
                nc += d[1];
            }
        }
        return false;
    }
    
    /**
     * Counts the number of lights adjacent to a cell.
     */
    public int countAdjacentLights(GameBoard board, int row, int col) {
        int count = 0;
        for (int[] d : DIRECTIONS) {
            int nr = row + d[0], nc = col + d[1];
            if (isInBounds(board, nr, nc) && board.hasLightAt(nr, nc)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns all cells that would be illuminated by a light at the given position.
     */
    public List<Point> getIlluminatedCells(GameBoard board, int row, int col) {
        List<Point> illuminated = new ArrayList<>();
        for (int[] d : DIRECTIONS) {
            int nr = row + d[0], nc = col + d[1];
            while (isInBounds(board, nr, nc) && board.getCellType(nr, nc) == '.') {
                if (!board.hasLightAt(nr, nc)) {
                    illuminated.add(new Point(nr, nc));
                }
                nr += d[0];
                nc += d[1];
            }
        }
        return illuminated;
    }
    
    /**
     * Checks if the game has been won.
     */
    public boolean checkWin(GameBoard board, JButton[][] buttons) {
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                // Check if any white cell is not illuminated
                if (board.getCellType(r, c) == '.' && !board.hasLightAt(r, c)) {
                    if (buttons[r][c].getBackground().equals(Color.WHITE)) {
                        return false;
                    }
                }
                // Check for red cells (conflicts or violations)
                if (buttons[r][c].getBackground().equals(Color.RED)) {
                    return false;
                }
                if (buttons[r][c].getForeground().equals(Color.RED)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isInBounds(GameBoard board, int row, int col) {
        return row >= 0 && row < board.getGridSize() && col >= 0 && col < board.getGridSize();
    }
}
