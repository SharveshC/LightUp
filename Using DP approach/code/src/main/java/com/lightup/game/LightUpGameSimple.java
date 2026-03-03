package com.lightup.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

public class LightUpGameSimple extends JFrame {
    // UI Components
    private JButton[][] buttons = new JButton[7][7];
    private JLabel statusLabel;
    private JButton undoButton;
    private JButton newGameButton;
    private JLabel timerLabel;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JPanel gamePanel;

    // Game Components
    private GameBoard gameBoard;
    private GameRules gameRules;
    private AIPlayer dpAIPlayer;
    private DACAIPlayer dacAIPlayer;
    private GreedyAIPlayer greedyAIPlayer;
    private GameTimer gameTimer;
    private UIComponents uiComponents;
    
    // UI Components for selection
    private JComboBox<String> algorithmComboBox;
    private JComboBox<String> difficultyComboBox;

    // Game State
    private boolean playerTurn = true;
    private boolean awaitingComputer;
    private String currentDifficulty = "Medium";
    private String currentAlgorithm = "DP";
    private boolean gameOver;

    // Difficulty Levels with different board layouts
    private static final Map<String, String[]> DIFFICULTY_LAYOUTS = Map.of(
        "Easy", new String[]{
            ".......",
            "..1.1..",
            "...#...",
            ".#...#.",
            "...#...",
            "..2.0..",
            "......."
        },
        "Medium", new String[]{
            ".......",
            ".1...1.",
            "...#...",
            "..2.4..",
            "...#...",
            ".1...0.",
            "......."
        },
        "Hard", new String[]{
            ".......",
            ".3#.#1.",
            "..#....",
            ".#2.1#.",
            "....#..",
            ".1#.#2.",
            "......."
        }
    );

    public LightUpGameSimple() {
        setTitle("Light Up Game - Dark Theme");
        setSize(800, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(30, 30, 30));

        // Initialize components
        gameBoard = new GameBoard(DIFFICULTY_LAYOUTS.get(currentDifficulty));
        gameRules = new GameRules();
        dpAIPlayer = new AIPlayer(gameRules);
        dacAIPlayer = new DACAIPlayer(gameRules);
        greedyAIPlayer = new GreedyAIPlayer(gameRules);
        uiComponents = new UIComponents();

        // Initialize dropdowns
        algorithmComboBox = new JComboBox<>(new String[]{"DP", "Greedy", "DAC"});
        difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        algorithmComboBox.setSelectedItem(currentAlgorithm);
        difficultyComboBox.setSelectedItem(currentDifficulty);
        
        // Add listeners for dropdown changes
        algorithmComboBox.addActionListener(e -> {
            currentAlgorithm = (String) algorithmComboBox.getSelectedItem();
            System.out.println("Algorithm changed to: " + currentAlgorithm);
        });
        
        difficultyComboBox.addActionListener(e -> {
            String newDifficulty = (String) difficultyComboBox.getSelectedItem();
            if (!newDifficulty.equals(currentDifficulty)) {
                currentDifficulty = newDifficulty;
                System.out.println("Difficulty changed to: " + currentDifficulty);
                // Restart game with new difficulty
                restartWithNewDifficulty();
            }
        });

        // Initialize UI
        initializeUI();

        pack();
        setLocationRelativeTo(null);

        System.out.println("Light Up game with multiple algorithms created!");

        // Show Rules First
        cardLayout.show(mainContainer, "RULES");
    }

    private void showSetupScreen() {
        cardLayout.show(mainContainer, "SETUP");
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

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newGameButton.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.addActionListener(e -> startNewGame());

        // Create panels using UIComponents
        JPanel rulesPanel = uiComponents.createRulesPanel(this::showSetupScreen);
        JPanel setupPanel = uiComponents.createGameSetupPanel(this::startGame);
        gamePanel = uiComponents.createGamePanel(
                gameBoard, buttons, createCellClickHandler(),
                statusLabel, undoButton, newGameButton, timerLabel,
                algorithmComboBox, difficultyComboBox);

        mainContainer.add(rulesPanel, "RULES");
        mainContainer.add(setupPanel, "SETUP");
        mainContainer.add(gamePanel, "GAME");

        add(mainContainer);
    }

