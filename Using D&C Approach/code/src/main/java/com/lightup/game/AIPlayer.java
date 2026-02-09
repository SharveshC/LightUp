package com.lightup.game;

import java.awt.Point;
import java.util.*;

public class AIPlayer {
    private final GameRules rules;

    public AIPlayer(GameRules rules) {
        this.rules = rules;
    }

    /**
     * Finds the best SINGLE move for the AI player using Divide and Conquer.
     * Solves the puzzle recursively but returns only the first move.
     * 
     * @return Point representing the best move, or null if no valid moves exist
     */
    public Point findBestMove(GameBoard board) {
        System.out.println("AI thinking using Divide and Conquer...");
        long startTime = System.currentTimeMillis();

        // Use divide-and-conquer to find the complete solution
        List<Point> solution = solveDivideAndConquer(board);

        // Return ONLY the first move from the solution path
        Point move = (solution != null && !solution.isEmpty()) ? solution.get(0) : null;

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI found move in " + duration + "ms");

        return move;
    }

    /**
     * Solves the puzzle using Divide and Conquer (Recursive Backtracking).
     * 
     * DIVIDE: Break the problem into subproblems by trying each legal move
     * CONQUER: Recursively solve each subproblem
     * COMBINE: Return the solution path if found
     * 
     * @return List of moves that solve the puzzle, or null if no solution exists
     */
    private List<Point> solveDivideAndConquer(GameBoard board) {
        // BASE CASE: Check if puzzle is solved
        if (isSolved(board)) {
            return new ArrayList<>(); // Empty list = solution found
        }

        // DIVIDE: Get all legal moves for current state
        List<Point> legalMoves = getAllLegalMoves(board);

        if (legalMoves.isEmpty()) {
            return null; // No solution from this state
        }

        // CONQUER: Try each move recursively
        for (Point move : legalMoves) {
            // Create a copy of the board for this subproblem
            GameBoard nextBoard = board.copy();
            nextBoard.placeLight(move.x, move.y);

            // Recursively solve the subproblem
            List<Point> resultPath = solveDivideAndConquer(nextBoard);

            // COMBINE: If this path leads to a solution, return it
            if (resultPath != null) {
                resultPath.add(0, move); // Add current move to the front
                return resultPath;
            }
            // Otherwise, backtrack and try next move
        }

        return null; // No solution found from this state
    }

    /**
     * Checks if the puzzle is completely solved.
     */
    private boolean isSolved(GameBoard board) {
        // Check if all white cells are illuminated
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                if (board.getCellType(r, c) == '.' && !board.hasLightAt(r, c)) {
                    if (!isIlluminated(board, r, c)) {
                        return false; // Found an unlit cell
                    }
                }
            }
        }

        // Check if all numbered constraints are satisfied
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                char ch = board.getCellType(r, c);
                if (ch >= '0' && ch <= '4') {
                    int required = ch - '0';
                    int actual = countAdjacentLights(board, r, c);
                    if (actual != required) {
                        return false; // Constraint not satisfied
                    }
                }
            }
        }

        return true; // Puzzle is solved!
    }

    /**
     * Checks if a cell is illuminated by any light.
     */
    private boolean isIlluminated(GameBoard board, int r, int c) {
        if (board.hasLightAt(r, c)) {
            return true;
        }

        // Check all four directions for light beams
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                if (board.getCellType(nr, nc) != '.') {
                    break; // Hit a wall
                }
                if (board.hasLightAt(nr, nc)) {
                    return true; // Found a light illuminating this cell
                }
                nr += d[0];
                nc += d[1];
            }
        }
        return false;
    }

    private int countAdjacentLights(GameBoard board, int r, int c) {
        int count = 0;
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                if (board.hasLightAt(nr, nc)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Gets all legal moves on the current board.
     */
    public List<Point> getAllLegalMoves(GameBoard board) {
        List<Point> moves = new ArrayList<>();

        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                if (board.getCellType(r, c) == '.' &&
                        !board.hasLightAt(r, c) &&
                        !board.isMarkedAt(r, c)) {

                    if (rules.isPlacementAllowed(board, r, c)) {
                        moves.add(new Point(r, c));
                    }
                }
            }
        }
        return moves;
    }
}
