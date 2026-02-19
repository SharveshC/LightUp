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

        // --- VERIFICATION: EXECUTE STRUCTURAL DIVIDE STEP ---
        BoardDivider divider = new BoardDivider();
        List<BoardDivider.Region> independentRegions = divider.divideBoard(board);
        System.out.println("--- STRUCTURAL DIVIDE ---");
        System.out.println("Found " + independentRegions.size() + " independent regions:");
        for (int i = 0; i < independentRegions.size(); i++) {
            System.out.println("Region " + (i + 1) + ": " + independentRegions.get(i));
        }
        System.out.println("-------------------------");
        // ----------------------------------------------------

        long startTime = System.currentTimeMillis();

        // Use pure divide-and-conquer to find the complete solution
        List<Point> solution = solveDivideAndConquerPure(board);

        // Return ONLY the first move from the solution path
        Point move = (solution != null && !solution.isEmpty()) ? solution.get(0) : null;

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI found move in " + duration + "ms");

        return move;
    }

    

    /**
     * Pure Divide and Conquer without backtracking.
     * DIVIDE: Split board into independent regions
     * CONQUER: Solve each region independently using deterministic approach
     * COMBINE: Merge all region solutions
     */
    private List<Point> solveDivideAndConquerPure(GameBoard board) {
        // DIVIDE: Split board into independent regions
        BoardDivider divider = new BoardDivider();
        List<BoardDivider.Region> regions = divider.divideBoard(board);
        
        List<Point> completeSolution = new ArrayList<>();
        
        // CONQUER: Solve each region independently
        for (BoardDivider.Region region : regions) {
            List<Point> regionSolution = solveRegionDeterministically(board, region);
            if (regionSolution == null) {
                return null; // Region unsolvable
            }
            completeSolution.addAll(regionSolution);
        }
        
        return completeSolution;
    }

    /**
     * Solve a single region using deterministic algorithm (no backtracking)
     */
    private List<Point> solveRegionDeterministically(GameBoard board, BoardDivider.Region region) {
        // Create sub-board for this region
        GameBoard regionBoard = extractSubBoard(board, region);
        
        // Use deterministic placement based on constraints
        List<Point> solution = new ArrayList<>();
        
        // Step 1: Handle numbered constraints first (deterministic)
        for (Point constraint : region.constraints) {
            char constraintValue = board.getCellType(constraint.x, constraint.y);
            int requiredLights = constraintValue - '0';
            
            List<Point> validPositions = getValidPositionsForConstraint(board, constraint);
            if (validPositions.size() == requiredLights) {
                // Only one way to satisfy this constraint
                for (Point pos : validPositions) {
                    if (regionBoard.getCellType(pos.x, pos.y) == '.' && 
                        !regionBoard.hasLightAt(pos.x, pos.y)) {
                        regionBoard.placeLight(pos.x, pos.y);
                        solution.add(pos);
                    }
                }
            }
        }
        
        // Step 2: Fill remaining unlit cells (deterministic placement)
        fillRemainingCells(regionBoard, solution);
        
        return solution;
    }

    /**
     * Fill remaining unlit cells using deterministic rules
     */
    private void fillRemainingCells(GameBoard regionBoard, List<Point> solution) {
        boolean changed = true;
        
        while (changed) {
            changed = false;
            
            // Find unlit cells
            for (int r = 0; r < regionBoard.getGridSize(); r++) {
                for (int c = 0; c < regionBoard.getGridSize(); c++) {
                    if (regionBoard.getCellType(r, c) == '.' && 
                        !regionBoard.hasLightAt(r, c) && 
                        !isIlluminated(regionBoard, r, c)) {
                        
                        // Find all positions that can light this cell
                        List<Point> candidates = getPositionsThatCanLight(regionBoard, r, c);
                        
                        if (candidates.size() == 1) {
                            // Only one position can light this cell - forced move
                            Point forced = candidates.get(0);
                            regionBoard.placeLight(forced.x, forced.y);
                            solution.add(forced);
                            changed = true;
                        } else if (candidates.size() > 1) {
                            // Choose the position that lights most other unlit cells
                            Point best = chooseBestPosition(regionBoard, candidates);
                            regionBoard.placeLight(best.x, best.y);
                            solution.add(best);
                            changed = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Extract sub-board containing only the region
     */
    private GameBoard extractSubBoard(GameBoard board, BoardDivider.Region region) {
        // Create a copy of the original board
        // We'll work with the full board but only modify cells in this region
        return board.copy();
    }

    /**
     * Get valid positions that can satisfy a numbered constraint
     */
    private List<Point> getValidPositionsForConstraint(GameBoard board, Point constraint) {
        List<Point> validPositions = new ArrayList<>();
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        
        for (int[] d : dirs) {
            int nr = constraint.x + d[0];
            int nc = constraint.y + d[1];
            
            if (nr >= 0 && nr < board.getGridSize() && 
                nc >= 0 && nc < board.getGridSize() &&
                board.getCellType(nr, nc) == '.' &&
                rules.isPlacementAllowed(board, nr, nc)) {
                validPositions.add(new Point(nr, nc));
            }
        }
        
        return validPositions;
    }

    /**
     * Get positions that can light a specific cell
     */
    private List<Point> getPositionsThatCanLight(GameBoard board, int targetR, int targetC) {
        List<Point> candidates = new ArrayList<>();
        
        // Check same row
        for (int c = 0; c < board.getGridSize(); c++) {
            if (c != targetC && board.getCellType(targetR, c) == '.' && 
                !board.hasLightAt(targetR, c) &&
                canLightPath(board, targetR, c, targetR, targetC)) {
                candidates.add(new Point(targetR, c));
            }
        }
        
        // Check same column
        for (int r = 0; r < board.getGridSize(); r++) {
            if (r != targetR && board.getCellType(r, targetC) == '.' && 
                !board.hasLightAt(r, targetC) &&
                canLightPath(board, r, targetC, targetR, targetC)) {
                candidates.add(new Point(r, targetC));
            }
        }
        
        return candidates;
    }

    /**
     * Check if a light at (fromR, fromC) can light (toR, toC)
     */
    private boolean canLightPath(GameBoard board, int fromR, int fromC, int toR, int toC) {
        if (fromR == toR) {
            // Same row
            int start = Math.min(fromC, toC) + 1;
            int end = Math.max(fromC, toC);
            for (int c = start; c < end; c++) {
                if (board.getCellType(fromR, c) != '.') {
                    return false;
                }
            }
            return true;
        } else if (fromC == toC) {
            // Same column
            int start = Math.min(fromR, toR) + 1;
            int end = Math.max(fromR, toR);
            for (int r = start; r < end; r++) {
                if (board.getCellType(r, fromC) != '.') {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Choose best position from candidates (deterministic selection)
     */
    private Point chooseBestPosition(GameBoard board, List<Point> candidates) {
        Point best = null;
        int maxLit = -1;
        
        for (Point candidate : candidates) {
            int litCount = countWouldIlluminate(board, candidate);
            if (litCount > maxLit) {
                maxLit = litCount;
                best = candidate;
            }
        }
        
        return best;
    }

    /**
     * Count how many cells a position would illuminate
     */
    private int countWouldIlluminate(GameBoard board, Point pos) {
        int count = 0;
        
        // Count in all four directions
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] d : dirs) {
            int r = pos.x + d[0];
            int c = pos.y + d[1];
            
            while (r >= 0 && r < board.getGridSize() && 
                   c >= 0 && c < board.getGridSize()) {
                if (board.getCellType(r, c) != '.') {
                    break;
                }
                if (!board.hasLightAt(r, c) && !isIlluminated(board, r, c)) {
                    count++;
                }
                r += d[0];
                c += d[1];
            }
        }
        
        return count;
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
        // Create the list of legal moves
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

        // SORT: Prioritize "better" moves to try first (heuristic)
        // Using Merge Sort (Divide and Conquer) instead of built-in sort
        moves = mergeSort(moves, board);

        return moves;
    }

    /**
     * Sorts the list of moves using Merge Sort (Divide and Conquer).
     */
    private List<Point> mergeSort(List<Point> list, GameBoard board) {
        if (list.size() <= 1) {
            return list;
        }

        // DIVIDE
        int mid = list.size() / 2;
        List<Point> left = new ArrayList<>(list.subList(0, mid));
        List<Point> right = new ArrayList<>(list.subList(mid, list.size()));

        // CONQUER
        left = mergeSort(left, board);
        right = mergeSort(right, board);

        // COMBINE
        return merge(left, right, board);
    }

    /**
     * Merges two sorted lists based on the heuristic score.
     */
    private List<Point> merge(List<Point> left, List<Point> right, GameBoard board) {
        List<Point> merged = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            int scoreLeft = getMoveScore(board, left.get(i));
            int scoreRight = getMoveScore(board, right.get(j));

            // Descending order: Higher score comes first
            if (scoreLeft >= scoreRight) {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }

        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }

        return merged;
    }

    /**
     * Calculates a heuristic score for a move.
     * Higher score = better move to try first.
     */
    private int getMoveScore(GameBoard board, Point p) {
        int score = 0;

        // Priority 1: Moves adjacent to unsatisfied numbered walls (4 > 3 > 2 > 1)
        score += getAdjacentWallPriority(board, p.x, p.y);

        // Priority 2: Moves that illuminate the most unlit white cells
        List<Point> illuminated = rules.getIlluminatedCells(board, p.x, p.y);
        score += illuminated.size();

        return score;
    }

    /**
     * Returns a priority score based on adjacent numbered walls.
     * 4 -> 4000
     * 3 -> 3000
     * 2 -> 2000
     * 1 -> 1000
     */
    private int getAdjacentWallPriority(GameBoard board, int r, int c) {
        int maxPriority = 0;
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            // Check bounds
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                char ch = board.getCellType(nr, nc);
                if (ch >= '1' && ch <= '4') { // '0' walls don't need lights
                    int required = ch - '0';
                    int current = rules.countAdjacentLights(board, nr, nc);

                    if (current < required) {
                        // Priority is based on the number itself (higher number = more constrained =
                        // higher priority)
                        int priority = (ch - '0') * 1000;
                        if (priority > maxPriority) {
                            maxPriority = priority;
                        }
                    }
                }
            }
        }
        return maxPriority;
    }
}
