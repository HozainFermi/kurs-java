import java.util.Objects;

public class TOP {
    private String operation;
    private String type1;
    private String type2;
    private String resultType;

    public TOP(String operation, String type1, String type2, String resultType) {
        this.operation = operation;
        this.type1 = type1;
        this.type2 = type2;
        this.resultType = resultType;
    }

    public String getOperation() {
        return operation;
    }

    public String getType1() {
        return type1;
    }

    public String getType2() {
        return type2;
    }

    public String getResultType() {
        return resultType;
    }
}