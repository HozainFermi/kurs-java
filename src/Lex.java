public class Lex {
    private int table;
    private int index;

    public void set(int table, int index) {
        this.table = table;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean ID() {
        return table == 4;
    }

    public boolean NUM() {
        return table == 3;
    }

    public boolean EQ(String s) {
        if (table == 1) {
            return Tables.tw[index].equals(s);
        }
        else if (table == 2) {
            return Tables.tl[index].equals(s);
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + table + "," + index + "]";
    }
}