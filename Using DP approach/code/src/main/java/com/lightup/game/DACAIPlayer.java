package com.lightup.game;

import java.awt.Point;
import java.util.*;

/**
 * AI Player using Divide and Conquer algorithm.
 */
public class DACAIPlayer {
    private final GameRules rules;

    public DACAIPlayer(GameRules rules) {
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

        BoardDivider divider = new BoardDivider();
        List<BoardDivider.Region> independentRegions = divider.divideBoard(board);
        System.out.println("Found " + independentRegions.size() + " independent regions.");

        long startTime = System.currentTimeMillis();

        // Use divide-and-conquer to find logically forced moves
        List<Point> solution = solveDivideAndConquer(board);

        // Return ONLY the first move from the solution path
        Point move = (solution != null && !solution.isEmpty()) ? solution.get(0) : null;

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI found move in " + duration + "ms");

        return move;
    }

    /**
     * Divide and Conquer:
     * DIVIDE: Split board into independent regions
     * CONQUER: Solve each region deterministically (only forced moves)
     * COMBINE: Merge all region solutions
     */
    private List<Point> solveDivideAndConquer(GameBoard board) {
        BoardDivider divider = new BoardDivider();
        List<BoardDivider.Region> regions = divider.divideBoard(board);

        List<Point> completeSolution = new ArrayList<>();

        for (BoardDivider.Region region : regions) {
            List<Point> regionSolution = solveRegionDeterministically(board, region);
            if (regionSolution != null) {
                completeSolution.addAll(regionSolution);
            }
        }

        return completeSolution;
    }

    /**
     * Solve a single region using deterministic logic only.
     */
    private List<Point> solveRegionDeterministically(GameBoard board, BoardDivider.Region region) {
        GameBoard regionBoard = board.copy();
        List<Point> solution = new ArrayList<>();

        // Convert Set to List and sort using Merge Sort (DAC)
        // Priority: Higher numbered walls (4, 3, 2, 1, 0)
        List<Point> sortedConstraints = new ArrayList<>(region.constraints);
        sortedConstraints = mergeSortConstraints(sortedConstraints, regionBoard);

        boolean changed = true;
        while (changed) {
            changed = false;

            // Rule 1: Numbered walls with exactly N available spots must be filled
            for (Point constraint : sortedConstraints) {
                char val = regionBoard.getCellType(constraint.x, constraint.y);
                if (val >= '0' && val <= '4') {
                    int required = val - '0';
                    int current = countAdjacentLights(regionBoard, constraint.x, constraint.y);
                    int needed = required - current;

                    if (needed > 0) {
                        List<Point> available = getAvailableSpotsForConstraint(regionBoard, constraint);
                        if (available.size() == needed) {
                            for (Point p : available) {
                                regionBoard.placeLight(p.x, p.y);
                                solution.add(p);
                                changed = true;
                            }
                        }
                    }
                }
            }

            // Rule 2: White cells that can only be lit by ONE spot must have a light there
            for (Point whiteCell : region.whiteCells) {
                if (!isIlluminated(regionBoard, whiteCell.x, whiteCell.y)) {
                    List<Point> candidates = getValidPositionsThatCanLight(regionBoard, whiteCell.x, whiteCell.y);
                    if (candidates.size() == 1) {
                        Point forced = candidates.get(0);
                        regionBoard.placeLight(forced.x, forced.y);
                        solution.add(forced);
                        changed = true;
                    }
                }
            }
        }

        return solution;
    }

    /**
     * Sorts constraints using Merge Sort (Divide and Conquer).
     * Priority given to bigger numbers (4 > 3 > 2 > 1 > 0).
     */
    private List<Point> mergeSortConstraints(List<Point> constraints, GameBoard board) {
        if (constraints.size() <= 1) {
            return constraints;
        }

        // DIVIDE
        int mid = constraints.size() / 2;
        List<Point> left = new ArrayList<>(constraints.subList(0, mid));
        List<Point> right = new ArrayList<>(constraints.subList(mid, constraints.size()));

        // CONQUER
        left = mergeSortConstraints(left, board);
        right = mergeSortConstraints(right, board);

        // COMBINE
        return mergeConstraints(left, right, board);
    }

    private List<Point> mergeConstraints(List<Point> left, List<Point> right, GameBoard board) {
        List<Point> merged = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            int valLeft = board.getCellType(left.get(i).x, left.get(i).y) - '0';
            int valRight = board.getCellType(right.get(j).x, right.get(j).y) - '0';

            // Descending order: Higher number first
            if (valLeft >= valRight) {
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
}
