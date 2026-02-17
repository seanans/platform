import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Main extends JFrame {

    // Data structure for conversion factors
    private final Map<String, Map<String, Double>> conversionData = new HashMap<>();

    // GUI Components
    private final JComboBox<String> categoryCombo;
    private final JComboBox<String> fromUnitCombo;
    private final JComboBox<String> toUnitCombo;
    private final JTextField inputField;
    private final JLabel resultLabel;

    // Fonts
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 18);

    public Main() {
        // --- Window Setup ---
        setTitle("Modern Unit Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // --- Initialize Data ---
        initData();

        // --- Header Section ---
        JLabel titleLabel = new JLabel("Unit Converter", SwingConstants.CENTER);
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(new Color(50, 50, 50));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // --- Form Section (Center) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // Padding around cells
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Category Row
        addLabel(formPanel, "Category:", 0, 0, gbc);
        categoryCombo = new JComboBox<>(conversionData.keySet().toArray(new String[0]));
        styleComponent(categoryCombo);
        categoryCombo.addActionListener(_ -> updateUnitCombos());
        addComponent(formPanel, categoryCombo, 1, 0, gbc);

        // 2. Value Row
        addLabel(formPanel, "Enter Value:", 0, 1, gbc);
        inputField = new JTextField();
        styleComponent(inputField);
        addComponent(formPanel, inputField, 1, 1, gbc);

        // 3. From Unit Row
        addLabel(formPanel, "From Unit:", 0, 2, gbc);
        fromUnitCombo = new JComboBox<>();
        styleComponent(fromUnitCombo);
        addComponent(formPanel, fromUnitCombo, 1, 2, gbc);

        // 4. To Unit Row
        addLabel(formPanel, "To Unit:", 0, 3, gbc);
        toUnitCombo = new JComboBox<>();
        styleComponent(toUnitCombo);
        addComponent(formPanel, toUnitCombo, 1, 3, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // --- Bottom Section (Button & Result) ---
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JButton convertButton = new JButton("Convert Now");
        convertButton.setFont(boldFont);
        convertButton.setBackground(new Color(70, 130, 180));
        convertButton.setForeground(Color.WHITE);
        convertButton.setFocusPainted(false);
        convertButton.addActionListener(new ConvertAction());
        convertButton.setOpaque(true);
        convertButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        bottomPanel.add(convertButton);

        resultLabel = new JLabel("Result will appear here", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setForeground(new Color(0, 102, 204));
        bottomPanel.add(resultLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Initial population
        updateUnitCombos();
    }

    // --- Helper Methods for UI Styling ---

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {

                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception _) {
                }
            }
            new Main().setVisible(true);
        });
    }

    private void addLabel(JPanel panel, String text, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(text);
        label.setFont(boldFont);
        panel.add(label, gbc);
    }

    private void addComponent(JPanel panel, JComponent comp, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    // --- Data & Logic ---

    private void styleComponent(JComponent comp) {
        comp.setFont(mainFont);
        if (comp instanceof JComboBox) {
            comp.setBackground(Color.WHITE);
        }
    }

    private void initData() {
        // 1. Time
        Map<String, Double> time = new HashMap<>();
        time.put("Seconds", 1.0);
        time.put("Minutes", 60.0);
        time.put("Hours", 3600.0);
        time.put("Days", 86400.0);
        conversionData.put("Time", time);

        // 2. Distance
        Map<String, Double> distance = new HashMap<>();
        distance.put("Meters", 1.0);
        distance.put("Kilometers", 1000.0);
        distance.put("Centimeters", 0.01);
        distance.put("Miles", 1609.34);
        distance.put("Yards", 0.9144);
        distance.put("Feet", 0.3048);
        distance.put("Inches", 0.0254);
        conversionData.put("Distance", distance);

        // 3. Speed
        Map<String, Double> speed = new HashMap<>();
        speed.put("m/s", 1.0);
        speed.put("km/h", 0.277778);
        speed.put("Miles/hour (mph)", 0.44704);
        speed.put("Knots", 0.514444);
        conversionData.put("Speed", speed);

        // 4. Mass
        Map<String, Double> mass = new HashMap<>();
        mass.put("Kilograms", 1.0);
        mass.put("Grams", 0.001);
        mass.put("Tonnes", 1000.0);
        mass.put("Pounds (lbs)", 0.453592);
        mass.put("Ounces", 0.0283495);
        conversionData.put("Mass", mass);

        // 5. Area
        Map<String, Double> area = new HashMap<>();
        area.put("Square Meters", 1.0);
        area.put("Hectares", 10000.0);
        area.put("Acres", 4046.86);
        area.put("Square Feet", 0.092903);
        conversionData.put("Area", area);

        // 6. Pressure
        Map<String, Double> pressure = new HashMap<>();
        pressure.put("Pascals", 1.0);
        pressure.put("Bar", 100000.0);
        pressure.put("Atmospheres (atm)", 101325.0);
        pressure.put("PSI", 6894.76);
        conversionData.put("Pressure", pressure);

        // 7. Volume
        Map<String, Double> volume = new HashMap<>();
        volume.put("Liters", 1.0);
        volume.put("Milliliters", 0.001);
        volume.put("Cubic Meters", 1000.0);
        volume.put("Gallons (US)", 3.78541);
        volume.put("Pints (US)", 0.473176);
        conversionData.put("Volume", volume);

        // 8. Energy
        Map<String, Double> energy = new HashMap<>();
        energy.put("Joules", 1.0);
        energy.put("Kilojoules", 1000.0);
        energy.put("Calories", 4.184);
        energy.put("kWh", 3600000.0);
        conversionData.put("Energy", energy);

        // 9. Temperature
        Map<String, Double> temp = new HashMap<>();
        temp.put("Celsius (°C)", 0.0);
        temp.put("Fahrenheit (°F)", 0.0);
        temp.put("Kelvin (K)", 0.0);
        conversionData.put("Temperature", temp);
    }

    private void updateUnitCombos() {
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        fromUnitCombo.removeAllItems();
        toUnitCombo.removeAllItems();

        if (selectedCategory != null) {
            Map<String, Double> units = conversionData.get(selectedCategory);
            for (String unit : units.keySet()) {
                fromUnitCombo.addItem(unit);
                toUnitCombo.addItem(unit);
            }
        }
    }

    private double convertTemperature(double val, String from, String to) {
        if (from.equals(to)) return val;

        double celsius = switch (from) {
            case "Celsius (°C)" -> val;
            case "Fahrenheit (°F)" -> (val - 32) * 5.0 / 9.0;
            case "Kelvin (K)" -> val - 273.15;
            default -> 0;
        };

        return switch (to) {
            case "Celsius (°C)" -> celsius;
            case "Fahrenheit (°F)" -> (celsius * 9.0 / 5.0) + 32;
            case "Kelvin (K)" -> celsius + 273.15;
            default -> celsius;
        };
    }

    private class ConvertAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String inputStr = inputField.getText();
                if (inputStr.isEmpty()) {
                    resultLabel.setText("Please enter a number!");
                    return;
                }

                String category = (String) categoryCombo.getSelectedItem();
                String from = (String) fromUnitCombo.getSelectedItem();
                String to = (String) toUnitCombo.getSelectedItem();

                if (category == null || from == null || to == null) {
                    resultLabel.setText("Error: Selection missing");
                    return;
                }

                double val = Double.parseDouble(inputStr.replace(",", "."));
                double result;

                if ("Temperature".equals(category)) {
                    result = convertTemperature(val, from, to);
                } else {
                    Map<String, Double> units = conversionData.get(category);
                    double fromFactor = units.get(from);
                    double toFactor = units.get(to);
                    result = (val * fromFactor) / toFactor;
                }

                DecimalFormat df = new DecimalFormat("#.####");
                resultLabel.setText("Result: " + df.format(result) + " " + to);

            } catch (NumberFormatException ex) {
                resultLabel.setText("Invalid Number");
            } catch (Exception ex) {
                resultLabel.setText("Error occurred");
            }
        }
    }
}