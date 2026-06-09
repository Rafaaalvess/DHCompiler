package lexer;

public enum TokenType {

    // --- Palavras reservadas ---
    INTEIRO, CARACTERE, LOGICO, REAL,
    SE, ENTAO, SENAO,
    ENQUANTO, FACA,
    INICIO, FIM,
    LEIA, ESCREVA,
    OU, DIV, MOD,
    VERDADEIRO, FALSO,
    NOT,

    // --- Operadores relacionais ---
    EQUAL,      // ==
    NEQ,        // <>
    LT,         // <
    GT,         // >
    LE,         // <=
    GE,         // >=

    // --- Operadores de atribuição e lógico composto ---
    ASSIGN,     // :=
    AND,        // &&

    // --- Operadores aritméticos ---
    PLUS,       // +
    MINUS,      // -
    STAR,       // *
    SLASH,      // /

    // --- Delimitadores ---
    LPAREN,     // (
    RPAREN,     // )
    SEMICOLON,  // ;
    COLON,      // :
    COMMA,      // ,

    // --- Literais ---
    ID,
    CONST_INT,
    CONST_REAL,
    CONST_STRING,

    // --- Controle ---
    EOF;

    /** Retorna a representação legível do token para mensagens de erro. */
    public String display() {
        switch (this) {
            case INTEIRO:      return "inteiro";
            case CARACTERE:    return "caractere";
            case LOGICO:       return "logico";
            case REAL:         return "real";
            case SE:           return "se";
            case ENTAO:        return "entao";
            case SENAO:        return "senao";
            case ENQUANTO:     return "enquanto";
            case FACA:         return "faca";
            case INICIO:       return "inicio";
            case FIM:          return "fim";
            case LEIA:         return "leia";
            case ESCREVA:      return "escreva";
            case OU:           return "ou";
            case DIV:          return "div";
            case MOD:          return "mod";
            case VERDADEIRO:   return "verdadeiro";
            case FALSO:        return "falso";
            case NOT:          return "not";
            case EQUAL:        return "==";
            case NEQ:          return "<>";
            case LT:           return "<";
            case GT:           return ">";
            case LE:           return "<=";
            case GE:           return ">=";
            case ASSIGN:       return ":=";
            case AND:          return "&&";
            case PLUS:         return "+";
            case MINUS:        return "-";
            case STAR:         return "*";
            case SLASH:        return "/";
            case LPAREN:       return "(";
            case RPAREN:       return ")";
            case SEMICOLON:    return ";";
            case COLON:        return ":";
            case COMMA:        return ",";
            case ID:           return "identificador";
            case CONST_INT:    return "literal inteiro";
            case CONST_REAL:   return "literal real";
            case CONST_STRING: return "literal string";
            case EOF:          return "fim de arquivo";
            default:           return name();
        }
    }
}
