package com.lightup.game;

import java.awt.Point;
import java.util.*;

/**
 * Implements board division using Dynamic Programming and Backtracking.
 * Splits the board into independent regions that can be solved separately.
 */
public class BoardDivider {

    /**
     * Represents an independent region of the board.
     */
    public static class Region {
        public final Set<Point> whiteCells = new HashSet<>();
        public final Set<Point> constraints = new HashSet<>();

        public void addWhiteCell(Point p) {
            whiteCells.add(p);
        }

        public void addConstraint(Point p) {
            constraints.add(p);
        }

        @Override
        public String toString() {
            return "Region{whiteCells=" + whiteCells.size() + ", constraints=" + constraints.size() + "}";
        }
    }

    /**
     * Divides the board into independent regions using DP and backtracking.
     */
    public List<Region> divideBoard(GameBoard board) {
        int size = board.getGridSize();
        boolean[][] visited = new boolean[size][size];
        List<Region> regions = new ArrayList<>();
        
        // Use backtracking to find all connected components
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board.getCellType(r, c) == '.' && !visited[r][c]) {
                    Set<Point> component = new HashSet<>();
                    backtrackFindComponent(board, r, c, visited, component);
                    
                    Region region = new Region();
                    region.whiteCells.addAll(component);
                    
                    // Add constraints for this region
                    addConstraintsToRegion(board, region, component);
                    regions.add(region);
                }
            }
        }
        
        // Use DP to merge regions connected by numbered walls
        return mergeRegionsWithDP(board, regions);
    }
    
    /**
     * Backtracking approach to find connected components.
     */
    private void backtrackFindComponent(GameBoard board, int r, int c, boolean[][] visited, Set<Point> component) {
        if (r < 0 || r >= board.getGridSize() || c < 0 || c >= board.getGridSize()) {
            return;
        }
        
        if (visited[r][c] || board.getCellType(r, c) != '.') {
            return;
        }
        
        visited[r][c] = true;
        component.add(new Point(r, c));
        
        // Explore all four directions
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            backtrackFindComponent(board, r + d[0], c + d[1], visited, component);
        }
    }
    
    /**
     * Add numbered wall constraints to regions.
     */
    private void addConstraintsToRegion(GameBoard board, Region region, Set<Point> whiteCells) {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (Point whiteCell : whiteCells) {
            for (int[] d : dirs) {
                int nr = whiteCell.x + d[0];
                int nc = whiteCell.y + d[1];
                
                if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                    char ch = board.getCellType(nr, nc);
                    if (ch >= '0' && ch <= '4') {
                        region.addConstraint(new Point(nr, nc));
                    }
                }
            }
        }
    }
    
    /**
     * Dynamic Programming approach to merge regions connected by numbered walls.
     */
    private List<Region> mergeRegionsWithDP(GameBoard board, List<Region> initialRegions) {
        // DP table: dp[i] represents the final region index for region i
        int[] dp = new int[initialRegions.size()];
        for (int i = 0; i < dp.length; i++) {
            dp[i] = i; // Initially, each region is its own
        }
        
        boolean changed = true;
        while (changed) {
            changed = false;
            
            // Check all numbered walls to see if they connect regions
            for (int r = 0; r < board.getGridSize(); r++) {
                for (int c = 0; c < board.getGridSize(); c++) {
                    char ch = board.getCellType(r, c);
                    if (ch >= '0' && ch <= '4') {
                        Set<Integer> adjacentRegions = findAdjacentRegions(board, r, c, initialRegions);
                        
                        if (adjacentRegions.size() > 1) {
                            // Merge all adjacent regions
                            int root = findRoot(dp, adjacentRegions.iterator().next());
                            for (int regionId : adjacentRegions) {
                                int currentRoot = findRoot(dp, regionId);
                                if (currentRoot != root) {
                                    dp[currentRoot] = root;
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Build final regions using DP results
        Map<Integer, Region> finalRegions = new HashMap<>();
        for (int i = 0; i < initialRegions.size(); i++) {
            int root = findRoot(dp, i);
            Region finalRegion = finalRegions.computeIfAbsent(root, k -> new Region());
            Region original = initialRegions.get(i);
            finalRegion.whiteCells.addAll(original.whiteCells);
            finalRegion.constraints.addAll(original.constraints);
        }
        
        return new ArrayList<>(finalRegions.values());
    }
    
    /**
     * Find regions adjacent to a numbered wall.
     */
    private Set<Integer> findAdjacentRegions(GameBoard board, int r, int c, List<Region> regions) {
        Set<Integer> adjacentRegions = new HashSet<>();
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                if (board.getCellType(nr, nc) == '.') {
                    // Find which region contains this white cell
                    for (int i = 0; i < regions.size(); i++) {
                        if (regions.get(i).whiteCells.contains(new Point(nr, nc))) {
                            adjacentRegions.add(i);
                            break;
                        }
                    }
                }
            }
        }
        
        return adjacentRegions;
    }
    
    /**
     * Find root with path compression (DP optimization).
     */
    private int findRoot(int[] dp, int i) {
        if (dp[i] == i) {
            return i;
        }
        return dp[i] = findRoot(dp, dp[i]);
    }
}
