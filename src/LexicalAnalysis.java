import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private String state = "H";
    private boolean stop = false;

    public LexicalAnalysis() {
        Arrays.sort(tw);
        Arrays.sort(tl);
        Tables.ti.clear();
        Tables.tn.clear();
    }

    private void writeInFile(String string) {
        try {
            writer.write(string);
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

        // Чтение программы начинается с комментария /*
        if (c == '/') {
            state = "H";
            s.append(c);
            c = gc();
            if (c == '*') {
                s.append(c);
                // Пропускаем начальный комментарий
                skipComment();
            } else {
                er("Ожидался символ '*' после '/' для начала комментария");
                return;
            }
        } else {
            stop = true;
            er("Не найден символ начала комментария '/*'");
        }

        while(!endOfFile) {
            if(stop) return;

            // Комментарии
            if(c == '/' && !endOfFile) {
                s.append(c);
                c = gc();
                if(c == '*') {
                    s.append(c);
                    skipComment();
                    continue;
                } else {
                    // Это оператор деления
                    state = "L";
                    writeInFile(search(s));
                    s.delete(0, s.length());
                    continue;
                }
            }

            // Конец программы
            else if (state.equals("PROGRAM") && c == '/' && !endOfFile) {
                s.append(c);
                c = gc();
                if(c == '/') {
                    s.append(c);
                    if(s.toString().equals("//")) {
                        writeInFile(search(s));
                        break;
                    }
                } else {
                    er("Ожидался символ '/' для конца программы");
                }
            }

            // Операции отношения
            else if(c == '<') {
                state = "<";
                s.append(c);
                c = gc();
                if(c == '=') {
                    state = "<=";
                    s.append(c);
                    writeInFile(search(s));
                } else if(c == '>') {
                    state = "<>";
                    s.append(c);
                    writeInFile(search(s));
                } else {
                    writeInFile(search(s));
                }
                s.delete(0, s.length());
                continue;
            }
            else if(c == '>') {
                state = ">";
                s.append(c);
                c = gc();
                if(c == '=') {
                    state = ">=";
                    s.append(c);
                    writeInFile(search(s));
                } else {
                    writeInFile(search(s));
                }
                s.delete(0, s.length());
                continue;
            }
            else if(c == '=') {
                state = "=";
                s.append(c);
                writeInFile(search(s));
                s.delete(0, s.length());
            }

            // Числа
            else if(Character.isDigit(c)) {
                readNumber();
                s.delete(0, s.length());
                continue;
            }

            // Идентификаторы и ключевые слова
            else if(Character.isLetter(c)) {
                state = "I";
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

            // Операторы и разделители
            else if(isOperatorOrDelimiter(c)) {
                state = "L";
                s.append(c);

                // Проверяем многосимвольные операторы
                if(c == ':' && !endOfFile) {
                    c = gc();
                    if(c == '=') {
                        s.append(c);
                        writeInFile(search(s));
                    } else {
                        writeInFile(search(s));
                        continue;
                    }
                } else {
                    writeInFile(search(s));
                }

                s.delete(0, s.length());
            }

            // Пробельные символы
            else if(Character.isWhitespace(c)) {
                if(c == '\n') {
                    s.append(c);
                    writeInFile(search(s));
                    s.delete(0, s.length());
                }
                c = gc();
                continue;
            }
            else {
                er("Неопознанный символ '" + c + "'");
            }

            c = gc();
            s.delete(0, s.length());
        }

        reader.close();
        writer.close();

        Main.ui.log("Лексический анализ успешно завершен");
        // Вывод таблиц после лексического анализа
        Tables.printTables();
        new SyntaxAnalysis().analysis();
    }

    private void skipComment() throws IOException {
        while(!endOfFile) {
            c = gc();
            if(c == '*' && !endOfFile) {
                c = gc();
                if(c == '/') {
                    break;
                }
            }
        }
        c = gc(); // читаем следующий символ после комментария
        state = "PROGRAM";
    }

    private void readNumber() {
        state = "NUM";
        boolean isFloat = false;
        boolean hasExponent = false;

        while(Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' ||
                c == '+' || c == '-' || Character.isLetter(c)) {

            if(c == '.') {
                if(isFloat) {
                    er("Несколько точек в числе");
                    return;
                }
                isFloat = true;
            }

            if(c == 'e' || c == 'E') {
                if(hasExponent) {
                    er("Несколько экспонент в числе");
                    return;
                }
                hasExponent = true;
                s.append(c);
                c = gc();

                if(c == '+' || c == '-') {
                    s.append(c);
                    c = gc();
                }
                continue;
            }

            // Проверка систем счисления
            if(Character.isLetter(c) && c != 'e' && c != 'E') {
                if(c == 'B' || c == 'b') {
                    s.append(c);
                    writeInFile(search(s));
                    return;
                } else if(c == 'O' || c == 'o') {
                    s.append(c);
                    writeInFile(search(s));
                    return;
                } else if(c == 'D' || c == 'd') {
                    s.append(c);
                    writeInFile(search(s));
                    return;
                } else if(c == 'H' || c == 'h') {
                    s.append(c);
                    writeInFile(search(s));
                    return;
                }
            }

            s.append(c);
            c = gc();
        }

        writeInFile(search(s));
    }

    private char gc() {
        try {
            int ch = reader.read();
            if(ch == -1) {
                endOfFile = true;
            }
            return (char) ch;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String search(StringBuilder s) {
        String str = s.toString();
        int table;
        int index = BinarySearch.getLexemeIndex(tw, str);

        if(index == -1) {
            index = BinarySearch.getLexemeIndex(tl, str);
            if(index == -1) {
                if(str.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
                    table = 4;
                    boolean found = false;
                    for(int i = 0; i < ti.size(); i++) {
                        if(str.equals(ti.get(i).getName())) {
                            index = i;
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        ti.add(new Identifier(str));
                        index = ti.size() - 1;
                    }
                }
                else if(str.matches("^-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?$") ||
                        str.matches("^[01]+[Bb]$") ||
                        str.matches("^[0-7]+[Oo]$") ||
                        str.matches("^\\d+[Dd]?$") ||
                        str.matches("^[0-9A-Fa-f]+[Hh]$")) {
                    table = 3;
                    tn.add(str);
                    index = tn.size() - 1;
                }
                else {
                    table = 2;
                    // Добавляем новый разделитель
                    String[] newTl = Arrays.copyOf(tl, tl.length + 1);
                    newTl[newTl.length - 1] = str;
                    index = newTl.length - 1;
                }
            }
            else table = 2;
        }
        else {
            table = 1;
        }

        return "[" + table + "," + index + "]";
    }

    private void er(String message) {
        state = "ER";
        Main.ui.log("Лексическая ошибка: " + message);
        stop = true;
    }

    private boolean isOperatorOrDelimiter(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == ':' ||
                c == ',' || c == '[' || c == ']' || c == '(' || c == ')' ||
                c == '=' || c == '<' || c == '>';
    }
}