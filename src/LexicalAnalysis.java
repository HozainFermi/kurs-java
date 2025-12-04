import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class LexicalAnalysis {
    private final String[] tw = Tables.tw;
    private final String[] tl = Tables.tl;
    private final ArrayList<String> tn = Tables.tn;
    private final ArrayList<Identifier> ti = Tables.ti;
    private FileReader reader;
    private FileWriter writer;
    private boolean endOfFile = false;
    private final StringBuilder s = new StringBuilder();
    private char c;
    private boolean stop = false;
    private boolean inComment = false;
    private int nextChar = -1;

    public LexicalAnalysis() {
        Arrays.sort(tw);
        Arrays.sort(tl);
        Tables.ti.clear();
        Tables.tn.clear();
    }

    private void writeInFile(String string) {
        try {
            writer.write(string + " ");
            Main.ui.addLexemeInLexemesArea(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void analysis() throws IOException {
        try {
            File file = new File("lexeme.txt");
            if (file.createNewFile()) {
                Main.ui.log("Файл лексем создан");
            }
        } catch (IOException e) {
            Main.ui.log("Ошибка при создании файла лексем");
            System.exit(1);
        }
        writer = new FileWriter("lexeme.txt");
        reader = new FileReader("program.txt");

        Main.ui.log("Запущен лексический анализ");

        c = gc();

        while (!endOfFile) {
            if (stop) return;

            if (c == ' ' || c == '\t') {
                c = gc();
                continue;
            }

            // Обработка комментариев
            if (c == '/' && !endOfFile) {
                int next = peekChar();
                if (next == '*') {
                    // Начало комментария
                    gc(); // пропускаем '*'
                    inComment = true;
                    boolean commentClosed = false;

                    while (!endOfFile) {
                        c = gc();
                        if (c == '*' && !endOfFile) {
                            next = peekChar();
                            if (next == '/') {
                                gc(); // пропускаем '/'
                                commentClosed = true;
                                inComment = false;
                                c = gc();
                                break;
                            }
                        }
                    }

                    // проверяем, закрыт ли комментарий
                    if (!commentClosed && endOfFile) {
                        er("Лексическая ошибка: незакрытый комментарий");
                        return;
                    }

                    continue;
                } else {
                    // Это оператор деления
                    s.append(c);
                    writeInFile(search(s));
                    s.delete(0, s.length());
                    c = gc();
                    continue;
                }
            }

            if (inComment) {
                c = gc();
                continue;
            }

            if (c == '\n') {
                s.append(c);
                writeInFile(search(s));
                s.delete(0, s.length());
                c = gc();
                continue;
            }

            // Операции отношения
            if (c == '<') {
                s.append(c);
                int next = peekChar();
                if (next == '=') {
                    gc();
                    s.append(c);
                    writeInFile(search(s));
                } else if (next == '>') {
                    gc();
                    s.append(c);
                    writeInFile(search(s));
                } else {
                    writeInFile(search(s));
                }
                s.delete(0, s.length());
                c = gc();
                continue;
            }

            if (c == '>') {
                s.append(c);
                int next = peekChar();
                if (next == '=') {
                    gc();
                    s.append(c);
                    writeInFile(search(s));
                } else {
                    writeInFile(search(s));
                }
                s.delete(0, s.length());
                c = gc();
                continue;
            }

            if (c == '=') {
                s.append(c);
                writeInFile(search(s));
                s.delete(0, s.length());
                c = gc();
                continue;
            }

            // Числа
            if (Character.isDigit(c) || c=='.') {
                readNumber();
                continue;
            }

            // Идентификаторы и ключевые слова
            if (Character.isLetter(c)) {
                s.append(c);
                c = gc();
                while (Character.isLetterOrDigit(c)) {
                    s.append(c);
                    c = gc();
                }
                writeInFile(search(s));
                s.delete(0, s.length());
                continue;
            }

            // Одиночные символы
            if (isDelimiter(c)) {
                s.append(c);
                writeInFile(search(s));
                s.delete(0, s.length());
                c = gc();
                continue;
            }

            er("Неопознанный символ: '" + c + "'");
            break;
        }

        reader.close();
        writer.close();

        Main.ui.log("Лексический анализ успешно завершен");
        Tables.printTables();
        new SyntaxAnalysis().analysis();
    }

    private char gc() {
        try {
            if (nextChar != -1) {
                c = (char) nextChar;
                nextChar = -1;
                return c;
            }

            int ch = reader.read();
            if (ch == -1) {
                endOfFile = true;
                return '\0';
            }
            c = (char) ch;
            return c;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int peekChar() {
        try {
            if (nextChar == -1) {
                nextChar = reader.read();
            }
            return nextChar;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readNumber() {
        s.setLength(0);
        s.append(c);

        boolean hasDecimal = false;
        boolean hasExponent = false;

        c = gc();

        while (!endOfFile) {
            if (Character.isDigit(c)) {
                s.append(c);
                c = gc();

                // конец числа
                if (c == -1 || c == ' ' || c == '\n' || c == ':' || c == ',' ||
                        c == ';' || c == ')' || c == ']' || isDelimiter(c)) {

                    String numberStr = s.toString();
                    boolean isValid = numberStr.matches("[0-9]+");
                    if (!isValid) {
                        er("Лексическая ошибка: число не соответствует системе счисления '" + "D" + "'");
                        return;
                    }
                }

            } else if (c == '.') {
                if (hasDecimal) {
                    er("Лексическая ошибка: несколько точек в числе");
                    return;
                }
                hasDecimal = true;
                s.append(c);
                c = gc();

                if (!Character.isDigit(c)) {
                    er("Лексическая ошибка: после точки в числе должны быть цифры");
                    return;
                }
            } else if (c == 'e' || c == 'E') {
                if (hasExponent) {
                    er("Лексическая ошибка: несколько экспонент в числе");
                    return;
                }
                hasExponent = true;
                s.append(c);
                c = gc();

                if (c == '+' || c == '-') {
                    s.append(c);
                    c = gc();
                }

                if (!Character.isDigit(c)) {
                    er("Лексическая ошибка: после экспоненты в числе должны быть цифры");
                    return;
                }
            } else if (Character.isLetter(c)) {
                // Суффиксы систем счисления
                int nextChar = peekChar();
                char next = (char) nextChar;

                // Если после суффикса будет разделитель - это валидное число
                if (nextChar == -1 || next == ' ' || next == '\n' || next == ':' || next == ',' ||
                        next == ';' || next == ')' || next == ']' || isDelimiter(next)) {

                    // Добавляем суффикс
                    s.append(c);
                    String numberStr = s.toString();
                    String numberWithoutSuffix = numberStr.substring(0, numberStr.length() - 1);
                    char suffix = Character.toUpperCase(numberStr.charAt(numberStr.length() - 1));

                    // Проверяем соответствие системы счисления
                    boolean isValid = true;

                    if (Character.isDigit(suffix)){
                        isValid = numberStr.matches("[0-9]+");
                    }
                    if (!isValid) {
                        er("Лексическая ошибка: число не соответствует системе счисления '" + "D" + "'");
                        return;
                    }

                    switch (suffix) {
                        case 'B':
                            // Двоичная - только 0 и 1
                            isValid = numberWithoutSuffix.matches("[01]+");
                            break;
                        case 'O':
                            // Восьмеричная - только 0-7
                            isValid = numberWithoutSuffix.matches("[0-7]+");
                            break;
                        case 'D':
                            // Десятичная - только 0-9
                            isValid = numberWithoutSuffix.matches("[0-9]+");
                            break;
                        case 'H':
                            // Шестнадцатеричная - только 0-9, A-F
                            isValid = numberWithoutSuffix.toUpperCase().matches("[0-9A-F]+");
                            break;
                        default:
                            // Неизвестный суффикс
                            er("Лексическая ошибка: неизвестный суффикс системы счисления '" + suffix + "'");
                            return;
                    }

                    if (!isValid) {
                        er("Лексическая ошибка: число не соответствует системе счисления '" + suffix + "'");
                        return;
                    }

                    // Число валидно - записываем
                    writeInFile(search(s));
                    s.delete(0, s.length());
                    c = gc();
                    return;
                } else {
                    // Если после суффикса НЕ разделитель - продолжаем читать как число
                    s.append(c);
                    c = gc();
                }
            } else {
                break; // не числовой символ
            }
        }

        writeInFile(search(s));
        s.delete(0, s.length());
    }

    public String search(StringBuilder s) {
        String str = s.toString();
        int table;
        int index = BinarySearch.getLexemeIndex(tw, str);

        if (index == -1) {
            index = BinarySearch.getLexemeIndex(tl, str);
            if (index == -1) {
                if (str.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
                    table = 4;
                    boolean found = false;
                    for (int i = 0; i < ti.size(); i++) {
                        if (str.equals(ti.get(i).getName())) {
                            index = i;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ti.add(new Identifier(str));
                        index = ti.size() - 1;
                    }
                } else {
                    table = 3;
                    boolean found = false;
                    for (int i = 0; i < tn.size(); i++) {
                        if (str.equals(tn.get(i))) {
                            index = i;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        tn.add(str);
                        index = tn.size() - 1;
                    }
                }
            } else {
                table = 2;
            }
        } else {
            table = 1;
        }

        return "[" + table + "," + index + "]";
    }

    private void er(String message) {
        Main.ui.log("Лексическая ошибка: " + message);
        stop = true;
    }

    private boolean isDelimiter(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' ||
                c == ')' || c == '[' || c == ']' || c == ':' || c == ';' ||
                c == ',' || c == '=' || c == '<' || c == '>';
    }

}