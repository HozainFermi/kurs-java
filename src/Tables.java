import java.util.ArrayList;

public class Tables {
    // Таблица служебных слов (обновлена под ваш язык)
    public static final String[] tw = {"true", "false", "if", "then", "else", "end", "for", "val", "do",
            "while", "next", "enter", "displ", "int", "float", "bool", "not", "or", "and"};

    // Таблица ограничителей (обновлена под ваш язык)
    public static final String[] tl = {"<>", "=", "<", "<=", ">", ">=", "+", "-", "*", "/",
            ":", ",", "assign", "[", "]", "(", ")", "\n", " "};

    // Таблица чисел
    public static final ArrayList<String> tn = new ArrayList<>();
    public static final ArrayList<Identifier> ti = new ArrayList<>();

    public static void printTables() {
        Main.ui.printInTableArea("Таблица служебных слов");
        for(int i=0; i<tw.length; i++) {
            Main.ui.printInTableArea(i + ")  " + tw[i]);
        }

        Main.ui.printInTableArea();

        Main.ui.printInTableArea("Таблица ограничителей");
        for(int i=0; i<tl.length; i++) {
            Main.ui.printInTableArea(i + ")  " + tl[i]);
        }

        Main.ui.printInTableArea();

        Main.ui.printInTableArea("Таблица чисел");
        for(int i=0; i<tn.size(); i++) {
            Main.ui.printInTableArea(i + ")  " + tn.get(i));
        }

        Main.ui.printInTableArea();

        Main.ui.printInTableArea("Таблица идентификаторов");
        for(int i=0; i<ti.size(); i++) {
            Main.ui.printInTableArea(i + ")  " + ti.get(i).getName());
        }
    }
}