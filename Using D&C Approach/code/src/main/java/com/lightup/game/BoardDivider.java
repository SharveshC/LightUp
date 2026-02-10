package com.lightup.game;

import java.awt.Point;
import java.util.*;

/**
 * Implements the "Divide" step of the Divide-and-Conquer strategy for Light Up.
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
     * Divides the board into independent regions.
     */
    public List<Region> divideBoard(GameBoard board) {
        int size = board.getGridSize();
        // 1. Identify initial connected components of white cells
        // Using a grid to map each cell to a Region ID
        int[][] regionMap = new int[size][size];
        for (int[] row : regionMap)
            Arrays.fill(row, -1);

        Map<Integer, Set<Point>> initialComponents = new HashMap<>(); // ID -> Component
        int nextRegionId = 0;

        // BFS to find all connected components of white cells
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board.getCellType(r, c) == '.' && regionMap[r][c] == -1) {
                    Set<Point> component = getConnectedComponent(board, r, c, regionMap, nextRegionId);
                    initialComponents.put(nextRegionId, component);
                    nextRegionId++;
                }
            }
        }

        // 2. Union-Find to merge regions connected by numbered walls
        DisjointSet dsu = new DisjointSet(nextRegionId);

        // Iterate over all numbered walls
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                char ch = board.getCellType(r, c);
                if (ch >= '0' && ch <= '4') {
                    // Check logic: A numbered wall connects ALL regions adjacent to it.
                    // Collect all unique region IDs adjacent to this wall.
                    Set<Integer> adjacentRegionIds = new HashSet<>();
                    int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

                    for (int[] d : dirs) {
                        int nr = r + d[0];
                        int nc = c + d[1];
                        if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                            // If it's a white cell, it belongs to a region
                            if (board.getCellType(nr, nc) == '.') {
                                int regionId = regionMap[nr][nc];
                                if (regionId != -1) {
                                    adjacentRegionIds.add(dsu.find(regionId));
                                }
                            }
                        }
                    }

                    // Merge all these regions together because they are coupled by this wall
                    if (adjacentRegionIds.size() > 1) {
                        Iterator<Integer> it = adjacentRegionIds.iterator();
                        int first = it.next();
                        while (it.hasNext()) {
                            dsu.union(first, it.next());
                        }
                    }
                }
            }
        }

        // 3. Construct final Regions
        Map<Integer, Region> finalRegions = new HashMap<>();

        // Add white cells to their final parent region
        for (Map.Entry<Integer, Set<Point>> entry : initialComponents.entrySet()) {
            int originalId = entry.getKey();
            int rootId = dsu.find(originalId);

            Region region = finalRegions.computeIfAbsent(rootId, k -> new Region());
            region.whiteCells.addAll(entry.getValue());
        }

        // Add numbered wall constraints to their final parent region
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                char ch = board.getCellType(r, c);
                if (ch >= '0' && ch <= '4') {
                    // Find which region this constraint belongs to (via any adjacent white cell)
                    int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

                    for (int[] d : dirs) {
                        int nr = r + d[0];
                        int nc = c + d[1];
                        if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                            if (board.getCellType(nr, nc) == '.') {
                                int regionId = regionMap[nr][nc];
                                if (regionId != -1) {
                                    int rootId = dsu.find(regionId);
                                    Region region = finalRegions.get(rootId);
                                    if (region != null) {
                                        region.addConstraint(new Point(r, c));
                                        break; // Only need to add it once
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(finalRegions.values());
    }

    /**
     * Performs BFS/Flood Fill to find a connected component of white cells starting
     * at (startR, startC).
     */
    private Set<Point> getConnectedComponent(GameBoard board, int startR, int startC, int[][] regionMap, int id) {
        Set<Point> component = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();

        Point start = new Point(startR, startC);
        queue.add(start);
        regionMap[startR][startC] = id;
        component.add(start);

        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        while (!queue.isEmpty()) {
            Point p = queue.poll();

            for (int[] d : dirs) {
                int nr = p.x + d[0];
                int nc = p.y + d[1];

                // Check bounds
                if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                    // Check if it's a white cell and not visited yet
                    if (board.getCellType(nr, nc) == '.' && regionMap[nr][nc] == -1) {
                        regionMap[nr][nc] = id;
                        Point next = new Point(nr, nc);
                        component.add(next);
                        queue.add(next);
                    }
                }
            }
        }
        return component;
    }

    // Helper Disjoint Set (Union-Find) class
    private static class DisjointSet {
        private int[] parent;

        public DisjointSet(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        public int find(int i) {
            if (parent[i] == i)
                return i;
            return parent[i] = find(parent[i]); // Path compression
        }

        public void union(int i, int j) {
            int rootI = find(i);
            int rootJ = find(j);
            if (rootI != rootJ) {
                parent[rootI] = rootJ;
            }
        }
    }
}
