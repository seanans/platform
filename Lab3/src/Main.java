import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {

    // --- Game Configuration ---
    private static final int ROWS = 50;
    private static final int COLS = 70;
    private static final int CELL_SIZE = 12;

    // --- Game State ---
    private boolean[][] grid = new boolean[ROWS][COLS];
    private boolean isRunning = false;
    private Timer timer; // Виправлено: тепер final

    // --- GUI Components ---
    private final GridPanel gridPanel;
    private final JButton startStopButton;
    private final JSlider speedSlider;
    private final JSpinner speedSpinner;

    // --- Colors (Темніші для кращого контрасту з білим текстом) ---
    private final Color colorBtnNormal = new Color(25, 110, 45); // Темно-зелений
    private final Color colorBtnDanger = new Color(150, 30, 40); // Темно-червоний

    public Main() {
        // --- Window Setup ---
        setTitle("Conway's Game of Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- Grid Panel (The Universe) ---
        gridPanel = new GridPanel();
        add(gridPanel, BorderLayout.CENTER);

        // --- Control Panel (Bottom) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(240, 240, 240));

        // Start/Stop Button
        startStopButton = new JButton("Start");
        styleButton(startStopButton, colorBtnNormal);
        startStopButton.addActionListener(_ -> toggleGame());
        controlPanel.add(startStopButton);

        // Randomize Button (Виправлено: тепер локальна змінна)
        JButton randomizeButton = new JButton("Randomize");
        styleButton(randomizeButton, colorBtnNormal);
        randomizeButton.addActionListener(_ -> randomizeGrid());
        controlPanel.add(randomizeButton);

        // Clear Button (Виправлено: тепер локальна змінна)
        JButton clearButton = new JButton("Clear");
        styleButton(clearButton, colorBtnNormal);
        clearButton.addActionListener(_ -> clearGrid());
        controlPanel.add(clearButton);

        // --- Speed Controls (Slider + Number Input) ---
        controlPanel.add(new JLabel("Delay (ms):"));

        // Slider
        speedSlider = new JSlider(10, 500, 100);
        speedSlider.setInverted(true); // Вліво = 500 (повільно), Вправо = 10 (швидко)

        // Spinner
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 10, 500, 10);
        speedSpinner = new JSpinner(spinnerModel);
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) speedSpinner.getEditor();
        spinnerEditor.getTextField().setColumns(3);


        speedSlider.addChangeListener(_ -> {
            int val = speedSlider.getValue();
            speedSpinner.setValue(val);
            if (timer != null) timer.setDelay(val);
        });

        speedSpinner.addChangeListener(_ -> {
            int val = (Integer) speedSpinner.getValue();
            speedSlider.setValue(val);
            if (timer != null) timer.setDelay(val);
        });

        controlPanel.add(speedSlider);
        controlPanel.add(speedSpinner);

        add(controlPanel, BorderLayout.SOUTH);

        // --- Timer Setup (Game Loop) ---
        timer = new Timer(speedSlider.getValue(), _ -> nextGeneration());

        pack();
        setLocationRelativeTo(null);
    }

    // --- Game Logic Methods ---

    private void toggleGame() {
        isRunning = !isRunning;
        if (isRunning) {
            startStopButton.setText("Pause");
            startStopButton.setBackground(colorBtnDanger);
            timer.start();
        } else {
            startStopButton.setText("Start");
            startStopButton.setBackground(colorBtnNormal);
            timer.stop();
        }
    }

    private void clearGrid() {
        if (isRunning) toggleGame();
        grid = new boolean[ROWS][COLS];
        gridPanel.repaint();
    }

    private void randomizeGrid() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = Math.random() < 0.2;
            }
        }
        gridPanel.repaint();
    }

    private void nextGeneration() {
        boolean[][] nextGrid = new boolean[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int aliveNeighbors = countNeighbors(row, col);

                if (grid[row][col]) {
                    nextGrid[row][col] = (aliveNeighbors == 2 || aliveNeighbors == 3);
                } else {
                    nextGrid[row][col] = (aliveNeighbors == 3);
                }
            }
        }

        grid = nextGrid;
        gridPanel.repaint();
    }

    private int countNeighbors(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r == row && c == col) continue;
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                    if (grid[r][c]) count++;
                }
            }
        }
        return count;
    }

    // --- Helper for UI ---
    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);

        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);

        btn.setOpaque(true);
        btn.setMargin(new Insets(8, 15, 8, 15));
    }

    // --- Custom Panel for Drawing the Grid ---
    private class GridPanel extends JPanel {

        public GridPanel() {
            setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
            setBackground(Color.BLACK);

            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { toggleCell(e.getX(), e.getY()); }

                @Override
                public void mouseDragged(MouseEvent e) { toggleCell(e.getX(), e.getY()); }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        private void toggleCell(int x, int y) {
            int col = x / CELL_SIZE;
            int row = y / CELL_SIZE;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                grid[row][col] = true;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (grid[row][col]) {
                        g.setColor(new Color(50, 205, 50)); // Alive
                    } else {
                        g.setColor(new Color(30, 30, 30)); // Dead
                    }
                    g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE - 1, CELL_SIZE - 1);
                }
            }
        }
    }

    static void main() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new Main().setVisible(true);
        });
    }
}