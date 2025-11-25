import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI extends JFrame {
    private JTextArea codeArea;
    private JTextArea lexemesArea;
    private JTextArea tableArea;
    private JTextArea logArea;
    private JButton analyzeButton;
    private JButton clearButton;

    public UI() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Анализатор языка программирования");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Создание основной панели
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        analyzeButton = new JButton("Анализировать");
        clearButton = new JButton("Очистить");

        buttonPanel.add(analyzeButton);
        buttonPanel.add(clearButton);

        // Панель с текстовыми областями
        JPanel textPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        // Область для ввода кода
        JPanel codePanel = createTextPanel("Исходный код:", true);
        codeArea = new JTextArea();
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane codeScroll = new JScrollPane(codeArea);
        ((JPanel) codePanel.getComponent(1)).add(codeScroll);

        // Область для лексем
        JPanel lexemesPanel = createTextPanel("Лексемы:", false);
        lexemesArea = new JTextArea();
        lexemesArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        lexemesArea.setEditable(false);
        JScrollPane lexemesScroll = new JScrollPane(lexemesArea);
        ((JPanel) lexemesPanel.getComponent(1)).add(lexemesScroll);

        // Область для таблиц
        JPanel tablePanel = createTextPanel("Таблицы:", false);
        tableArea = new JTextArea();
        tableArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        tableArea.setEditable(false);
        JScrollPane tableScroll = new JScrollPane(tableArea);
        ((JPanel) tablePanel.getComponent(1)).add(tableScroll);

        // Область для логов
        JPanel logPanel = createTextPanel("Лог выполнения:", false);
        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        ((JPanel) logPanel.getComponent(1)).add(logScroll);

        // Добавление панелей в основную сетку
        textPanel.add(codePanel);
        textPanel.add(lexemesPanel);
        textPanel.add(tablePanel);
        textPanel.add(logPanel);

        // Добавление компонентов на основную панель
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(textPanel, BorderLayout.CENTER);

        // Обработчики событий
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeCode();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createTextPanel(String title, boolean editable) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title);
        JPanel contentPanel = new JPanel(new BorderLayout());

        panel.add(label, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private void analyzeCode() {
        // Очистка предыдущих результатов
        lexemesArea.setText("");
        tableArea.setText("");
        logArea.setText("");

        // Сохранение кода в файл
        try {
            java.io.FileWriter writer = new java.io.FileWriter("program.txt");
            writer.write(codeArea.getText());
            writer.close();

            // Запуск анализа
            LexicalAnalysis lexicalAnalysis = new LexicalAnalysis();
            lexicalAnalysis.analysis();

        } catch (Exception ex) {
            log("Ошибка: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearAll() {
        codeArea.setText("");
        lexemesArea.setText("");
        tableArea.setText("");
        logArea.setText("");
        Tables.ti.clear();
        Tables.tn.clear();
    }

    public void log(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logArea.append(message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    public void addLexemeInLexemesArea(String lexeme) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lexemesArea.append(lexeme + "\n");
            }
        });
    }

    public void printInTableArea(String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableArea.append(text + "\n");
                tableArea.setCaretPosition(tableArea.getDocument().getLength());
            }
        });
    }

    public void printInTableArea() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableArea.append("\n");
            }
        });
    }
}