    private void restartWithNewDifficulty() {
        // Reset game with new difficulty
        gameBoard = new GameBoard(DIFFICULTY_LAYOUTS.get(currentDifficulty));
        buttons = new JButton[7][7];
        gameOver = false;

        // Rebuild the GAME panel so the grid uses the new board layout (and fresh listeners)
        JPanel newGamePanel = uiComponents.createGamePanel(
                gameBoard, buttons, createCellClickHandler(),
                statusLabel, undoButton, newGameButton, timerLabel,
                algorithmComboBox, difficultyComboBox);

        mainContainer.remove(gamePanel);
        gamePanel = newGamePanel;
        mainContainer.add(gamePanel, "GAME");

        gameTimer.reset();
        gameTimer.start();
        playerTurn = true;
        awaitingComputer = false;
        undoButton.setEnabled(true);
        statusLabel.setText("New difficulty: " + currentDifficulty + "! Your turn.");
        statusLabel.setForeground(new Color(0, 102, 204));
        uiComponents.updateDisplay(gameBoard, gameRules, buttons);

        cardLayout.show(mainContainer, "GAME");
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    private void startNewGame() {
        gameBoard.reset();
        gameTimer.reset();
        gameTimer.start();
        playerTurn = true;
        awaitingComputer = false;
        gameOver = false;
        undoButton.setEnabled(true);
        statusLabel.setText("New Game Started! Your turn.");
        statusLabel.setForeground(new Color(0, 102, 204));
        uiComponents.updateDisplay(gameBoard, gameRules, buttons);
    }

    private boolean checkForWinAndHandle() {
        if (gameOver) {
            System.out.println("[DEBUG] checkForWinAndHandle: already gameOver, returning true");
            return true;
        }
        if (gameRules.checkWin(gameBoard, buttons)) {
            System.out.println("[DEBUG] checkForWinAndHandle: checkWin returned true, setting gameOver=true and calling handleGameOver");
            gameOver = true;
            statusLabel.setText("YOU AND COMPUTER WON!");
            statusLabel.setForeground(Color.GREEN);
            handleGameOver();
            return true;
        } else {
            System.out.println("[DEBUG] checkForWinAndHandle: checkWin returned false, not triggering popup");
        }
        return false;
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
        System.out.println("[DEBUG] handleGameOver: entered");
        gameTimer.stop();

        SwingUtilities.invokeLater(() -> {
            System.out.println("[DEBUG] handleGameOver: inside invokeLater, about to show JOptionPane");
            JOptionPane.showMessageDialog(this,
                    "Game is done by user and computer!\nTaken time: " + gameTimer.getElapsedSeconds() + "s",
                    "CONGRATULATIONS!",
                    JOptionPane.INFORMATION_MESSAGE);
            System.out.println("[DEBUG] handleGameOver: JOptionPane.showMessageDialog completed");
        });
        System.out.println("[DEBUG] handleGameOver: invokeLater queued, exiting method");
    }

    private void playerClick(int r, int c) {
        if (gameOver || !playerTurn || gameBoard.hasLightAt(r, c) || gameBoard.isMarkedAt(r, c)) {
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

        if (checkForWinAndHandle()) {
            return;
        } else {
            String blocker = gameRules.getWinBlocker(gameBoard);
            if (blocker != null && blocker.startsWith("Number ")) {
                statusLabel.setText(blocker);
                statusLabel.setForeground(new Color(255, 140, 0));
            }
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

            if (checkForWinAndHandle()) {
                return;
            } else {
                playerTurn = true;
                String blocker = gameRules.getWinBlocker(gameBoard);
                if (blocker != null && blocker.startsWith("Number ")) {
                    statusLabel.setText(blocker);
                    statusLabel.setForeground(new Color(255, 140, 0));
                } else {
                    statusLabel.setText("Your turn - place a light");
                    statusLabel.setForeground(Color.BLUE);
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void rightClick(int r, int c) {
        if (gameOver || !playerTurn || gameBoard.hasLightAt(r, c)) {
            return;
        }
        gameBoard.toggleMark(r, c);
        uiComponents.updateDisplay(gameBoard, gameRules, buttons);
        checkForWinAndHandle();
    }

    private void requestUndo() {
        if (gameOver || !playerTurn || !gameBoard.canUndo()) {
            return;
        }

        // Undo moves
        gameBoard.undo(!awaitingComputer);
        awaitingComputer = false;

        uiComponents.updateDisplay(gameBoard, gameRules, buttons);
        statusLabel.setText("Undid last move. Your turn!");
        statusLabel.setForeground(Color.BLUE);

        checkForWinAndHandle();

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
            case "Greedy":
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
