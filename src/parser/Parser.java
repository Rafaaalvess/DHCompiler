package parser;

import error.SyntaxException;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

/**
 * Parser por descida recursiva para a linguagem BRL.
 *
 * Gramática:
 *   programa    → 'inicio' ID ';' declaracoes instrucoes 'fim'
 *   declaracoes → { declaracao }
 *   declaracao  → id_list ':' tipo ';'
 *   id_list     → ID { ',' ID }
 *   tipo        → 'inteiro' | 'real' | 'logico' | 'caractere'
 *   instrucoes  → { instrucao }
 *   instrucao   → atribuicao | se | leia | escreva | enquanto
 *   atribuicao  → ID ':=' exp ';'
 *   se          → 'se' exp 'entao' 'inicio' instrucoes 'fim' [ 'senao' 'inicio' instrucoes 'fim' ]
 *   leia        → 'leia' '(' ID { ',' ID } ')' ';'
 *   escreva     → 'escreva' '(' exp ')' ';'
 *   enquanto    → 'enquanto' exp 'faca' 'inicio' instrucoes 'fim'
 *   exp         → exp_add { RELOP exp_add }
 *   exp_add     → exp_mul { ADDOP exp_mul }
 *   exp_mul     → exp_unario { MULOP exp_unario }
 *   exp_unario  → 'not' exp_unario | '-' exp_unario | primario
 *   primario    → ID | CONST_INT | CONST_REAL | CONST_STRING | 'verdadeiro' | 'falso' | '(' exp ')'
 */
public class Parser {

    private final Lexer lexer;
    private Token current;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.current = lexer.nextToken();
    }

    // -------------------------------------------------------------------------
    // Ponto de entrada
    // -------------------------------------------------------------------------

    public void parseProgram() {
        eat(TokenType.INICIO);
        eat(TokenType.ID);
        eat(TokenType.SEMICOLON);
        parseDeclaracoes();
        parseInstrucoes();
        eat(TokenType.FIM);
        eat(TokenType.EOF);
    }

    // -------------------------------------------------------------------------
    // Declarações
    // -------------------------------------------------------------------------

    private void parseDeclaracoes() {
        while (check(TokenType.ID) &&
               (lexer.peekType() == TokenType.COLON || lexer.peekType() == TokenType.COMMA)) {
            parseDeclaracao();
        }
    }

    private void parseDeclaracao() {
        eat(TokenType.ID);
        while (check(TokenType.COMMA)) {
            eat(TokenType.COMMA);
            eat(TokenType.ID);
        }
        eat(TokenType.COLON);
        parseTipo();
        eat(TokenType.SEMICOLON);
    }

    private void parseTipo() {
        if      (check(TokenType.INTEIRO))   eat(TokenType.INTEIRO);
        else if (check(TokenType.REAL))      eat(TokenType.REAL);
        else if (check(TokenType.LOGICO))    eat(TokenType.LOGICO);
        else if (check(TokenType.CARACTERE)) eat(TokenType.CARACTERE);
        else throw new SyntaxException(current.line,
                "tipo esperado (inteiro, real, logico, caractere)");
    }

    // -------------------------------------------------------------------------
    // Instruções
    // -------------------------------------------------------------------------

    private void parseInstrucoes() {
        while (check(TokenType.ID)      || check(TokenType.SE)   ||
               check(TokenType.LEIA)   || check(TokenType.ESCREVA) ||
               check(TokenType.ENQUANTO)) {
            parseInstrucao();
        }
    }

    private void parseInstrucao() {
        switch (current.type) {
            case ID:       parseAtribuicao(); break;
            case SE:       parseSe();         break;
            case LEIA:     parseLeia();       break;
            case ESCREVA:  parseEscreva();    break;
            case ENQUANTO: parseEnquanto();   break;
            default:
                throw new SyntaxException(current.line, "instrucao invalida");
        }
    }

    private void parseAtribuicao() {
        eat(TokenType.ID);
        eat(TokenType.ASSIGN);
        parseExp();
        eat(TokenType.SEMICOLON);
    }

    private void parseSe() {
        eat(TokenType.SE);
        parseExp();
        eat(TokenType.ENTAO);
        eat(TokenType.INICIO);
        parseInstrucoes();
        eat(TokenType.FIM);
        if (check(TokenType.SENAO)) {
            eat(TokenType.SENAO);
            eat(TokenType.INICIO);
            parseInstrucoes();
            eat(TokenType.FIM);
        }
    }

    private void parseLeia() {
        eat(TokenType.LEIA);
        eat(TokenType.LPAREN);
        eat(TokenType.ID);
        while (check(TokenType.COMMA)) {
            eat(TokenType.COMMA);
            eat(TokenType.ID);
        }
        eat(TokenType.RPAREN);
        eat(TokenType.SEMICOLON);
    }

    private void parseEscreva() {
        eat(TokenType.ESCREVA);
        eat(TokenType.LPAREN);
        parseExp();
        eat(TokenType.RPAREN);
        eat(TokenType.SEMICOLON);
    }

    private void parseEnquanto() {
        eat(TokenType.ENQUANTO);
        parseExp();
        eat(TokenType.FACA);
        eat(TokenType.INICIO);
        parseInstrucoes();
        eat(TokenType.FIM);
    }

    // -------------------------------------------------------------------------
    // Expressões (hierarquia de precedência)
    // -------------------------------------------------------------------------

    private void parseExp() {
        parseExpAdd();
        while (isRelop()) {
            eatRelop();
            parseExpAdd();
        }
    }

    private void parseExpAdd() {
        parseExpMul();
        while (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.OU)) {
            eat(current.type);
            parseExpMul();
        }
    }

    private void parseExpMul() {
        parseExpUnario();
        while (check(TokenType.STAR) || check(TokenType.SLASH) ||
               check(TokenType.DIV)  || check(TokenType.MOD)   ||
               check(TokenType.AND)) {
            eat(current.type);
            parseExpUnario();
        }
    }

    private void parseExpUnario() {
        if (check(TokenType.NOT)) {
            eat(TokenType.NOT);
            parseExpUnario();
        } else if (check(TokenType.MINUS)) {
            eat(TokenType.MINUS);
            parseExpUnario();
        } else {
            parsePrimario();
        }
    }

    private void parsePrimario() {
        if      (check(TokenType.ID))           eat(TokenType.ID);
        else if (check(TokenType.CONST_INT))    eat(TokenType.CONST_INT);
        else if (check(TokenType.CONST_REAL))   eat(TokenType.CONST_REAL);
        else if (check(TokenType.CONST_STRING)) eat(TokenType.CONST_STRING);
        else if (check(TokenType.VERDADEIRO))   eat(TokenType.VERDADEIRO);
        else if (check(TokenType.FALSO))        eat(TokenType.FALSO);
        else if (check(TokenType.LPAREN)) {
            eat(TokenType.LPAREN);
            parseExp();
            eat(TokenType.RPAREN);
        } else {
            throw new SyntaxException(current.line, "expressao invalida");
        }
    }

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    private Token eat(TokenType expected) {
        if (current.type == expected) {
            Token consumed = current;
            current = lexer.nextToken();
            return consumed;
        }
        throw new SyntaxException(current.line, expected, current.type);
    }

    private boolean check(TokenType type) {
        return current.type == type;
    }

    private boolean isRelop() {
        return check(TokenType.EQUAL) || check(TokenType.NEQ) ||
               check(TokenType.LT)    || check(TokenType.GT)  ||
               check(TokenType.LE)    || check(TokenType.GE);
    }

    private void eatRelop() {
        eat(current.type);
    }
}
