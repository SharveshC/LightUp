package com.lightup.game;

import java.awt.Point;
import java.util.*;


public class DACAIPlayer {
    private final GameRules rules;

    // Toggle between BFS and DFS for demonstration
    private boolean useBFS = true;

    public DACAIPlayer(GameRules rules) {
        this.rules = rules;
    }

    /**
     * Finds the best move for the AI player using Divide and Conquer approach.
     * 
     * @return Point representing the best move, or null if no valid moves exist
     */
    public Point findBestMove(GameBoard board) {
        System.out.println("AI thinking using Divide and Conquer (" + (useBFS ? "BFS" : "DFS") + ")...");
        long startTime = System.currentTimeMillis();

        Point move = useBFS ? solveBFS(board) : solveDFS(board);

        // Toggle strategy for next turn to show both work (or could be config)
        // For a real solver, BFS is usually preferred for optimality (shortest path),
        // but DFS uses less memory.
        useBFS = !useBFS;

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI found move in " + duration + "ms");

        return move;
    }

    /**
     * Represents a state in the search space.
     */
    private class Node {
        GameBoard board;
        Point firstMove; // The move that started this specific branch from the root

        Node(GameBoard board, Point firstMove) {
            this.board = board;
            this.firstMove = firstMove; // If null, this is root
        }
    }

    /**
     * Implements Breadth-First Search to find a winning move.
     */
    private Point solveBFS(GameBoard rootBoard) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(new Node(rootBoard.copy(), null));

        int nodesExplored = 0;
        // Limit search to avoid hanging UI on complex boards
        int MAX_NODES = 10000;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            nodesExplored++;

            if (nodesExplored > MAX_NODES) {
                System.out.println("BFS limit reached.");
                break;
            }
            List<Point> legalMoves = getAllLegalMoves(current.board);

            if (legalMoves.isEmpty()) {

                if (current.firstMove != null) {
                    
                    if (isSolved(current.board)) {
                        return current.firstMove;
                    }
                }
            }

            // Sort moves to pick better ones first (Optimizing Search)
            sortMoves(current.board, legalMoves);

            for (Point move : legalMoves) {
                GameBoard nextBoard = current.board.copy();
                nextBoard.placeLight(move.x, move.y);

                Point nextFirstMove = (current.firstMove == null) ? move : current.firstMove;

                // Early exit: if this move effectively solves the board?
                if (isSolved(nextBoard)) {
                    return nextFirstMove;
                }

                queue.add(new Node(nextBoard, nextFirstMove));
            }
        }

    
        List<Point> rootMoves = getAllLegalMoves(rootBoard);
        if (!rootMoves.isEmpty()) {
            sortMoves(rootBoard, rootMoves);
            return rootMoves.get(0);
        }
        return null;
    }

    /**
     * Implements Depth-First Search to find a winning move.
     */
    private Point solveDFS(GameBoard rootBoard) {
        Stack<Node> stack = new Stack<>();
        stack.push(new Node(rootBoard.copy(), null));

        int nodesExplored = 0;
        int MAX_NODES = 10000;

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            nodesExplored++;

            if (nodesExplored > MAX_NODES)
                break;

            if (isSolved(current.board)) {
                return current.firstMove;
            }

            List<Point> legalMoves = getAllLegalMoves(current.board);

          
            sortMoves(current.board, legalMoves);
            // Reverse to process 'best' move first (since stack is LIFO)
            Collections.reverse(legalMoves);

            for (Point move : legalMoves) {
                GameBoard nextBoard = current.board.copy();
                nextBoard.placeLight(move.x, move.y);

                Point nextFirstMove = (current.firstMove == null) ? move : current.firstMove;
                stack.push(new Node(nextBoard, nextFirstMove));
            }
        }

        // Fallback
        List<Point> rootMoves = getAllLegalMoves(rootBoard);
        if (!rootMoves.isEmpty()) {
            sortMoves(rootBoard, rootMoves);
            return rootMoves.get(0);
        }
        return null;
    }


    private boolean isSolved(GameBoard board) {
        // Quick check: are there any white cells NOT lit?
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                if (board.getCellType(r, c) == '.' && !board.hasLightAt(r, c)) {
                    // Check if it is illuminated by another light
                    // (But hasLightAt only checks if THERE IS a light there, not if lit)
                    // We need to check illumination.
                    if (!isIlluminated(board, r, c)) {
                        return false;
                    }
                }
            }
        }
       
        return true;
    }

    private boolean isIlluminated(GameBoard board, int r, int c) {
        if (board.hasLightAt(r, c))
            return true;
        // Check 4 directions
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                if (board.getCellType(nr, nc) != '.')
                    break; // Blocked by wall
                if (board.hasLightAt(nr, nc))
                    return true;
                nr += d[0];
                nc += d[1];
            }
        }
        return false;
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

   
    private void sortMoves(GameBoard board, List<Point> moves) {
        moves.sort((p1, p2) -> {
            int score1 = calculateHeuristicScore(board, p1);
            int score2 = calculateHeuristicScore(board, p2);
            // Sort descending (higher score first)
            return Integer.compare(score2, score1);
        });
    }

    private int calculateHeuristicScore(GameBoard board, Point p) {
        int score = 0;

        // Temporarily place light
        board.placeLight(p.x, p.y);

        
        board.removeLight(p.x, p.y); // Reset immediately

    
        score += getNumberAdjacencyScore(board, p.x, p.y) * 10; // High weight

        // Count illumination potential (Stateless)
        score += countIlluminated(board, p.x, p.y);

        return score;
    }

    private int getNumberAdjacencyScore(GameBoard board, int r, int c) {
        int score = 0;
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                char ch = board.getCellType(nr, nc);
                if (ch >= '0' && ch <= '4') {
                   
                    score += 5;

                    
                    int num = ch - '0';
                    score += num;
                }
            }
        }
        return score;
    }

    private int countIlluminated(GameBoard board, int r, int c) {
        int count = 0;
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            while (nr >= 0 && nr < board.getGridSize() && nc >= 0 && nc < board.getGridSize()) {
                if (board.getCellType(nr, nc) != '.')
                    break; // Blocked
                if (!isIlluminated(board, nr, nc))
                    count++; // Only count meaningful illumination
                nr += d[0];
                nc += d[1];
            }
        }
        return count;
    }
}
