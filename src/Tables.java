import java.util.ArrayList;

public class Tables {
    // Таблица служебных слов (исправлена согласно вашим данным)
    public static final String[] tw = {
            "int", "float", "bool", "do", "else", "false", "for",
            "if", "then", "true", "while", "end", "val", "displ",
            "enter", "next"
    };

    // Таблица ограничителей (исправлена согласно вашим данным)
    public static final String[] tl = {
            "\n", "(", ")", "*", "+", ",", "-", "/", ":", ";",
            "<", "<=", "<>", "=", ">", ">=", "[", "]", "assign",
            "or", "and"
    };

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