package com.lightup.game;

import java.awt.Point;
import java.util.*;

public class AIPlayer {
    private final GameRules rules;

    public AIPlayer(GameRules rules) {
        this.rules = rules;
    }

    /**
     * Finds the best SINGLE move for the AI player using Dynamic Programming and Backtracking.
     * Solves the puzzle using backtracking and returns only the first move.
     * 
     * @return Point representing the best move, or null if no valid moves exist
     */
    public Point findBestMove(GameBoard board) {
        System.out.println("AI thinking using Dynamic Programming and Backtracking...");

        BoardDivider divider = new BoardDivider();
        List<BoardDivider.Region> independentRegions = divider.divideBoard(board);
        System.out.println("Found " + independentRegions.size() + " independent regions.");

        long startTime = System.currentTimeMillis();

        // Use backtracking with DP memoization to find solution
        List<Point> solution = solveWithBacktracking(board);

        // Return ONLY the first move from the solution path
        Point move = (solution != null && !solution.isEmpty()) ? solution.get(0) : null;
        
        // If DP solution fails, fall back to a simple greedy move
        if (move == null) {
            move = findSimpleMove(board);
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI found move in " + duration + "ms");

        return move;
    }

    /**
     * Dynamic Programming with Backtracking:
     * DP: Memoize solutions to subproblems
     * BACKTRACK: Try all possibilities and backtrack when stuck
     */
    private List<Point> solveWithBacktracking(GameBoard board) {
        BoardDivider divider = new BoardDivider();
        List<BoardDivider.Region> regions = divider.divideBoard(board);
        
        // DP memoization table
        Map<String, List<Point>> memo = new HashMap<>();
        
        List<Point> completeSolution = new ArrayList<>();
        
        for (BoardDivider.Region region : regions) {
            List<Point> regionSolution = solveRegionWithBacktracking(board, region, memo);
            if (regionSolution != null) {
                completeSolution.addAll(regionSolution);
            }
        }
        
        return completeSolution;
    }
    
    /**
     * Solve a single region using backtracking with DP memoization.
     */
    private List<Point> solveRegionWithBacktracking(GameBoard board, BoardDivider.Region region, Map<String, List<Point>> memo) {
        // Create DP key for memoization
        String boardKey = getBoardKey(board, region);
        if (memo.containsKey(boardKey)) {
            return memo.get(boardKey);
        }
        
        GameBoard regionBoard = board.copy();
        List<Point> solution = new ArrayList<>();
        
        // Sort constraints using DP
        List<Point> sortedConstraints = new ArrayList<>(region.constraints);
        sortedConstraints = dpSortConstraints(sortedConstraints, regionBoard);
        
        // Try to solve with backtracking
        if (backtrackSolve(regionBoard, sortedConstraints, region.whiteCells, solution, 0)) {
            memo.put(boardKey, new ArrayList<>(solution));
            return solution;
        }
        
        memo.put(boardKey, null);
        return null;
    }
    
    /**
     * Backtracking algorithm to solve the region.
     */
    private boolean backtrackSolve(GameBoard board, List<Point> constraints, Set<Point> whiteCells, List<Point> solution, int constraintIndex) {
        // Base case: All constraints processed
        if (constraintIndex >= constraints.size()) {
            // Check if all white cells are illuminated
            for (Point whiteCell : whiteCells) {
                if (!isIlluminated(board, whiteCell.x, whiteCell.y)) {
                    return false;
                }
            }
            return true;
        }
        
        Point constraint = constraints.get(constraintIndex);
        char val = board.getCellType(constraint.x, constraint.y);
        
        if (val >= '0' && val <= '4') {
            int required = val - '0';
            int current = countAdjacentLights(board, constraint.x, constraint.y);
            int needed = required - current;
            
            if (needed == 0) {
                // Constraint already satisfied, move to next
                return backtrackSolve(board, constraints, whiteCells, solution, constraintIndex + 1);
            } else if (needed > 0) {
                // Try all combinations of placing lights
                List<Point> available = getAvailableSpotsForConstraint(board, constraint);
                
                // Backtrack: try all subsets of available spots of size 'needed'
                return tryLightCombinations(board, available, needed, 0, new ArrayList<>(), constraints, whiteCells, solution, constraintIndex);
            }
        }
        
        return backtrackSolve(board, constraints, whiteCells, solution, constraintIndex + 1);
    }
    
    /**
     * Try all combinations of lights for a constraint.
     */
    private boolean tryLightCombinations(GameBoard board, List<Point> available, int needed, int start, List<Point> current, List<Point> constraints, Set<Point> whiteCells, List<Point> solution, int constraintIndex) {
        if (current.size() == needed) {
            // Place these lights and check
            for (Point p : current) {
                board.placeLight(p.x, p.y);
                solution.add(p);
            }
            
            boolean valid = true;
            // Check for light conflicts
            for (Point p : current) {
                if (seesOtherLight(board, p.x, p.y)) {
                    valid = false;
                    break;
                }
            }
            
            if (valid) {
                valid = backtrackSolve(board, constraints, whiteCells, solution, constraintIndex + 1);
            }
            
            // Backtrack: remove lights
            for (Point p : current) {
                board.removeLight(p.x, p.y);
                solution.remove(solution.size() - 1);
            }
            
            return valid;
        }
        
        for (int i = start; i < available.size(); i++) {
            current.add(available.get(i));
            if (tryLightCombinations(board, available, needed, i + 1, current, constraints, whiteCells, solution, constraintIndex)) {
                return true;
            }
            current.remove(current.size() - 1);
        }
        
        return false;
    }
    
    /**
     * Create a unique key for the board state for DP memoization.
     */
    private String getBoardKey(GameBoard board, BoardDivider.Region region) {
        StringBuilder key = new StringBuilder();
        for (Point constraint : region.constraints) {
            key.append(board.getCellType(constraint.x, constraint.y));
            key.append(countAdjacentLights(board, constraint.x, constraint.y));
        }
        return key.toString();
    }
    
    /**
     * Check if a light at position sees another light.
     */
    private boolean seesOtherLight(GameBoard board, int r, int c) {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize() && board.getCellType(nr, nc) == '.') {
                if (board.hasLightAt(nr, nc) && !(nr == r && nc == c)) {
                    return true;
                }
                nr += d[0];
                nc += d[1];
            }
        }
        return false;
    }

    /**
     * Sorts constraints using Dynamic Programming (Counting Sort approach).
     * Priority given to bigger numbers (4 > 3 > 2 > 1 > 0).
     * This is more efficient than merge sort for our limited range (0-4).
     */
    private List<Point> dpSortConstraints(List<Point> constraints, GameBoard board) {
        // Counting sort approach - O(n + k) where k=5 (numbers 0-4)
        List<List<Point>> buckets = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            buckets.add(new ArrayList<>());
        }
        
        // Distribute constraints into buckets based on their values
        for (Point constraint : constraints) {
            char val = board.getCellType(constraint.x, constraint.y);
            if (val >= '0' && val <= '4') {
                int bucketIndex = val - '0';
                buckets.get(bucketIndex).add(constraint);
            }
        }
        
        // Collect from buckets in descending order (4, 3, 2, 1, 0)
        List<Point> sorted = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            sorted.addAll(buckets.get(i));
        }
        
        return sorted;
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
     * Smart fallback move finder when DP fails.
     * Prioritizes moves that illuminate more cells and satisfy constraints.
     */
    private Point findSimpleMove(GameBoard board) {
        List<Point> candidates = new ArrayList<>();
        
        // Find all valid moves
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                if (board.getCellType(r, c) == '.' && 
                    !board.hasLightAt(r, c) && 
                    !board.isMarkedAt(r, c)) {
                    
                    if (rules.isPlacementAllowed(board, r, c)) {
                        candidates.add(new Point(r, c));
                    }
                }
            }
        }
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        // Score each candidate and pick the best one
        Point bestMove = candidates.get(0);
        int bestScore = -1;
        
        for (Point move : candidates) {
            int score = scoreMove(board, move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * Score a move based on how many cells it illuminates and constraints it satisfies.
     */
    private int scoreMove(GameBoard board, Point move) {
        int score = 0;
        
        // Count how many unlit white cells this would illuminate
        score += countUnlitCellsIlluminated(board, move.x, move.y) * 10;
        
        // Check if this satisfies numbered wall constraints
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] d : dirs) {
            int nr = move.x + d[0];
            int nc = move.y + d[1];
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                char ch = board.getCellType(nr, nc);
                if (ch >= '0' && ch <= '4') {
                    int required = ch - '0';
                    int current = countAdjacentLights(board, nr, nc);
                    if (current < required) {
                        score += 50; // High bonus for helping satisfy constraints
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * Count how many currently unlit cells would be illuminated by placing a light at (r,c).
     */
    private int countUnlitCellsIlluminated(GameBoard board, int r, int c) {
        int count = 0;
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        
        // Check the cell itself
        if (!isCurrentlyIlluminated(board, r, c)) {
            count++;
        }
        
        // Check all four directions
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize() &&
                   board.getCellType(nr, nc) == '.') {
                if (!isCurrentlyIlluminated(board, nr, nc)) {
                    count++;
                }
                nr += d[0];
                nc += d[1];
            }
        }
        
        return count;
    }
    
    /**
     * Check if a cell is currently illuminated by any existing light.
     */
    private boolean isCurrentlyIlluminated(GameBoard board, int r, int c) {
        if (board.hasLightAt(r, c)) {
            return true;
        }
        
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                if (board.getCellType(nr, nc) != '.') {
                    break; // Blocked by wall
                }
                if (board.hasLightAt(nr, nc)) {
                    return true;
                }
                nr += d[0];
                nc += d[1];
            }
        }
        return false;
    }
}
