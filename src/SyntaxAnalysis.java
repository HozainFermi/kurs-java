import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

public class SyntaxAnalysis {
    private final ArrayList<Identifier> ti = Tables.ti;
    private final BufferedInputStream bufferedInputStream;

    {
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream("lexeme.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final Lex lex = new Lex();
    private final ArrayList<Integer> stack = new ArrayList<>();
    private final Stack<String> opStack = new Stack<>();
    private final ArrayList<TOP> top = new ArrayList<>();
    private boolean stop = false;

    public void analysis(){
        // Инициализация таблицы операций (адаптирована под ваш язык)
        initializeTOP();

        Main.ui.log("Запущен синтаксический анализ");
        Main.ui.log("Запущен семантический анализ");

        gl();

        // Программа начинается с комментария /*
        if(lex.EQ("/*")) {
            gl();
        } else {
            er("Синтаксическая ошибка: не найден начальный комментарий '/*'");
        }

        // Основное тело программы
        while(!lex.EQ("*/") && !stop) {
            if(description() || operator()) {
                if(lex.EQ(":") || lex.EQ("\n")) {
                    gl();
                } else if(!lex.EQ("*/")) {
                    er("Синтаксическая ошибка: ожидался ':' или перевод строки после оператора");
                }
            } else if(!lex.EQ("*/")) {
                er("Синтаксическая ошибка: ожидалось описание или оператор");
            }
        }

        if(!lex.EQ("*/")) {
            er("Синтаксическая ошибка: не найден конечный комментарий '*/'");
        }

        if(!lex.EQ("end")) {
            er("Синтаксическая ошибка: не найдено ключевое слово 'end'");
        }

        if(stop) return;

        Main.ui.log("Синтаксический анализ успешно завершен");
        Main.ui.log("Семантический анализ успешно завершен");
    }

    private void initializeTOP() {
        // Операции присваивания
        top.add(new TOP("assign", "int", "int", "int"));
        top.add(new TOP("assign", "float", "float", "float"));
        top.add(new TOP("assign", "bool", "bool", "bool"));
        top.add(new TOP("assign", "int", "float", "float"));
        top.add(new TOP("assign", "float", "int", "float"));

        // Арифметические операции
        top.add(new TOP("+", "int", "int", "int"));
        top.add(new TOP("-", "int", "int", "int"));
        top.add(new TOP("*", "int", "int", "int"));
        top.add(new TOP("/", "int", "int", "float"));

        top.add(new TOP("+", "float", "float", "float"));
        top.add(new TOP("-", "float", "float", "float"));
        top.add(new TOP("*", "float", "float", "float"));
        top.add(new TOP("/", "float", "float", "float"));

        top.add(new TOP("+", "int", "float", "float"));
        top.add(new TOP("-", "int", "float", "float"));
        top.add(new TOP("*", "int", "float", "float"));
        top.add(new TOP("/", "int", "float", "float"));

        // Операции отношения
        top.add(new TOP("<", "int", "int", "bool"));
        top.add(new TOP("<=", "int", "int", "bool"));
        top.add(new TOP(">", "int", "int", "bool"));
        top.add(new TOP(">=", "int", "int", "bool"));
        top.add(new TOP("=", "int", "int", "bool"));
        top.add(new TOP("<>", "int", "int", "bool"));

        top.add(new TOP("<", "float", "float", "bool"));
        top.add(new TOP("<=", "float", "float", "bool"));
        top.add(new TOP(">", "float", "float", "bool"));
        top.add(new TOP(">=", "float", "float", "bool"));
        top.add(new TOP("=", "float", "float", "bool"));
        top.add(new TOP("<>", "float", "float", "bool"));

        // Логические операции
        top.add(new TOP("and", "bool", "bool", "bool"));
        top.add(new TOP("or", "bool", "bool", "bool"));
        top.add(new TOP("not", "bool", "#", "bool"));
    }

    private boolean description() {
        if(!type()) {
            return false;
        }

        String varType = getTypeFromLexeme();
        gl();

        if(!lex.EQ(":")) {
            er("Синтаксическая ошибка: ожидался ':' после типа");
            return false;
        }
        gl();

        if(!identifier()) {
            er("Синтаксическая ошибка: ожидался идентификатор");
            return false;
        }

        stack.add(lex.getIndex());
        gl();

        while(lex.EQ(",")) {
            gl();
            if(!identifier()) {
                er("Синтаксическая ошибка: ожидался идентификатор после ','");
                return false;
            }
            stack.add(lex.getIndex());
            gl();
        }

        // Семантический анализ: установка типов переменных
        for(int index : stack) {
            Identifier id = ti.get(index);
            if(id.isDescribed()) {
                er("Семантическая ошибка: переменная '" + id.getName() + "' уже описана");
            }
            id.setDescribed(true);
            id.setType(varType);
        }

        stack.clear();
        return true;
    }

    private boolean operator() {
        return compound() || assignment() || conditional() || fixedLoop() ||
                conditionalLoop() || input() || output();
    }

    private boolean compound() {
        if(!lex.EQ("[")) {
            return false;
        }
        gl();

        if(!operator()) {
            er("Синтаксическая ошибка: ожидался оператор в составном операторе");
            return false;
        }

        while(lex.EQ(":") || lex.EQ("\n")) {
            gl();
            if(!operator()) {
                er("Синтаксическая ошибка: ожидался оператор после ':' или перевода строки");
                return false;
            }
        }

        if(!lex.EQ("]")) {
            er("Синтаксическая ошибка: ожидался ']' для окончания составного оператора");
            return false;
        }
        gl();

        return true;
    }

    private boolean assignment() {
        if(!identifier()) {
            return false;
        }
        String idType = getIdentifierType();
        gl();

        if(!lex.EQ("assign")) {
            return false;
        }
        opStack.push("assign");
        gl();

        if(!expression()) {
            er("Синтаксическая ошибка: ожидалось выражение в операторе присваивания");
            return false;
        }

        String exprType = opStack.pop();
        checkAssignmentTypes(idType, exprType);

        return true;
    }

    private boolean conditional() {
        if(!lex.EQ("if")) {
            return false;
        }
        gl();

        if(!expression()) {
            er("Синтаксическая ошибка: ожидалось выражение в условном операторе");
            return false;
        }

        String exprType = opStack.pop();
        if(!exprType.equals("bool")) {
            er("Семантическая ошибка: условие должно быть логического типа");
        }

        if(!lex.EQ("then")) {
            er("Синтаксическая ошибка: ожидалось 'then'");
            return false;
        }
        gl();

        if(!operator()) {
            er("Синтаксическая ошибка: ожидался оператор после 'then'");
            return false;
        }

        if(lex.EQ("else")) {
            gl();
            if(!operator()) {
                er("Синтаксическая ошибка: ожидался оператор после 'else'");
                return false;
            }
        }

        if(!lex.EQ("end")) {
            er("Синтаксическая ошибка: ожидалось 'end' для окончания условного оператора");
            return false;
        }
        gl();

        return true;
    }

    private boolean fixedLoop() {
        if(!lex.EQ("for")) {
            return false;
        }
        gl();

        if(!assignment()) {
            er("Синтаксическая ошибка: ожидалось присваивание в цикле for");
            return false;
        }

        if(!lex.EQ("val")) {
            er("Синтаксическая ошибка: ожидалось 'val'");
            return false;
        }
        gl();

        if(!expression()) {
            er("Синтаксическая ошибка: ожидалось выражение после 'val'");
            return false;
        }

        String exprType = opStack.pop();
        if(!exprType.equals("int") && !exprType.equals("float")) {
            er("Семантическая ошибка: граница цикла должна быть числового типа");
        }

        if(!lex.EQ("do")) {
            er("Синтаксическая ошибка: ожидалось 'do'");
            return false;
        }
        gl();

        if(!operator()) {
            er("Синтаксическая ошибка: ожидался оператор в цикле");
            return false;
        }

        return true;
    }

    private boolean conditionalLoop() {
        if(!lex.EQ("while")) {
            return false;
        }
        gl();

        if(!expression()) {
            er("Синтаксическая ошибка: ожидалось выражение в цикле while");
            return false;
        }

        String exprType = opStack.pop();
        if(!exprType.equals("bool")) {
            er("Семантическая ошибка: условие цикла должно быть логического типа");
        }

        if(!lex.EQ("do")) {
            er("Синтаксическая ошибка: ожидалось 'do'");
            return false;
        }
        gl();

        if(!operator()) {
            er("Синтаксическая ошибка: ожидался оператор в цикле");
            return false;
        }

        if(!lex.EQ("next")) {
            er("Синтаксическая ошибка: ожидалось 'next' для окончания цикла");
            return false;
        }
        gl();

        return true;
    }

    private boolean input() {
        if(!lex.EQ("enter")) {
            return false;
        }
        gl();

        if(!identifier()) {
            er("Синтаксическая ошибка: ожидался идентификатор в операторе ввода");
            return false;
        }
        checkIdentifierDescription();
        gl();

        while(lex.EQ(" ")) {
            gl();
            if(!identifier()) {
                er("Синтаксическая ошибка: ожидался идентификатор после пробела");
                return false;
            }
            checkIdentifierDescription();
            gl();
        }

        return true;
    }

    private boolean output() {
        if(!lex.EQ("displ")) {
            return false;
        }
        gl();

        if(!expression()) {
            er("Синтаксическая ошибка: ожидалось выражение в операторе вывода");
            return false;
        }
        opStack.pop();

        while(lex.EQ(",")) {
            gl();
            if(!expression()) {
                er("Синтаксическая ошибка: ожидалось выражение после ','");
                return false;
            }
            opStack.pop();
        }

        return true;
    }

    private boolean expression() {
        if(!operand()) {
            return false;
        }

        while(relationGroupOperations()) {
            String op = getCurrentOperation();
            gl();
            if(!operand()) {
                er("Синтаксическая ошибка: ожидался операнд после операции отношения");
                return false;
            }
            checkOperation(op);
        }

        return true;
    }

    private boolean operand() {
        if(!summand()) {
            return false;
        }

        while(additionalGroupOperations()) {
            String op = getCurrentOperation();
            gl();
            if(!summand()) {
                er("Синтаксическая ошибка: ожидалось слагаемое после операции сложения");
                return false;
            }
            checkOperation(op);
        }

        return true;
    }

    private boolean summand() {
        if(!multiplier()) {
            return false;
        }

        while(multiplicationGroupOperations()) {
            String op = getCurrentOperation();
            gl();
            if(!multiplier()) {
                er("Синтаксическая ошибка: ожидался множитель после операции умножения");
                return false;
            }
            checkOperation(op);
        }

        return true;
    }

    private boolean multiplier() {
        if(identifier()) {
            checkIdentifierDescription();
            gl();
            return true;
        } else if(number()) {
            gl();
            return true;
        } else if(booleanConstant()) {
            gl();
            return true;
        } else if(unaryOperation()) {
            gl();
            if(!multiplier()) {
                er("Синтаксическая ошибка: ожидался множитель после унарной операции");
                return false;
            }
            checkUnaryOperation();
            return true;
        } else if(lex.EQ("(")) {
            gl();
            if(!expression()) {
                er("Синтаксическая ошибка: ожидалось выражение в скобках");
                return false;
            }
            if(!lex.EQ(")")) {
                er("Синтаксическая ошибка: ожидалась закрывающая скобка");
                return false;
            }
            gl();
            return true;
        }
        return false;
    }

    private boolean type() {
        return lex.EQ("int") || lex.EQ("float") || lex.EQ("bool");
    }

    private boolean identifier() {
        return lex.ID();
    }

    private boolean number() {
        return lex.NUM();
    }

    private boolean booleanConstant() {
        return lex.EQ("true") || lex.EQ("false");
    }

    private boolean unaryOperation() {
        return lex.EQ("not");
    }

    private boolean relationGroupOperations() {
        return lex.EQ("<>") || lex.EQ("=") || lex.EQ("<") || lex.EQ("<=") ||
                lex.EQ(">") || lex.EQ(">=");
    }

    private boolean additionalGroupOperations() {
        return lex.EQ("+") || lex.EQ("-") || lex.EQ("or");
    }

    private boolean multiplicationGroupOperations() {
        return lex.EQ("*") || lex.EQ("/") || lex.EQ("and");
    }

    private void gl() {
        try {
            int c;
            c = bufferedInputStream.read();

            StringBuilder table = new StringBuilder();
            StringBuilder index = new StringBuilder();
            if(c == -1) {
                return;
            }
            if ((char) c == '[') {
                c = bufferedInputStream.read();
                while ((char) c != ',') {
                    table.append((char) c);
                    c = bufferedInputStream.read();
                }
                c = bufferedInputStream.read();
                while ((char) c != ']') {
                    index.append((char) c);
                    c = bufferedInputStream.read();
                }
            }
            lex.set(Integer.parseInt(table.toString()), Integer.parseInt(index.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void er(String message) {
        Main.ui.log(message);
        stop = true;
    }

    private String getTypeFromLexeme() {
        if(lex.EQ("int")) return "int";
        if(lex.EQ("float")) return "float";
        if(lex.EQ("bool")) return "bool";
        return "";
    }

    private String getIdentifierType() {
        int index = lex.getIndex();
        if(index >= 0 && index < ti.size()) {
            return ti.get(index).getType();
        }
        return "";
    }

    private void checkIdentifierDescription() {
        int index = lex.getIndex();
        if(index >= 0 && index < ti.size()) {
            Identifier id = ti.get(index);
            if(!id.isDescribed()) {
                er("Семантическая ошибка: идентификатор '" + id.getName() + "' не описан");
            } else {
                opStack.push(id.getType());
            }
        }
    }

    private String getCurrentOperation() {
        if(lex.EQ("<>")) return "<>";
        if(lex.EQ("=")) return "=";
        if(lex.EQ("<")) return "<";
        if(lex.EQ("<=")) return "<=";
        if(lex.EQ(">")) return ">";
        if(lex.EQ(">=")) return ">=";
        if(lex.EQ("+")) return "+";
        if(lex.EQ("-")) return "-";
        if(lex.EQ("*")) return "*";
        if(lex.EQ("/")) return "/";
        if(lex.EQ("and")) return "and";
        if(lex.EQ("or")) return "or";
        if(lex.EQ("not")) return "not";
        return "";
    }

    private void checkOperation(String operation) {
        String type2 = opStack.pop();
        String type1 = opStack.pop();

        for(TOP topOp : top) {
            if(topOp.getOperation().equals(operation) &&
                    topOp.getType1().equals(type1) &&
                    topOp.getType2().equals(type2)) {
                opStack.push(topOp.getResultType());
                return;
            }
        }
        er("Семантическая ошибка: несовместимые типы для операции '" + operation +
                "': " + type1 + " и " + type2);
    }

    private void checkUnaryOperation() {
        String type = opStack.pop();
        if(!type.equals("bool")) {
            er("Семантическая ошибка: унарная операция 'not' применяется только к логическому типу");
        }
        opStack.push("bool");
    }

    private void checkAssignmentTypes(String leftType, String rightType) {
        if(leftType.equals("int") && rightType.equals("int")) return;
        if(leftType.equals("float") && (rightType.equals("float") || rightType.equals("int"))) return;
        if(leftType.equals("bool") && rightType.equals("bool")) return;

        er("Семантическая ошибка: несовместимые типы в присваивании: " +
                leftType + " и " + rightType);
    }
}