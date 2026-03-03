package com.lightup.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Creates and manages all UI panels and components.
 */
public class UIComponents {

    /**
     * Creates the rules panel with game instructions.
     */
    public JPanel createRulesPanel(Runnable onStartGame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Rules & Regulations", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(50, 50, 50));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        panel.add(title, BorderLayout.NORTH);

        JTextArea rulesText = new JTextArea();
        rulesText.setText(
                "OBJECTIVE:\n" +
                        "The goal is to place lights (bulbs) on the grid so that every white square is illuminated. " +
                        "A square is illuminated if it contains a light or if a light shines on it horizontally or vertically.\n\n"
                        +

                        "LIGHT PLACEMENT RULES:\n" +
                        "• You can place lights in any empty white square.\n" +
                        "• Light beams travel in straight lines until they hit a black square or the edge of the board.\n"
                        +
                        "• PROHIBITED: Two lights cannot shine on each other. They must be blocked by a black square to coexist on the same row or column.\n\n"
                        +

                        "NUMBERED BLACK SQUARES:\n" +
                        "• Some black squares have numbers (0, 1, 2, 3, 4).\n" +
                        "• These numbers tell you EXACTLY how many lights must be placed adjacent (horizontally or vertically) to that square.\n"
                        +
                        "• Diagonal lights do not count.\n" +
                        "• Unnumbered black squares can have any number of lights around them.\n\n" +

                        "COOPERATIVE MODE:\n" +
                        "• You and the Computer are teammates!\n" +
                        "• You take turns placing one light at a time.\n" +
                        "• Help the computer by setting up good moves, and watch how it responds.\n\n" +

                        "HOW TO WIN:\n" +
                        "The game is won when:\n" +
                        "1. All white squares are illuminated.\n" +
                        "2. No lights are shining on each other.\n" +
                        "3. All numbered black square conditions are satisfied.");
        rulesText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        rulesText.setForeground(new Color(60, 60, 60));
        rulesText.setBackground(Color.WHITE);
        rulesText.setLineWrap(true);
        rulesText.setWrapStyleWord(true);
        rulesText.setEditable(false);
        rulesText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(rulesText);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startButton.setBackground(new Color(70, 130, 180));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.addActionListener(e -> onStartGame.run());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        buttonPanel.add(startButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a simple game setup panel without dropdowns (since they're now in the game screen).
     */
    public JPanel createGameSetupPanel(Runnable onStartGame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Game Setup", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(50, 50, 50));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        panel.add(title, BorderLayout.NORTH);

        // Selection panel
        JPanel selectionPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        // Algorithm selection
        JLabel algorithmLabel = new JLabel("Algorithm:");
        algorithmLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        algorithmLabel.setForeground(new Color(50, 50, 50));
        
        String[] algorithms = {"DP", "Greedy", "DAC"};
        algorithmComboBox = new JComboBox<>(algorithms);
        algorithmComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        algorithmComboBox.setBackground(Color.WHITE);

        // Difficulty selection
        JLabel difficultyLabel = new JLabel("Difficulty:");
        difficultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        difficultyLabel.setForeground(new Color(50, 50, 50));
        
        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyComboBox = new JComboBox<>(difficulties);
        difficultyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyComboBox.setBackground(Color.WHITE);

        selectionPanel.add(algorithmLabel);
        selectionPanel.add(algorithmComboBox);
        selectionPanel.add(difficultyLabel);
        selectionPanel.add(difficultyComboBox);

        panel.add(selectionPanel, BorderLayout.CENTER);

        // Start button
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startButton.setBackground(new Color(70, 130, 180));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.addActionListener(e -> onStartGame.run());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        buttonPanel.add(startButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the game panel with the grid and controls.
     */
    public JPanel createGamePanel(GameBoard board, JButton[][] buttons,
            MouseAdapter cellClickHandler,
            JLabel statusLabel, JButton undoButton,
            JButton newGameButton, JLabel timerLabel,
            JComboBox<String> algorithmComboBox, JComboBox<String> difficultyComboBox) {
        // Create the grid panel
        JPanel gridPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        gridPanel.setBackground(Color.LIGHT_GRAY);
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                final int row = r, col = c;
                char ch = board.getCellType(r, c);
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(50, 50));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
                btn.setOpaque(true);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createEmptyBorder());

                if (ch >= '0' && ch <= '4') {
                    btn.setBackground(Color.BLACK);
                    btn.setForeground(Color.WHITE);
                    btn.setText(String.valueOf(ch));
                    btn.setEnabled(false);
                } else if (ch == '#') {
                    btn.setBackground(Color.BLACK);
                    btn.setEnabled(false);
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            cellClickHandler.mouseClicked(e);
                        }

                        public void mouseEntered(MouseEvent e) {
                            if (btn.isEnabled() && !board.hasLightAt(row, col) &&
                                    !board.isMarkedAt(row, col) &&
                                    btn.getBackground().equals(Color.WHITE)) {
                                btn.setBackground(new Color(240, 240, 240));
                            }
                        }

                        public void mouseExited(MouseEvent e) {
                            if (btn.isEnabled() && !board.hasLightAt(row, col) &&
                                    !board.isMarkedAt(row, col) &&
                                    btn.getBackground().equals(new Color(240, 240, 240))) {
                                btn.setBackground(Color.WHITE);
                            }
                        }
                    });
                }

                buttons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        // Top controls with timer and dropdowns
        JPanel topControls = new JPanel(new BorderLayout());
        topControls.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Timer on the left
        topControls.add(timerLabel, BorderLayout.WEST);
        
        // Dropdowns on the right
        JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        algorithmPanel.add(new JLabel("Algorithm:"));
        algorithmPanel.add(algorithmComboBox);
        
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        difficultyPanel.add(new JLabel("Difficulty:"));
        difficultyPanel.add(difficultyComboBox);
        
        dropdownPanel.add(algorithmPanel);
        dropdownPanel.add(difficultyPanel);
        topControls.add(dropdownPanel, BorderLayout.EAST);

        // Bottom controls
        JPanel bottomControls = new JPanel(new BorderLayout());
        bottomControls.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(newGameButton);
        buttonPanel.add(undoButton);

        bottomControls.add(statusLabel, BorderLayout.CENTER);
        bottomControls.add(buttonPanel, BorderLayout.EAST);

        // Wrap grid in a container to center
        JPanel gridContainer = new JPanel(new GridBagLayout());
        gridContainer.add(gridPanel);

        // Main panel assembly
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topControls, BorderLayout.NORTH);
        mainPanel.add(gridContainer, BorderLayout.CENTER);
        mainPanel.add(bottomControls, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * Updates the display of all buttons based on the current game state.
     */
    public void updateDisplay(GameBoard board, GameRules rules, JButton[][] buttons) {
        // First pass: reset and set basic text
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                if (board.getCellType(r, c) == '.') {
                    if (board.hasLightAt(r, c)) {
                        buttons[r][c].setText("O");
                        buttons[r][c].setBackground(Color.GREEN);
                    } else if (board.isMarkedAt(r, c)) {
                        buttons[r][c].setText("X");
                        buttons[r][c].setBackground(Color.WHITE);
                        buttons[r][c].setForeground(Color.GRAY);
                    } else {
                        buttons[r][c].setText("");
                        buttons[r][c].setBackground(Color.WHITE);
                    }
                } else if (board.getCellType(r, c) >= '0' && board.getCellType(r, c) <= '4') {
                    // Check numbered cells
                    int required = board.getCellType(r, c) - '0';
                    int actual = rules.countAdjacentLights(board, r, c);

                    if (actual > required) {
                        buttons[r][c].setForeground(Color.RED);
                    } else if (actual == required) {
                        buttons[r][c].setForeground(Color.GREEN);
                    } else {
                        buttons[r][c].setForeground(Color.WHITE);
                    }
                }
            }
        }

