import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends JFrame {

    // --- Core UI Components ---
    private final JTextArea logArea;
    private final JButton startSimulationBtn;
    private final JSpinner usersSpinner;

    // --- ATM Core System ---
    private final ATM atm;

    public Main() {
        // --- Window Setup ---
        setTitle("ATM Simulation (Multithreading)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Log Area ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(" ATM Operations Log "));
        add(scrollPane, BorderLayout.CENTER);

        // --- Control Panel ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        controlPanel.add(new JLabel("Concurrent users count:"));
        usersSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        controlPanel.add(usersSpinner);

        startSimulationBtn = new JButton("Start Simulation");
        styleButton(startSimulationBtn, new Color(0, 123, 255));
        startSimulationBtn.addActionListener(_ -> startSimulation());
        controlPanel.add(startSimulationBtn);

        add(controlPanel, BorderLayout.SOUTH);

        // Initialize ATM System
        atm = new ATM(logArea);
    }

    private void startSimulation() {
        startSimulationBtn.setEnabled(false);
        logArea.setText("");
        atm.log("=== SIMULATION STARTED ===");

        int numUsers = (Integer) usersSpinner.getValue();

        // Create and start user threads
        for (int i = 1; i <= numUsers; i++) {
            Thread userThread = new Thread(new UserAction(atm, i), "User-" + i);
            userThread.start();
        }

        // A separate thread to re-enable the button after the simulation roughly ends
        new Thread(() -> {
            try {
                Thread.sleep(6000); // Simulation takes around 5 seconds
                SwingUtilities.invokeLater(() -> {
                    atm.log("=== SIMULATION FINISHED ===");
                    startSimulationBtn.setEnabled(true);
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }

    // ==========================================
    // ATM SYSTEM (MULTITHREADING LOGIC)
    // ==========================================

    /**
     * Thread-safe Account class.
     * All methods that modify the balance are marked as 'synchronized'
     * to prevent Race Conditions when multiple threads access the same account.
     */
    static class Account {
        private double balance;

        public Account(double initialBalance) {
            this.balance = initialBalance;
        }

        public synchronized void deposit(double amount) {
            balance += amount;
        }

        public synchronized boolean withdraw(double amount) {
            if (balance >= amount) {
                // Artificial delay
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                balance -= amount;
                return true;
            }
            return false;
        }

        public synchronized double getBalance() {
            return balance;
        }
    }

    /**
     * Main ATM Class handling operations.
     */
    static class ATM {
        // Constants
        private static final double MAX_WITHDRAWAL_LIMIT = 5000.0;
        private static final String LOG_FILE_PATH = "atm_log.txt";

        // ConcurrentHashMap for thread-safe addition/removal of accounts
        private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
        private final JTextArea logArea;
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        public ATM(JTextArea logArea) {
            this.logArea = logArea;
        }

        /**
         * Universal logging method (GUI and File).
         * Marked as 'synchronized' to prevent file writing conflicts between threads.
         */
        public synchronized void log(String message) {
            String time = LocalDateTime.now().format(timeFormatter);
            String threadName = Thread.currentThread().getName();
            String fullMessage = String.format("[%s] [%s] %s", time, threadName, message);

            // Safely update Swing GUI from a background thread
            SwingUtilities.invokeLater(() -> {
                logArea.append(fullMessage + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
            });

            // Write to log file
            try (FileWriter fw = new FileWriter(LOG_FILE_PATH, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(fullMessage);
            } catch (IOException e) {
                System.err.println("Failed to write to log file.");
            }
        }

        public void openAccount(int accId, double initialDeposit) {
            accounts.putIfAbsent(accId, new Account(initialDeposit));
            log(String.format("Opened account #%d. Initial balance: %.2f", accId, initialDeposit));
        }

        public void closeAccount(int accId) {
            if (accounts.remove(accId) != null) {
                log(String.format("Account #%d closed.", accId));
            } else {
                log(String.format("Error: Account #%d not found for closing.", accId));
            }
        }

        public void deposit(int accId, double amount) {
            Account acc = accounts.get(accId);
            if (acc != null) {
                acc.deposit(amount);
                log(String.format("Account #%d deposited by %.2f. New balance: %.2f", accId, amount, acc.getBalance()));
            } else {
                log(String.format("Error: Account #%d does not exist.", accId));
            }
        }

        public void withdraw(int accId, double amount) {
            // Check single withdrawal limit
            if (amount > MAX_WITHDRAWAL_LIMIT) {
                log(String.format("DENIED: Withdrawal limit exceeded (%.2f > %.2f)", amount, MAX_WITHDRAWAL_LIMIT));
                return;
            }

            Account acc = accounts.get(accId);
            if (acc != null) {
                if (acc.withdraw(amount)) {
                    log(String.format("Withdrawal of %.2f from account #%d SUCCESSFUL. Remaining balance: %.2f", amount, accId, acc.getBalance()));
                } else {
                    log(String.format("DENIED: Insufficient funds on account #%d for withdrawal of %.2f", accId, amount));
                }
            } else {
                log(String.format("Error: Account #%d does not exist.", accId));
            }
        }
    }

    // ==========================================
    // USER SIMULATION (THREADS)
    // ==========================================

    /**
     * Simulates the behavior of a single user (runs in a separate thread).
     */
    static class UserAction implements Runnable {
        private final ATM atm;
        private final int userId;

        public UserAction(ATM atm, int userId) {
            this.atm = atm;
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                int accId = 1000 + userId; // Unique account ID

                // 1. User opens an account
                atm.openAccount(accId, 1000.0);
                Thread.sleep((long) (Math.random() * 500));

                // 2. User deposits money
                atm.deposit(accId, 1500.0);
                Thread.sleep((long) (Math.random() * 1000));

                // 3. User attempts a valid withdrawal
                atm.withdraw(accId, 2000.0);
                Thread.sleep((long) (Math.random() * 1000));

                // 4. User attempts to withdraw above the ATM limit
                atm.withdraw(accId, 6000.0);
                Thread.sleep((long) (Math.random() * 500));

                // 5. User attempts to withdraw more than their balance
                atm.withdraw(accId, 2000.0);
                Thread.sleep((long) (Math.random() * 500));

                // 6. User closes the account
                atm.closeAccount(accId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static void main() {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}