import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TextAnalyzerApp extends JFrame {

    private final JTextArea resultArea;

    public TextAnalyzerApp() {
        // Configure frame
        setTitle("Статистичний аналізатор тексту");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Load button
        JButton loadButton = new JButton("Обрати файл (.txt)");
        loadButton.setFont(new Font("Arial", Font.BOLD, 14));
        loadButton.addActionListener(this::chooseFile);

        // Result
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(loadButton, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void chooseFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        // Only txt files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            analyzeFile(selectedFile);
        }
    }

    private void analyzeFile(File file) {
        try {
            String content = Files.readString(file.toPath());

            if (content.isBlank()) {
                resultArea.setText("Файл порожній.");
                return;
            }

            // 1. Number of punctuation marks using regex
            long punctuationCount = content.chars()
                    .filter(c -> Pattern.matches("\\p{Punct}", String.valueOf((char) c)))
                    .count();

            // 2. Split to sentences (by period, exclamation mark, question mark)
            String[] sentences = content.split("[.!?]+");
            int sentenceCount = sentences.length;

            // 3. Split to words
            // \\P{L}+ means “split at everything that is NOT a letter OR a number” (ignores spaces, commas)
            String[] rawWords = content.split("[^\\p{L}\\p{N}]+");
            // Filter empty lines that may appear when splitting
            List<String> words = Arrays.stream(rawWords)
                    .filter(w -> !w.isEmpty())
                    .toList();

            long totalWords = words.size();

            // 4. Unique words and frequency (HashMap)
            Map<String, Integer> frequencyMap = new HashMap<>();
            long totalWordLength = 0;

            for (String word : words) {
                // To lowerCase for correct counting
                String lowerWord = word.toLowerCase();
                frequencyMap.put(lowerWord, frequencyMap.getOrDefault(lowerWord, 0) + 1);
                totalWordLength += word.length();
            }

            int uniqueWordsCount = frequencyMap.size();

            // 5. Average values
            double avgWordLength = totalWords > 0 ? (double) totalWordLength / totalWords : 0;
            double avgSentenceLength = sentenceCount > 0 ? (double) totalWords / sentenceCount : 0;

            // 6. Top 10
            List<Map.Entry<String, Integer>> top10Words = frequencyMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Сортування за спаданням
                    .limit(10)
                    .toList();

            // Report
            StringBuilder report = new StringBuilder();
            report.append("=== Результати аналізу ===\n\n");
            report.append(String.format("Загальна кількість слів: %d\n", totalWords));
            report.append(String.format("Кількість унікальних слів: %d\n", uniqueWordsCount));
            report.append(String.format("Кількість речень: %d\n", sentenceCount));
            report.append(String.format("Кількість знаків пунктуації: %d\n", punctuationCount));
            report.append(String.format("Середня довжина слова: %.2f символів\n", avgWordLength));
            report.append(String.format("Середня довжина речення: %.2f слів\n", avgSentenceLength));

            report.append("\n--- Топ 10 найчастіших слів ---\n");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : top10Words) {
                report.append(String.format("%d. %-15s : %d разів\n", rank++, entry.getKey(), entry.getValue()));
            }

            resultArea.setText(report.toString());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Помилка при зчитуванні файлу: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextAnalyzerApp().setVisible(true));
    }
}