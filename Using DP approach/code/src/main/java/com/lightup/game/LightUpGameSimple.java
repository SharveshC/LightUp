package com.lightup.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LightUpGameSimple extends JFrame {
    // UI Components
    private JButton[][] buttons = new JButton[7][7];
    private JLabel statusLabel;
    private JButton undoButton;
    private JLabel timerLabel;
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Game Components
    private GameBoard gameBoard;
    private GameRules gameRules;
    private AIPlayer dpAIPlayer;
    private DACAIPlayer dacAIPlayer;
    private GreedyAIPlayer greedyAIPlayer;
    private GameTimer gameTimer;
    private UIComponents uiComponents;
    
    // Current AI algorithm
    private String currentAlgorithm = "GREEDY";

    // Game State
    private boolean playerTurn = true;
    private boolean awaitingComputer;

    private static final String[] DEFAULT_LAYOUT = {
            ".......",
            ".2...1.",
            "...2...",
            "..4.3..",
            "...#...",
            ".1...0.",
            "......."
    };

    public LightUpGameSimple() {
        super("Light Up - Cooperative Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize components
        gameBoard = new GameBoard(DEFAULT_LAYOUT);
        gameRules = new GameRules();
        dpAIPlayer = new AIPlayer(gameRules);
        dacAIPlayer = new DACAIPlayer(gameRules);
        greedyAIPlayer = new GreedyAIPlayer(gameRules);
        uiComponents = new UIComponents();

        // Initialize UI
        initializeUI();

        pack();
        setLocationRelativeTo(null);

        System.out.println("Cooperative game with smart AI created! (Refactored V4)");

        // Show Rules First
        cardLayout.show(mainContainer, "RULES");
    }
    
    private void showAlgorithmSelection() {
        cardLayout.show(mainContainer, "ALGORITHM");
    }
    
    private void selectDP() {
        currentAlgorithm = "DP";
        System.out.println("Selected Dynamic Programming algorithm");
        startGame();
    }
    
    private void selectDAC() {
        currentAlgorithm = "DAC";
        System.out.println("Selected Divide and Conquer algorithm");
        startGame();
    }
    
    private void selectGreedy() {
        currentAlgorithm = "GREEDY";
        System.out.println("Selected Greedy algorithm");
        startGame();
    }

    private void initializeUI() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Create timer label
        timerLabel = new JLabel("Time: 0s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        timerLabel.setForeground(new Color(50, 50, 50));

        // Create timer
        gameTimer = new GameTimer(timerLabel);

        // Create status label
        statusLabel = new JLabel("Your turn - place the first light", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(0, 102, 204));

        // Create control buttons
        undoButton = new JButton("Undo");
        undoButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        undoButton.setBackground(new Color(220, 220, 220));
        undoButton.setFocusPainted(false);
        undoButton.addActionListener(e -> requestUndo());

        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newGameButton.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.addActionListener(e -> startNewGame());

        // Create panels using UIComponents
        JPanel rulesPanel = uiComponents.createRulesPanel(this::showAlgorithmSelection);
        JPanel algorithmPanel = uiComponents.createAlgorithmSelectionPanel(
            this::selectDAC, this::selectDP, this::selectGreedy);
        JPanel gamePanel = uiComponents.createGamePanel(
                gameBoard, buttons, createCellClickHandler(),
                statusLabel, undoButton, newGameButton, timerLabel); // Pass both buttons

        mainContainer.add(rulesPanel, "RULES");
        mainContainer.add(algorithmPanel, "ALGORITHM");
        mainContainer.add(gamePanel, "GAME");

        add(mainContainer);
    }

    private void startNewGame() {
        gameBoard.reset();
        gameTimer.reset();
        gameTimer.start();
        playerTurn = true;
        awaitingComputer = false;
        undoButton.setEnabled(true);
        statusLabel.setText("New Game Started! Your turn.");
        statusLabel.setForeground(new Color(0, 102, 204));
        uiComponents.updateDisplay(gameBoard, gameRules, buttons);
    }

    private MouseAdapter createCellClickHandler() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Find which button was clicked
                for (int r = 0; r < 7; r++) {
                    for (int c = 0; c < 7; c++) {
                        if (e.getSource() == buttons[r][c]) {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                playerClick(r, c);
                            } else if (e.getButton() == MouseEvent.BUTTON3) {
                                rightClick(r, c);
                            }
                            return;
                        }
                    }
                }
            }
        };
    }

    private void startGame() {
        cardLayout.show(mainContainer, "GAME");
        SwingUtilities.invokeLater(() -> {
            pack();
            setLocationRelativeTo(null);
        });

        gameTimer.start();
    }

    private void handleGameOver() {
        gameTimer.stop();

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Game is done by user and computer!\nTaken time: " + gameTimer.getElapsedSeconds() + "s",
                    "CONGRATULATIONS!",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void playerClick(int r, int c) {
        if (!playerTurn || gameBoard.hasLightAt(r, c) || gameBoard.isMarkedAt(r, c)) {
            return;
        }

        // Validate placement
        if (!gameRules.isPlacementAllowed(gameBoard, r, c)) {
            if (gameRules.wouldConflictWithExistingLight(gameBoard, r, c)) {
                uiComponents.flashInvalidLight(gameBoard, buttons, r, c,
                        "Lights can't see each other", statusLabel, () -> {
                            statusLabel.setText("Your turn - place a light");
                            statusLabel.setForeground(Color.BLUE);
                        });
            } else {
                statusLabel.setText("Cannot place light! Number constraint blocked.");
                statusLabel.setForeground(Color.RED);
            }
            return;
        }

        // Place the light
        gameBoard.placeLight(r, c);
        awaitingComputer = true;
        uiComponents.updateDisplay(gameBoard, gameRules, buttons);

        // Check win
        if (gameRules.checkWin(gameBoard, buttons)) {
            statusLabel.setText("YOU AND COMPUTER WON!");
            statusLabel.setForeground(Color.GREEN);
            handleGameOver();
            return;
        }

        // Computer's turn
        playerTurn = false;
        statusLabel.setText("Computer is thinking...");
        statusLabel.setForeground(Color.RED);
        undoButton.setEnabled(false);

        Timer timer = new Timer(1500, e -> {
            makeComputerMove();
            uiComponents.updateDisplay(gameBoard, gameRules, buttons);
            undoButton.setEnabled(true);
            awaitingComputer = false;

            if (gameRules.checkWin(gameBoard, buttons)) {
                statusLabel.setText("YOU AND COMPUTER WON!");
                statusLabel.setForeground(Color.GREEN);
                handleGameOver();
            } else {
                playerTurn = true;
                statusLabel.setText("Your turn - place a light");
                statusLabel.setForeground(Color.BLUE);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void rightClick(int r, int c) {
        if (!playerTurn || gameBoard.hasLightAt(r, c)) {
            return;
        }
        gameBoard.toggleMark(r, c);
        uiComponents.updateDisplay(gameBoard, gameRules, buttons);
    }

    private void requestUndo() {
        if (!playerTurn || !gameBoard.canUndo()) {
            return;
        }

        // Undo moves
        gameBoard.undo(!awaitingComputer);
        awaitingComputer = false;

        uiComponents.updateDisplay(gameBoard, gameRules, buttons);
        statusLabel.setText("Undid last move. Your turn!");
        statusLabel.setForeground(Color.BLUE);

        // Force refresh
        mainContainer.repaint();
    }

    private void makeComputerMove() {
        Point bestMove = null;
        
        // Use the selected algorithm
        switch (currentAlgorithm) {
            case "DAC":
                bestMove = dacAIPlayer.findBestMove(gameBoard);
                break;
            case "DP":
                bestMove = dpAIPlayer.findBestMove(gameBoard);
                break;
            case "GREEDY":
                bestMove = greedyAIPlayer.findBestMove(gameBoard);
                break;
        }

        if (bestMove != null) {
            // Safety Check: Only place if the move is actually legal
            if (gameRules.isPlacementAllowed(gameBoard, bestMove.x, bestMove.y)) {
                gameBoard.placeLight(bestMove.x, bestMove.y);
            } else {
                System.out
                        .println("AI suggested an illegal move at (" + bestMove.x + "," + bestMove.y + "). Skipping.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LightUpGameSimple().setVisible(true);
        });
    }
}
