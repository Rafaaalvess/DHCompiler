package semantic;

import error.SemanticException;

public class SemanticAnalyzer {

    private final SymbolTable symbolTable = new SymbolTable();

    public void declareVariable(String name, String type, int line) {
        symbolTable.insert(new Symbol(name, type, line));
    }

    public Symbol resolveVariable(String name, int line) {
        return symbolTable.lookup(name, line);
    }

    public void checkAssignment(String targetType, String expType, int line) {
        if (targetType.equals(expType)) return;
        // inteiro promovido para real em atribuicao
        if (targetType.equals(Symbol.REAL) && expType.equals(Symbol.INTEIRO)) return;
        throw new SemanticException(line, "tipos incompativeis na atribuicao");
    }

    public void checkLogicCondition(String type, String context, int line) {
        if (!type.equals(Symbol.LOGICO)) {
            throw new SemanticException(line,
                "condicao do '" + context + "' deve ser do tipo logico, encontrado '" + type + "'");
        }
    }

    public String inferBinaryType(String leftType, String op, String rightType, int line) {
        switch (op) {
            case "+":
                if (leftType.equals(Symbol.CARACTERE) && rightType.equals(Symbol.CARACTERE))
                    return Symbol.CARACTERE;
                if (isNumeric(leftType) && isNumeric(rightType))
                    return (leftType.equals(Symbol.REAL) || rightType.equals(Symbol.REAL))
                        ? Symbol.REAL : Symbol.INTEIRO;
                throw new SemanticException(line,
                    "operacao invalida entre tipos '" + leftType + "' e '" + rightType + "'");

            case "-":
            case "*":
            case "/":
                if (isNumeric(leftType) && isNumeric(rightType))
                    return (leftType.equals(Symbol.REAL) || rightType.equals(Symbol.REAL))
                        ? Symbol.REAL : Symbol.INTEIRO;
                throw new SemanticException(line,
                    "operacao invalida entre tipos '" + leftType + "' e '" + rightType + "'");

            case "div":
            case "mod":
                if (leftType.equals(Symbol.INTEIRO) && rightType.equals(Symbol.INTEIRO))
                    return Symbol.INTEIRO;
                throw new SemanticException(line,
                    "operacao '" + op + "' requer operandos do tipo inteiro");

            case "&&":
            case "ou":
                if (leftType.equals(Symbol.LOGICO) && rightType.equals(Symbol.LOGICO))
                    return Symbol.LOGICO;
                throw new SemanticException(line,
                    "operacao '" + op + "' requer operandos do tipo logico");

            case "==":
                if (leftType.equals(rightType)) return Symbol.LOGICO;
                if (isNumeric(leftType) && isNumeric(rightType)) return Symbol.LOGICO;
                throw new SemanticException(line,
                    "operacao invalida entre tipos '" + leftType + "' e '" + rightType + "'");

            case "<>":
            case "<":
            case ">":
            case "<=":
            case ">=":
                if (isNumeric(leftType) && isNumeric(rightType)) return Symbol.LOGICO;
                throw new SemanticException(line,
                    "operacao invalida entre tipos '" + leftType + "' e '" + rightType + "'");

            default:
                throw new SemanticException(line, "operador desconhecido: " + op);
        }
    }

    private boolean isNumeric(String type) {
        return type.equals(Symbol.INTEIRO) || type.equals(Symbol.REAL);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
