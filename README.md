# LightUp Game

A Java implementation of the Light Up puzzle game with multiple AI solving approaches.

## About LightUp

Light Up is a logic puzzle where the goal is to place light bulbs on a grid to illuminate all white cells while following these rules:
- Place light bulbs on white cells
- Light bulbs illuminate their entire row and column until hitting a black cell
- No light bulb can illuminate another light bulb
- Numbered black cells indicate exactly how many light bulbs must be placed adjacent to them

## Features

- **Interactive GUI**: User-friendly interface for playing the game
- **Multiple AI Algorithms**: Different solving approaches including:
  - Dynamic Programming with Backtracking
  - Divide and Conquer
  - Greedy Algorithm
- **Cooperative Mode**: Play alongside AI to solve puzzles together
- **Timer**: Track solving time
- **Undo Function**: Reverse moves when needed

## Project Structure

```
LightUp/
├── Using DP Approach/          # DP + Backtracking implementation
├── Using Greedy Approach/       # Greedy algorithm implementation  
├── Using DAC Approach/          # Divide and Conquer implementation
└── README.md                    # This file
```

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Maven for building the project

### Installation

1. Clone the repository:
```bash
git clone https://github.com/oxel18/LightUp.git
cd LightUp
```

2. Navigate to the desired algorithm approach:
```bash
cd "Using DP Approach"
```

3. Build the project:
```bash
cd Code
mvn compile
mvn exec:java -Dexec.mainClass="com.lightup.game.LightUpGameSimple"
```

## How to Play

1. **Left Click**: Place a light bulb
2. **Right Click**: Mark/unmark a cell as blocked
3. **AI Assistance**: Let the AI help solve the puzzle using different algorithms
4. **Win Condition**: All white cells must be illuminated without rule violations

## Algorithm Approaches

### Dynamic Programming with Backtracking
- Uses memoization to cache subproblem solutions
- Employs backtracking to explore all valid light placements
- Optimal for finding complete solutions

### Divide and Conquer
- Splits the board into independent regions
- Solves each region deterministically
- Efficient for puzzles with clear separations

### Greedy Algorithm
- Makes locally optimal choices at each step
- Uses heuristics to evaluate move quality
- Fast but may not always find optimal solutions

## Game Controls

- **New Game**: Start a fresh puzzle
- **Undo**: Reverse the last move
- **Algorithm Selection**: Choose different AI approaches

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Original Light Up puzzle concept
- Java Swing for GUI implementation
- Maven for project management
