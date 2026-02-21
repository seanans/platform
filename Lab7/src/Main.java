import javax.swing.*;
import java.awt.*;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame implements ActionListener {

    // --- Game Constants ---
    private static final int BOARD_SIZE = 15;
    private static final int WIN_CONDITION = 5;
    private static final int EMPTY = 0;
    private static final int PLAYER = 1; // 'X'
    private static final int AI = 2;     // 'O'

    // --- UI Components ---
    private final JButton[][] buttons = new JButton[BOARD_SIZE][BOARD_SIZE];
    private final JLabel statusLabel;
    private boolean isPlayerTurn = true;
    private boolean gameEnded = false;

    // --- Data Model ---
    private final int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

    public Main() {
        setTitle("Five in a Row (Gomoku)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Status Panel ---
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 44, 52));
        statusLabel = new JLabel("Your turn! (You are X)");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);

        // --- Game Board ---
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        boardPanel.setBackground(Color.BLACK);

        Font font = new Font("Arial", Font.BOLD, 20);
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                JButton btn = new JButton("");
                btn.setFont(font);
                btn.setBackground(new Color(240, 240, 240));
                btn.setFocusPainted(false);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setActionCommand(r + "," + c);
                btn.addActionListener(this);
                buttons[r][c] = btn;
                boardPanel.add(btn);
            }
        }
        add(boardPanel, BorderLayout.CENTER);

        // --- Control Panel ---
        JPanel bottomPanel = new JPanel();
        JButton restartBtn = new JButton("Restart Game");
        restartBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        restartBtn.addActionListener(_ -> resetBoard());
        bottomPanel.add(restartBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameEnded || !isPlayerTurn) return;

        String[] parts = e.getActionCommand().split(",");
        int r = Integer.parseInt(parts[0]);
        int c = Integer.parseInt(parts[1]);

        if (board[r][c] == EMPTY) {
            makeMove(r, c, PLAYER);

            if (!gameEnded) {
                isPlayerTurn = false;
                statusLabel.setText("AI is thinking...");

                // Delay for UX
                Timer timer = new Timer(300, _ -> makeAIMove());
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    private void makeMove(int r, int c, int player) {
        board[r][c] = player;
        buttons[r][c].setText(player == PLAYER ? "X" : "O");
        buttons[r][c].setForeground(player == PLAYER ? Color.BLUE : Color.RED);

        if (checkWin(r, c, player)) {
            gameEnded = true;
            statusLabel.setText(player == PLAYER ? "YOU WIN!" : "AI WINS!");
            highlightWinningLine(r, c, player);
        } else if (isBoardFull()) {
            gameEnded = true;
            statusLabel.setText("IT'S A DRAW!");
        }
    }

    private void resetBoard() {
        gameEnded = false;
        isPlayerTurn = true;
        statusLabel.setText("Your turn! (You are X)");
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = EMPTY;
                buttons[r][c].setText("");
                buttons[r][c].setBackground(new Color(240, 240, 240));
            }
        }
    }

    private boolean isBoardFull() {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == EMPTY) return false;
            }
        }
        return true;
    }

    // ==========================================
    // AI LOGIC (HEURISTIC EVALUATION)
    // ==========================================

    private void makeAIMove() {
        int bestScore = -1;
        int bestR = -1;
        int bestC = -1;

        // Iterate through all empty cells to evaluate their potential
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] == EMPTY) {
                    // Score = How good is this move for AI (Attack) + How much it blocks the Player (Defense)
                    int attackScore = evaluateCell(r, c, AI);
                    int defenseScore = evaluateCell(r, c, PLAYER);

                    // Defense is slightly prioritized if scores are equal
                    int totalScore = attackScore + defenseScore;

                    // Add a tiny random factor (0-5) to prevent deterministic predictable games
                    totalScore += (int)(Math.random() * 5);

                    if (totalScore > bestScore) {
                        bestScore = totalScore;
                        bestR = r;
                        bestC = c;
                    }
                }
            }
        }

        // Failsafe if board is empty (first move)
        if (bestR == -1) {
            bestR = BOARD_SIZE / 2;
            bestC = BOARD_SIZE / 2;
        }

        makeMove(bestR, bestC, AI);
        if (!gameEnded) {
            isPlayerTurn = true;
            statusLabel.setText("Your turn! (You are X)");
        }
    }

    /**
     * Evaluates the strategic value of placing a piece at (r, c).
     */
    private int evaluateCell(int r, int c, int targetPlayer) {
        int score = 0;
        // Evaluate all 4 axes (Horizontal, Vertical, Diagonal-Right, Diagonal-Left)
        score += evaluateAxis(r, c, targetPlayer, 0, 1);
        score += evaluateAxis(r, c, targetPlayer, 1, 0);
        score += evaluateAxis(r, c, targetPlayer, 1, 1);
        score += evaluateAxis(r, c, targetPlayer, 1, -1);
        return score;
    }

    private int evaluateAxis(int r, int c, int player, int dr, int dc) {
        int count = 1;
        int openEnds = 0;

        // Check forward direction
        int i = 1;
        while (isValid(r + i * dr, c + i * dc) && board[r + i * dr][c + i * dc] == player) {
            count++;
            i++;
        }
        if (isValid(r + i * dr, c + i * dc) && board[r + i * dr][c + i * dc] == EMPTY) openEnds++;

        // Check backward direction
        i = 1;
        while (isValid(r - i * dr, c - i * dc) && board[r - i * dr][c - i * dc] == player) {
            count++;
            i++;
        }
        if (isValid(r - i * dr, c - i * dc) && board[r - i * dr][c - i * dc] == EMPTY) openEnds++;

        return calculateHeuristicWeight(count, openEnds);
    }
    /**
     * Assigns a weight based on sequence length and open ends.
     */
    private int calculateHeuristicWeight(int count, int openEnds) {
        if (count >= WIN_CONDITION) return 100000; // Guaranteed Win

        if (count == 4) {
            if (openEnds == 2) return 10000; // Open 4 (Unstoppable)
            if (openEnds == 1) return 1000;  // Blocked 4 (Must react)
        }
        if (count == 3) {
            if (openEnds == 2) return 1000;  // Open 3
            if (openEnds == 1) return 100;   // Blocked 3
        }
        if (count == 2) {
            if (openEnds == 2) return 100;   // Open 2
            if (openEnds == 1) return 10;    // Blocked 2
        }
        return 1; // Single piece with potential
    }

    // ==========================================
    // WIN DETECTION
    // ==========================================

    private boolean checkWin(int r, int c, int player) {
        return countConsecutive(r, c, player, 0, 1) >= WIN_CONDITION || // Horizontal
                countConsecutive(r, c, player, 1, 0) >= WIN_CONDITION || // Vertical
                countConsecutive(r, c, player, 1, 1) >= WIN_CONDITION || // Diagonal \
                countConsecutive(r, c, player, 1, -1) >= WIN_CONDITION;  // Diagonal /
    }

    private int countConsecutive(int r, int c, int player, int dr, int dc) {
        int count = 1;
        int i = 1;
        while (isValid(r + i * dr, c + i * dc) && board[r + i * dr][c + i * dc] == player) { count++; i++; }
        i = 1;
        while (isValid(r - i * dr, c - i * dc) && board[r - i * dr][c - i * dc] == player) { count++; i++; }
        return count;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE;
    }

    private void highlightWinningLine(int r, int c, int player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] dir : directions) {
            if (countConsecutive(r, c, player, dir[0], dir[1]) >= WIN_CONDITION) {
                buttons[r][c].setBackground(Color.GREEN);
                int i = 1;
                while (isValid(r + i * dir[0], c + i * dir[1]) && board[r + i * dir[0]][c + i * dir[1]] == player) {
                    buttons[r + i * dir[0]][c + i * dir[1]].setBackground(Color.GREEN);
                    i++;
                }
                i = 1;
                while (isValid(r - i * dir[0], c - i * dir[1]) && board[r - i * dir[0]][c - i * dir[1]] == player) {
                    buttons[r - i * dir[0]][c - i * dir[1]].setBackground(Color.GREEN);
                    i++;
                }
                break;
            }
        }
    }

    static void main() {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}