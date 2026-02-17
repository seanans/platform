import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Main extends JFrame {

    // --- Fonts & Colors ---
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font boldFont = new Font("Segoe UI", Font.BOLD, 16);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 24);
    private final Font coinFont = new Font("Segoe UI", Font.BOLD, 64);
    private final Color colorBtnNormal = new Color(0, 123, 255); // Blue
    private final Color colorBtnTake = new Color(40, 167, 69);   // Green
    private final Color colorBtnDanger = new Color(220, 53, 69); // Red
    // --- GUI Components ---
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    // Game View Components
    private final JLabel turnLabel;
    private final JLabel coinsDisplayLabel;
    private final JLabel logLabel;
    private final JButton take1Btn;
    private final JButton take2Btn;
    private final JButton returnBtn;
    // --- Game State Variables ---
    private int coinsLeft;
    private Turn currentTurn;
    public Main() {
        // --- Window Setup ---
        setTitle("Гра в монети");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // --- Init Game Components ---
        turnLabel = new JLabel("Хід: ...", SwingConstants.CENTER);
        coinsDisplayLabel = new JLabel("0", SwingConstants.CENTER);
        logLabel = new JLabel("Починаємо гру!", SwingConstants.CENTER);

        take1Btn = new JButton("Взяти 1 монету");
        take2Btn = new JButton("Взяти 2 монети");
        returnBtn = new JButton("Повернутись до меню");

        // --- Create Views ---
        cardPanel.add(createMenuView(), "MENU");
        cardPanel.add(createGameView(), "GAME");

        add(cardPanel);
        cardLayout.show(cardPanel, "MENU");
    }

    private JPanel createMenuView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Гра в монети", SwingConstants.CENTER);
        title.setFont(headerFont);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel rules = new JLabel("<html><div style='text-align: center;'>" + "Правила:<br>За один хід можна взяти 1 або 2 монети.<br>" + "Виграє той, хто забере <b>останню</b> монету.</div></html>", SwingConstants.CENTER);
        rules.setFont(mainFont);
        gbc.gridy = 1;
        panel.add(rules, gbc);

        JButton startBtn = new JButton("Почати нову гру");
        styleButton(startBtn, colorBtnNormal);
        startBtn.addActionListener(_ -> startGame());
        gbc.gridy = 2;
        panel.add(startBtn, gbc);

        return panel;
    }

    // ==========================================
    // UI SETUP METHODS
    // ==========================================

    private JPanel createGameView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header (Turn Info)
        turnLabel.setFont(headerFont);
        panel.add(turnLabel, BorderLayout.NORTH);

        // Center (Coins Display)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));

        coinsDisplayLabel.setFont(coinFont);
        coinsDisplayLabel.setForeground(new Color(255, 165, 0)); // Golden Orange

        logLabel.setFont(boldFont);
        logLabel.setForeground(new Color(100, 100, 100)); // Gray

        centerPanel.add(coinsDisplayLabel);
        centerPanel.add(logLabel);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Footer (Action Buttons)
        JPanel footerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        styleButton(take1Btn, colorBtnTake);
        styleButton(take2Btn, colorBtnTake);
        take1Btn.addActionListener(_ -> userMove(1));
        take2Btn.addActionListener(_ -> userMove(2));

        buttonsPanel.add(take1Btn);
        buttonsPanel.add(take2Btn);

        styleButton(returnBtn, colorBtnNormal);
        returnBtn.setVisible(false);
        returnBtn.addActionListener(_ -> cardLayout.show(cardPanel, "MENU"));

        footerPanel.add(buttonsPanel);
        footerPanel.add(returnBtn);

        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void startGame() {
        // 1. Random initial coins (e.g., between 15 and 30)
        coinsLeft = (int) (Math.random() * 16) + 15;

        // 2. Random first turn (0 = User, 1 = AI)
        currentTurn = (Math.random() < 0.5) ? Turn.USER : Turn.AI;

        logLabel.setText("Починаємо! Монет на столі: " + coinsLeft);
        returnBtn.setVisible(false);
        take1Btn.setVisible(true);
        take2Btn.setVisible(true);

        updateUIState();
        cardLayout.show(cardPanel, "GAME");

        if (currentTurn == Turn.AI) {
            triggerAiTurn();
        }
    }

    // ==========================================
    // GAME LOGIC METHODS
    // ==========================================

    private void userMove(int amount) {
        if (amount > coinsLeft) return; // Safeguard

        coinsLeft -= amount;
        logLabel.setText("Ви взяли " + amount + " " + getCoinWord(amount));

        if (checkGameOver(Turn.USER)) return;

        currentTurn = Turn.AI;
        updateUIState();
        triggerAiTurn();
    }

    private void triggerAiTurn() {
        // 0.5 second delay.
        // Makes the AI feel more "human" and lets the user read what happened.
        Timer timer = new Timer(500, _ -> {
            int takeAmount = calculateAiOptimalMove();
            coinsLeft -= takeAmount;

            logLabel.setText("Комп'ютер взяв " + takeAmount + " " + getCoinWord(takeAmount));

            if (checkGameOver(Turn.AI)) return;

            currentTurn = Turn.USER;
            updateUIState();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * AI OPTIMAL STRATEGY LOGIC
     * Mathematical proof:
     * Target: Take the last coin (leave 0).
     * If you leave a multiple of 3 (0, 3, 6, 9...), whatever the opponent takes (1 or 2),
     * they will leave a non-multiple of 3. You can then restore it to a multiple of 3.
     */
    private int calculateAiOptimalMove() {
        int remainder = coinsLeft % 3;

        if (remainder == 1) {
            // Winning move: take 1 to leave a multiple of 3
            return 1;
        } else if (remainder == 2) {
            // Winning move: take 2 to leave a multiple of 3
            return 2;
        } else {
            // Losing position (coinsLeft % 3 == 0).
            // AI cannot force a win if user plays perfectly.
            // Take 1 or 2 randomly, or just 1 to delay the game, hoping user makes a mistake.
            // But we must be careful not to take 2 if only 1 coin is left (though 1 % 3 = 1, handled above).
            return (Math.random() < 0.5 && coinsLeft >= 2) ? 2 : 1;
        }
    }

    private boolean checkGameOver(Turn winner) {
        if (coinsLeft <= 0) {
            coinsLeft = 0;
            updateUIState();

            if (winner == Turn.USER) {
                turnLabel.setText("ВИ ПЕРЕМОГЛИ!");
                turnLabel.setForeground(colorBtnTake);
            } else {
                turnLabel.setText("ПЕРЕМІГ КОМП'ЮТЕР!");
                turnLabel.setForeground(colorBtnDanger);
            }

            take1Btn.setVisible(false);
            take2Btn.setVisible(false);
            returnBtn.setVisible(true);
            return true;
        }
        return false;
    }

    private void updateUIState() {
        coinsDisplayLabel.setText(String.valueOf(coinsLeft));

        if (coinsLeft > 0) {
            if (currentTurn == Turn.USER) {
                turnLabel.setText("Ваш хід");
                turnLabel.setForeground(colorBtnNormal);
                take1Btn.setEnabled(true);
                take2Btn.setEnabled(coinsLeft >= 2); // Disable take 2 if only 1 coin left
            } else {
                turnLabel.setText("Комп'ютер думає...");
                turnLabel.setForeground(colorBtnDanger);
                take1Btn.setEnabled(false);
                take2Btn.setEnabled(false);
            }
        }
    }

    private String getCoinWord(int amount) {
        return amount == 1 ? "монету" : "монети";
    }

    // ==========================================
    // UTILS
    // ==========================================

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(boldFont);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }

    void main() {
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

    // --- Enums ---
    enum Turn {USER, AI}
}