        // Second pass: apply illumination
        for (int r = 0; r < board.getGridSize(); r++) {
            for (int c = 0; c < board.getGridSize(); c++) {
                if (board.hasLightAt(r, c)) {
                    illuminateFromLight(board, buttons, r, c);
                    if (rules.seesOtherLight(board, r, c)) {
                        buttons[r][c].setBackground(Color.RED);
                    }
                }
            }
        }
    }

    /**
     * Illuminates cells from a light source.
     */
    private void illuminateFromLight(GameBoard board, JButton[][] buttons, int row, int col) {
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = row + d[0], nc = col + d[1];
            while (nr >= 0 && nr < board.getGridSize() &&
                    nc >= 0 && nc < board.getGridSize() &&
                    board.getCellType(nr, nc) == '.') {
                if (!board.hasLightAt(nr, nc)) {
                    buttons[nr][nc].setBackground(new Color(144, 238, 144));
                }
                nr += d[0];
                nc += d[1];
            }
        }
    }

    /**
     * Flashes an invalid light placement temporarily.
     */
    public void flashInvalidLight(GameBoard board, JButton[][] buttons,
            int row, int col, String message,
            JLabel statusLabel, Runnable afterFlash) {
        board.placeLight(row, col);
        updateDisplay(board, new GameRules(), buttons);
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);

        Timer clearTimer = new Timer(600, e -> {
            board.removeLight(row, col);
            updateDisplay(board, new GameRules(), buttons);
            afterFlash.run();
        });
        clearTimer.setRepeats(false);
        clearTimer.start();
    }
}
