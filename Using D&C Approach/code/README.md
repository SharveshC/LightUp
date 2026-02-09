# Light Up Puzzle Game

A Java implementation of the classic Light Up puzzle game, also known as Akari. This project provides both a playable game interface and an AI solver for the puzzle.

## Game Overview

Light Up is a grid-based logic puzzle where the objective is to place light bulbs on the grid to illuminate all white squares. The game follows these rules:

- Black cells are walls and cannot contain light bulbs
- Numbers in black cells indicate how many adjacent (up, down, left, right) light bulbs must be placed next to them
- Light bulbs emit light in straight lines (horizontally and vertically) until they hit a wall
- Light bulbs cannot illuminate each other (you can't place a bulb in a square that's already illuminated by another bulb)
- All white squares must be illuminated to solve the puzzle

## Features

- **Interactive GUI**: Built with Java Swing for a responsive and intuitive user experience
- **Multiple Difficulty Levels**: Ranging from beginner to expert puzzles
- **AI Solver**: Implements both BFS and DFS algorithms with configurable difficulty
- **Smart Validation**: Real-time move validation and rule enforcement
- **Undo/Redo**: Full move history with undo/redo functionality
- **Puzzle Generator**: Creates solvable puzzles with varying difficulty
- **Game Timer**: Tracks solving time with pause/resume capability
- **Save/Load**: Save your progress and resume later

## Requirements

- Java 11 or higher
- Maven 3.6.0 or higher

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/lightup-game.git
   cd lightup-game
   ```

2. Build the project using Maven:
   ```bash
   mvn clean package
   ```

## How to Play

1. Run the game:
   ```bash
   java -jar target/lightup-game-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

2. Game Controls:
   - Left-click on an empty cell to place a light bulb
   - Right-click to mark a cell as empty (no bulb allowed)
   - Middle-click to clear a cell
   - Use the menu to start a new game, change difficulty, or access the AI solver

## Project Structure

- `src/main/java/com/lightup/game/` - Main game source code
  - `AIPlayer.java` - AI solver implementation with BFS/DFS algorithms and heuristic scoring
  - `GameBoard.java` - Manages game state, grid layout, and move history
  - `GameRules.java` - Encapsulates all game validation logic and win conditions
  - `GameTimer.java` - Implements game timing functionality with pause/resume
  - `LightUpGameSimple.java` - Main game class that initializes and coordinates components
  - `UIComponents.java` - Handles all Swing-based UI elements and event handling

## Implementation Details

### Core Game Logic
- **Grid Representation**: 7x7 grid with support for walls, numbers, and light placements
- **Rule Engine**: Comprehensive validation of all game rules including adjacency constraints and light beam propagation
- **State Management**: Efficient tracking of game state with support for undo/redo operations

### AI Solver
- **Search Algorithms**: Implements both BFS (Breadth-First Search) and DFS (Depth-First Search)
- **Heuristic Scoring**: Intelligent move ordering based on potential impact on the board state
- **Difficulty Levels**: Adjustable search depth and strategy based on difficulty setting

### User Interface
- **Responsive Design**: Adapts to different window sizes
- **Visual Feedback**: Clear indication of valid/invalid moves and game state
- **Intuitive Controls**: Right-click to mark cells, left-click to place bulbs, middle-click to clear

## Development Setup

1. **Prerequisites**:
   - JDK 11 or later
   - Maven 3.6.0 or later
   - Git for version control

2. **Building from Source**:
   ```bash
   git clone https://github.com/yourusername/lightup-game.git
   cd lightup-game
   mvn clean package
   ```

3. **Running Tests**:
   ```bash
   mvn test
   ```

4. **Code Style**:
   - Follow Google Java Style Guide
   - Use 4 spaces for indentation
   - Write Javadoc for all public methods and classes

## Contributing

We welcome contributions! Here's how you can help:

1. **Report Bugs**: Open an issue with detailed reproduction steps
2. **Suggest Features**: Propose new features or improvements
3. **Submit Pull Requests**: Follow these steps:
   - Fork the repository
   - Create a feature branch (`git checkout -b feature/AmazingFeature`)
   - Commit your changes (`git commit -m 'Add some AmazingFeature'`)
   - Push to the branch (`git push origin feature/AmazingFeature`)
   - Open a Pull Request

## Performance

- The AI solver can solve standard 7x7 puzzles in under 1 second
- Memory usage is optimized for smooth performance on most systems
- The UI remains responsive even during complex AI calculations

## Future Enhancements

- [ ] Support for custom grid sizes
- [ ] Additional puzzle themes and visual styles
- [ ] Mobile version with touch controls
- [ ] Online multiplayer mode
- [ ] Puzzle editor for creating custom levels

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Based on the classic Light Up puzzle by Nikoli
- Built with Java and Swing
- Special thanks to all contributors who have helped improve this project