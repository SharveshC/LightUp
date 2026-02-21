package com.lightup.game;

import java.awt.Point;
import java.util.Stack;

/**
 * Manages the game state including grid layout, light placements, and move history.
 */
public class GameBoard {
    private static final int GRID_SIZE = 7;
    
    private final String[] layout;
    private final boolean[][] hasLight;
    private final boolean[][] isMarked;
    private final Stack<Point> moveHistory;
    
    public GameBoard(String[] layout) {
        this.layout = layout;
        this.hasLight = new boolean[GRID_SIZE][GRID_SIZE];
        this.isMarked = new boolean[GRID_SIZE][GRID_SIZE];
        this.moveHistory = new Stack<>();
    }
    
    public int getGridSize() {
        return GRID_SIZE;
    }
    
    public String[] getLayout() {
        return layout;
    }
    
    public boolean hasLightAt(int row, int col) {
        return hasLight[row][col];
    }
    
    public boolean isMarkedAt(int row, int col) {
        return isMarked[row][col];
    }
    
    public char getCellType(int row, int col) {
        return layout[row].charAt(col);
    }
    
    public void placeLight(int row, int col) {
        hasLight[row][col] = true;
        moveHistory.push(new Point(row, col));
    }
    
    public void removeLight(int row, int col) {
        hasLight[row][col] = false;
    }

    /**
     * Sets a light directly without recording it in history.
     * Useful for AI simulation or temporary checks.
     */
    public void setLight(int row, int col, boolean active) {
        hasLight[row][col] = active;
    }
    
    public void toggleMark(int row, int col) {
        isMarked[row][col] = !isMarked[row][col];
    }
    
    public boolean canUndo() {
        return !moveHistory.isEmpty();
    }
    
    /**
     * Undo the last move(s).
     * @param includeAIMove If true and history has >= 2 moves, undo both AI and player move
     * @return Array of Points that were undone [player] or [ai, player]
     */
    public Point[] undo(boolean includeAIMove) {
        if (moveHistory.isEmpty()) {
            return new Point[0];
        }
        
        Point[] undone;
        
        if (includeAIMove && moveHistory.size() >= 2) {
            Point aiMove = moveHistory.pop();
            Point playerMove = moveHistory.pop();
            hasLight[aiMove.x][aiMove.y] = false;
            hasLight[playerMove.x][playerMove.y] = false;
            undone = new Point[]{aiMove, playerMove};
        } else {
            Point playerMove = moveHistory.pop();
            hasLight[playerMove.x][playerMove.y] = false;
            undone = new Point[]{playerMove};
        }
        
        return undone;
    }
    
    public void reset() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                hasLight[r][c] = false;
                isMarked[r][c] = false;
            }
        }
        moveHistory.clear();
    }
    
    /**
     * Creates a deep copy of the game board.
     * Essential for AI search algorithms to explore states without modifying the actual game.
     */
    public GameBoard copy() {
        GameBoard newBoard = new GameBoard(this.layout);
        
        // Copy light state
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                newBoard.hasLight[r][c] = this.hasLight[r][c];
                newBoard.isMarked[r][c] = this.isMarked[r][c];
            }
        }
        
        // Copy history (though AI might not strictly need full history, it keeps state consistent)
        newBoard.moveHistory.addAll(this.moveHistory);
        
        return newBoard;
    }
}
