import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    // --- Fonts & Styling ---
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 22);
    private final List<Move> currentSessionMoves = new ArrayList<>();
    private final List<Move> globalMoves = new ArrayList<>();
    private final String historyFilePath = "game_history.csv";
    // --- GUI Components ---
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final JLabel roundLabel;
    private final JLabel logLabel;
    private final JButton wellBtn;
    private final JButton scissorsBtn;
    private final JButton paperBtn;
    private final JButton returnBtn;
    // --- Game State Variables ---
    private int targetRounds;
    private int currentRound;
    private int aiMode;
    private JSpinner roundsSpinner;
    private JComboBox<String> aiModeCombo;
    public Main() {
        // --- Window Setup ---
        setTitle("Криниця - Ножиці - Папір");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // --- Init Labels and Buttons before adding to panels ---
        roundLabel = new JLabel("Раунд 1", SwingConstants.CENTER);
        logLabel = new JLabel("Оберіть ваш хід!", SwingConstants.CENTER);

        wellBtn = new JButton(Move.WELL.toString());
        scissorsBtn = new JButton(Move.SCISSORS.toString());
        paperBtn = new JButton(Move.PAPER.toString());
        returnBtn = new JButton("Повернутись до меню");

        // --- Create Views ---
        cardPanel.add(createMenuView(), "MENU");
        cardPanel.add(createGameView(), "GAME");

        add(cardPanel);
        cardLayout.show(cardPanel, "MENU");
    }

    // Entry Point
    static void main() {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
            new Main().setVisible(true);
        });
    }

    private JPanel createMenuView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Криниця-Ножиці-Папір", SwingConstants.CENTER);
        title.setFont(headerFont);
        title.setForeground(new Color(50, 50, 50));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(title, gbc);

        // Rounds Spinner
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel roundsLabel = new JLabel("Кількість раундів:");
        roundsLabel.setFont(boldFont);
        panel.add(roundsLabel, gbc);

        roundsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        roundsSpinner.setFont(mainFont);
        gbc.gridx = 1;
        panel.add(roundsSpinner, gbc);

        // AI Mode Dropdown
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel modeLabel = new JLabel("Рівень інтелекту (ШІ):");
        modeLabel.setFont(boldFont);
        panel.add(modeLabel, gbc);

        String[] modes = {"Випадковий вибір (Легко)", "Аналіз поточного сеансу (Середньо)", "Аналіз минулих ігор з файлу (Складно)"};
        aiModeCombo = new JComboBox<>(modes);
        aiModeCombo.setFont(mainFont);
        aiModeCombo.setBackground(Color.WHITE);
        gbc.gridx = 1;
        panel.add(aiModeCombo, gbc);

        // Start Button
        JButton startBtn = new JButton("Почати гру");
        styleButton(startBtn, new Color(40, 167, 69));
        startBtn.addActionListener(_ -> startGame());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(startBtn, gbc);

        // Stats Button
        JButton statsBtn = new JButton("Переглянути статистику");
        styleButton(statsBtn, new Color(108, 117, 125));
        statsBtn.addActionListener(_ -> showStatistics());
        gbc.gridy = 4;
        panel.add(statsBtn, gbc);

        return panel;
    }

    // ==========================================
    // UI SETUP METHODS
    // ==========================================

    private JPanel createGameView() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        roundLabel.setFont(headerFont);
        logLabel.setFont(boldFont);
        headerPanel.add(roundLabel);
        headerPanel.add(logLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Action Buttons Panel
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        styleButton(wellBtn, new Color(52, 58, 64));
        styleButton(scissorsBtn, new Color(52, 58, 64));
        styleButton(paperBtn, new Color(52, 58, 64));

        wellBtn.addActionListener(_ -> playRound(Move.WELL));
        scissorsBtn.addActionListener(_ -> playRound(Move.SCISSORS));
        paperBtn.addActionListener(_ -> playRound(Move.PAPER));

        buttonsPanel.add(wellBtn);
        buttonsPanel.add(scissorsBtn);
        buttonsPanel.add(paperBtn);
        panel.add(buttonsPanel, BorderLayout.CENTER);

        // Footer Panel
        styleButton(returnBtn, new Color(220, 53, 69));
        returnBtn.setVisible(false);
        returnBtn.addActionListener(_ -> cardLayout.show(cardPanel, "MENU"));
        panel.add(returnBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void startGame() {
        targetRounds = (Integer) roundsSpinner.getValue();
        aiMode = aiModeCombo.getSelectedIndex();
        currentRound = 1;
        currentSessionMoves.clear();

        if (aiMode == 2) {
            loadGlobalHistory();
        }

        updateGameUI();
        logLabel.setText("Зробіть свій перший хід!");
        logLabel.setForeground(Color.BLACK);

        toggleGameButtons(true);
        returnBtn.setVisible(false);

        cardLayout.show(cardPanel, "GAME");
    }

    // ==========================================
    // GAME LOGIC METHODS
    // ==========================================

    private void playRound(Move userMove) {
        currentSessionMoves.add(userMove);

        Move aiMove = getAIMove();
        Result result = calculateResult(userMove, aiMove);

        logLabel.setText(String.format("Ви: %s | ПК: %s. %s", userMove, aiMove, result.text));
        logLabel.setForeground(result.color);

        saveToHistory(userMove, aiMove, result);

        // Ties do not progress the round counter
        if (result != Result.TIE) {
            currentRound++;
        }

        if (currentRound > targetRounds) {
            endSession();
        } else {
            updateGameUI();
        }
    }

    private void endSession() {
        roundLabel.setText("Сеанс завершено!");
        toggleGameButtons(false);
        returnBtn.setVisible(true);
    }

    private Move getAIMove() {
        return switch (aiMode) {
            case 1 -> predictUserMove(currentSessionMoves);
            case 2 -> predictUserMove(globalMoves);
            default -> getRandomMove();
        };
    }

    private Move predictUserMove(List<Move> history) {
        if (history == null || history.isEmpty()) {
            return getRandomMove();
        }

        int w = 0, s = 0, p = 0;
        for (Move move : history) {
            switch (move) {
                case WELL -> w++;
                case SCISSORS -> s++;
                case PAPER -> p++;
            }
        }

        Move predictedUserMove = Move.WELL;
        if (s > w && s >= p) predictedUserMove = Move.SCISSORS;
        if (p > w && p >= s) predictedUserMove = Move.PAPER;

        return switch (predictedUserMove) {
            case WELL -> Move.PAPER;
            case SCISSORS -> Move.WELL;
            case PAPER -> Move.SCISSORS;
        };
    }

    private Move getRandomMove() {
        int r = (int) (Math.random() * 3);
        return Move.values()[r];
    }

    private Result calculateResult(Move user, Move ai) {
        if (user == ai) return Result.TIE;

        return switch (user) {
            case WELL -> (ai == Move.SCISSORS) ? Result.WIN : Result.LOSE;
            case SCISSORS -> (ai == Move.PAPER) ? Result.WIN : Result.LOSE;
            case PAPER -> (ai == Move.WELL) ? Result.WIN : Result.LOSE;
        };
    }

    private void saveToHistory(Move user, Move ai, Result result) {
        try (FileWriter fw = new FileWriter(historyFilePath, true); PrintWriter pw = new PrintWriter(fw)) {
            // CSV: Player,Ai,Result
            pw.println(user.name() + "," + ai.name() + "," + result.name());
        } catch (IOException e) {
            System.err.println("Помилка запису в файл: " + e.getMessage());
        }
    }

    // ==========================================
    // FILE I/O AND STATISTICS
    // ==========================================

    private void loadGlobalHistory() {
        globalMoves.clear();
        File file = new File(historyFilePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        globalMoves.add(Move.valueOf(parts[0]));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка читання файлу: " + e.getMessage());
        }
    }

    private void showStatistics() {
        int wins = 0, losses = 0, ties = 0;
        File file = new File(historyFilePath);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        if ("WIN".equals(parts[2])) wins++;
                        if ("LOSE".equals(parts[2])) losses++;
                        if ("TIE".equals(parts[2])) ties++;
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Помилка читання статистики.");
                return;
            }
        }

        String msg = String.format("Загальна глобальна статистика:\n\n Перемог: %d\n Поразок: %d\n Нічиїх: %d", wins, losses, ties);
        JOptionPane.showMessageDialog(this, msg, "Статистика гравця", JOptionPane.INFORMATION_MESSAGE);
    }

    private void toggleGameButtons(boolean enabled) {
        wellBtn.setEnabled(enabled);
        scissorsBtn.setEnabled(enabled);
        paperBtn.setEnabled(enabled);
    }

    // ==========================================
    // UTILS
    // ==========================================

    private void updateGameUI() {
        roundLabel.setText(String.format("Раунд %d з %d", currentRound, targetRounds));
    }

    // Той самий метод стилізації, що й у грі "Життя"
    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(boldFont);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMargin(new Insets(10, 15, 10, 15));
    }

    // --- Enums for Game Logic ---
    enum Move {
        WELL("Криниця"), SCISSORS("Ножиці"), PAPER("Папір");
        private final String name;

        Move(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum Result {
        WIN("Перемога!", new Color(40, 167, 69)), LOSE("Поразка!", new Color(220, 53, 69)), TIE("Нічия! Переграємо раунд.", new Color(232, 169, 81));

        private final String text;
        private final Color color;

        Result(String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }
}