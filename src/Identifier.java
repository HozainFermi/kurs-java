public class Identifier implements Comparable<Identifier>{
    private String name;
    private boolean described = false;
    private String type;

    public Identifier(String name) {
        this.name = name;
    }

    public boolean isDescribed() {
        return described;
    }

    public void setDescribed(boolean described) {
        this.described = described;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int compareTo(Identifier o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "name='" + name + '\'' +
                ", described=" + described +
                ", type='" + type + '\'' +
                '}';
    }
}