package com.lightup.game;

import java.awt.Point;
import java.util.*;

/**
 * AI Player using Greedy algorithm.
 */
public class GreedyAIPlayer {
    private final GameRules rules;

    public GreedyAIPlayer(GameRules rules) {
        this.rules = rules;
    }

    /**
     * Finds the best SINGLE move for the AI player using Greedy algorithm.
     * Makes locally optimal choices at each step.
     * 
     * @return Point representing the best move, or null if no valid moves exist
     */
    public Point findBestMove(GameBoard board) {
        System.out.println("AI thinking using Greedy algorithm...");

        long startTime = System.currentTimeMillis();

        // Use greedy approach to find the best move
        Point move = findGreedyMove(board);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI found move in " + duration + "ms");

        return move;
    }

    /**
     * Greedy algorithm: Always make the locally optimal choice.
     */
    private Point findGreedyMove(GameBoard board) {
        List<MoveCandidate> candidates = generateAllCandidates(board);
        
        if (candidates.isEmpty()) {
            return null;
        }

        // Sort candidates by greedy heuristic (best first)
        candidates.sort((a, b) -> Integer.compare(b.score, a.score));

        // Return the best candidate
        return candidates.get(0).position;
    }

    /**
     * Generate all possible moves and score them using greedy heuristics.
     */
    private List<MoveCandidate> generateAllCandidates(GameBoard board) {
        List<MoveCandidate> candidates = new ArrayList<>();
        int size = board.getGridSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board.getCellType(r, c) == '.' && !board.hasLightAt(r, c) && !board.isMarkedAt(r, c)) {
                    if (rules.isPlacementAllowed(board, r, c)) {
                        int score = evaluateMove(board, r, c);
                        if (score > 0) {
                            candidates.add(new MoveCandidate(new Point(r, c), score));
                        }
                    }
                }
            }
        }

        return candidates;
    }

    /**
     * Evaluate a move using greedy heuristics.
     * Higher score = better move.
     */
    private int evaluateMove(GameBoard board, int r, int c) {
        int score = 0;

        // Heuristic 1: Count how many unlit white cells this move would illuminate
        int illuminatedCells = countIlluminatedCells(board, r, c);
        score += illuminatedCells * 10;

        // Heuristic 2: Prefer moves that satisfy numbered wall constraints
        score += evaluateConstraintSatisfaction(board, r, c) * 20;

        // Heuristic 3: Avoid moves that block many future possibilities
        int blockedMoves = countBlockedFutureMoves(board, r, c);
        score -= blockedMoves * 5;

        // Heuristic 4: Prefer cells with higher "forced move" potential
        if (isForcedMove(board, r, c)) {
            score += 15;
        }

        // Heuristic 5: Prefer moves that illuminate isolated cells
        if (illuminatesIsolatedCell(board, r, c)) {
            score += 25;
        }

        return Math.max(0, score);
    }

    /**
     * Count how many white cells would be illuminated by placing a light at (r, c).
     */
    private int countIlluminatedCells(GameBoard board, int r, int c) {
        int count = 0;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Count the cell itself
        if (!isIlluminated(board, r, c)) {
            count++;
        }

        // Count cells in all four directions
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize() 
                    && board.getCellType(nr, nc) == '.') {
                if (!isIlluminated(board, nr, nc)) {
                    count++;
                }
                nr += d[0];
                nc += d[1];
            }
        }

        return count;
    }

    /**
     * Evaluate how well this move satisfies numbered wall constraints.
     */
    private int evaluateConstraintSatisfaction(GameBoard board, int r, int c) {
        int satisfaction = 0;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                char ch = board.getCellType(nr, nc);
                if (ch >= '0' && ch <= '4') {
                    int required = ch - '0';
                    int current = countAdjacentLights(board, nr, nc);
                    
                    // If this move helps satisfy a constraint, give bonus points
                    if (current < required) {
                        satisfaction += (required - current);
                    }
                    
                    // If this move exactly satisfies a constraint, give big bonus
                    if (current + 1 == required) {
                        satisfaction += 10;
                    }
                }
            }
        }

        return satisfaction;
    }

    /**
     * Count how many future moves would be blocked by placing a light at (r, c).
     */
    private int countBlockedFutureMoves(GameBoard board, int r, int c) {
        int blocked = 0;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Temporarily place the light
        board.placeLight(r, c);

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize() 
                    && board.getCellType(nr, nc) == '.') {
                if (!board.hasLightAt(nr, nc) && !board.isMarkedAt(nr, nc)) {
                    if (rules.isPlacementAllowed(board, nr, nc)) {
                        blocked++;
                    }
                }
                nr += d[0];
                nc += d[1];
            }
        }

        // Remove the temporary light
        board.removeLight(r, c);

        return blocked;
    }

    /**
     * Check if this is a forced move (only one valid position for a constraint).
     */
    private boolean isForcedMove(GameBoard board, int r, int c) {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                char ch = board.getCellType(nr, nc);
                if (ch >= '0' && ch <= '4') {
                    int required = ch - '0';
                    int current = countAdjacentLights(board, nr, nc);
                    int needed = required - current;

                    if (needed > 0) {
                        List<Point> available = getAvailableSpotsForConstraint(board, new Point(nr, nc));
                        if (available.size() == needed && available.contains(new Point(r, c))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if this move illuminates an isolated cell (cell that can only be lit from one position).
     */
    private boolean illuminatesIsolatedCell(GameBoard board, int r, int c) {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize() 
                    && board.getCellType(nr, nc) == '.') {
                if (!isIlluminated(board, nr, nc)) {
                    List<Point> candidates = getValidPositionsThatCanLight(board, nr, nc);
                    if (candidates.size() == 1 && candidates.get(0).equals(new Point(r, c))) {
                        return true;
                    }
                }
                nr += d[0];
                nc += d[1];
            }
        }

        return false;
    }

    private List<Point> getAvailableSpotsForConstraint(GameBoard board, Point constraint) {
        List<Point> available = new ArrayList<>();
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = constraint.x + d[0];
            int nc = constraint.y + d[1];
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                if (board.getCellType(nr, nc) == '.' && !board.hasLightAt(nr, nc) && !board.isMarkedAt(nr, nc)) {
                    if (rules.isPlacementAllowed(board, nr, nc)) {
                        available.add(new Point(nr, nc));
                    }
                }
            }
        }
        return available;
    }

    private List<Point> getValidPositionsThatCanLight(GameBoard board, int targetR, int targetC) {
        List<Point> candidates = new ArrayList<>();

        // Spot itself
        if (rules.isPlacementAllowed(board, targetR, targetC)) {
            candidates.add(new Point(targetR, targetC));
        }

        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = targetR + d[0];
            int nc = targetC + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()
                    && board.getCellType(nr, nc) == '.') {
                if (rules.isPlacementAllowed(board, nr, nc)) {
                    candidates.add(new Point(nr, nc));
                }
                nr += d[0];
                nc += d[1];
            }
        }
        return candidates;
    }

    private boolean isIlluminated(GameBoard board, int r, int c) {
        if (board.hasLightAt(r, c))
            return true;
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()
                    && board.getCellType(nr, nc) == '.') {
                if (board.hasLightAt(nr, nc))
                    return true;
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
                if (board.hasLightAt(nr, nc))
                    count++;
            }
        }
        return count;
    }

    /**
     * Helper class to represent a move candidate with its score.
     */
    private static class MoveCandidate {
        final Point position;
        final int score;

        MoveCandidate(Point position, int score) {
            this.position = position;
            this.score = score;
        }
    }
}
