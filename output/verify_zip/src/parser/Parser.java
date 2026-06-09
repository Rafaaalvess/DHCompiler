package parser;

import codegen.CodeGenerator;
import error.SemanticException;
import error.SyntaxException;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import semantic.SemanticAnalyzer;
import semantic.Symbol;

import java.util.ArrayList;
import java.util.List;

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
    private final SemanticAnalyzer semantic = new SemanticAnalyzer();
    private CodeGenerator codegen;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.current = lexer.nextToken();
    }

    public SemanticAnalyzer getSemanticAnalyzer() {
        return semantic;
    }

    public void setCodeGenerator(CodeGenerator cg) {
        this.codegen = cg;
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
        List<Token> ids = new ArrayList<>();
        ids.add(eat(TokenType.ID));
        while (check(TokenType.COMMA)) {
            eat(TokenType.COMMA);
            ids.add(eat(TokenType.ID));
        }
        eat(TokenType.COLON);
        String type = parseTipo();
        eat(TokenType.SEMICOLON);

        for (Token id : ids) {
            semantic.declareVariable(id.value, type, id.line);
        }
    }

    private String parseTipo() {
        if      (check(TokenType.INTEIRO))   { eat(TokenType.INTEIRO);   return Symbol.INTEIRO;   }
        else if (check(TokenType.REAL))      { eat(TokenType.REAL);      return Symbol.REAL;      }
        else if (check(TokenType.LOGICO))    { eat(TokenType.LOGICO);    return Symbol.LOGICO;    }
        else if (check(TokenType.CARACTERE)) { eat(TokenType.CARACTERE); return Symbol.CARACTERE; }
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
        Token idToken = eat(TokenType.ID);
        Symbol target = semantic.resolveVariable(idToken.value, idToken.line);
        eat(TokenType.ASSIGN);
        String expType = parseExp();
        eat(TokenType.SEMICOLON);
        semantic.checkAssignment(target.type, expType, idToken.line);

        if (codegen != null) {
            if (target.type.equals(Symbol.CARACTERE)) {
                codegen.genStrCopy(target.name);
            } else {
                codegen.genStore(target.name, target.type);
            }
        }
    }

    private void parseSe() {
        int seLine = current.line;
        eat(TokenType.SE);
        String condType = parseExp();
        semantic.checkLogicCondition(condType, "se", seLine);

        String labelElse = codegen != null ? codegen.newLabel() : null;
        String labelEnd  = codegen != null ? codegen.newLabel() : null;

        if (codegen != null) {
            codegen.emitCmpAX0();
            codegen.emitJE(labelElse);
        }

        eat(TokenType.ENTAO);
        eat(TokenType.INICIO);
        parseInstrucoes();
        eat(TokenType.FIM);

        if (check(TokenType.SENAO)) {
            if (codegen != null) {
                codegen.emitJMP(labelEnd);
                codegen.emitLabel(labelElse);
            }
            eat(TokenType.SENAO);
            eat(TokenType.INICIO);
            parseInstrucoes();
            eat(TokenType.FIM);
            if (codegen != null) codegen.emitLabel(labelEnd);
        } else {
            if (codegen != null) codegen.emitLabel(labelElse);
        }
    }

    private void parseLeia() {
        eat(TokenType.LEIA);
        eat(TokenType.LPAREN);
        Token id = eat(TokenType.ID);
        Symbol s = semantic.resolveVariable(id.value, id.line);
        if (codegen != null) codegen.genLeia(s.name, s.type);
        while (check(TokenType.COMMA)) {
            eat(TokenType.COMMA);
            id = eat(TokenType.ID);
            s = semantic.resolveVariable(id.value, id.line);
            if (codegen != null) codegen.genLeia(s.name, s.type);
        }
        eat(TokenType.RPAREN);
        eat(TokenType.SEMICOLON);
    }

    private void parseEscreva() {
        eat(TokenType.ESCREVA);
        eat(TokenType.LPAREN);
        String type = parseExp();
        if (codegen != null) codegen.genEscreva(type);
        eat(TokenType.RPAREN);
        eat(TokenType.SEMICOLON);
    }

    private void parseEnquanto() {
        int enquantoLine = current.line;
        eat(TokenType.ENQUANTO);

        String labelStart = codegen != null ? codegen.newLabel() : null;
        String labelEnd   = codegen != null ? codegen.newLabel() : null;

        if (codegen != null) codegen.emitLabel(labelStart);

        String condType = parseExp();
        semantic.checkLogicCondition(condType, "enquanto", enquantoLine);

        if (codegen != null) {
            codegen.emitCmpAX0();
            codegen.emitJE(labelEnd);
        }

        eat(TokenType.FACA);
        eat(TokenType.INICIO);
        parseInstrucoes();
        eat(TokenType.FIM);

        if (codegen != null) {
            codegen.emitJMP(labelStart);
            codegen.emitLabel(labelEnd);
        }
    }

    // -------------------------------------------------------------------------
    // Expressões (hierarquia de precedência) — retornam o tipo inferido
    // -------------------------------------------------------------------------

    private String parseExp() {
        String type = parseExpAdd();
        while (isRelop()) {
            if (codegen != null) codegen.emitPushAX();
            String op = current.type.display();
            int opLine = current.line;
            eatRelop();
            String rightType = parseExpAdd();
            if (codegen != null) codegen.emitRelop(op);
            type = semantic.inferBinaryType(type, op, rightType, opLine);
        }
        return type;
    }

    private String parseExpAdd() {
        String type = parseExpMul();
        while (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.OU)) {
            if (codegen != null) codegen.emitPushAX();
            String op = current.type.display();
            int opLine = current.line;
            eat(current.type);
            String rightType = parseExpMul();
            if (codegen != null) codegen.emitBinaryOp(op);
            type = semantic.inferBinaryType(type, op, rightType, opLine);
        }
        return type;
    }

    private String parseExpMul() {
        String type = parseExpUnario();
        while (check(TokenType.STAR) || check(TokenType.SLASH) ||
               check(TokenType.DIV)  || check(TokenType.MOD)   ||
               check(TokenType.AND)) {
            if (codegen != null) codegen.emitPushAX();
            String op = current.type.display();
            int opLine = current.line;
            eat(current.type);
            String rightType = parseExpUnario();
            if (codegen != null) codegen.emitBinaryOp(op);
            type = semantic.inferBinaryType(type, op, rightType, opLine);
        }
        return type;
    }

    private String parseExpUnario() {
        if (check(TokenType.NOT)) {
            int opLine = current.line;
            eat(TokenType.NOT);
            String type = parseExpUnario();
            if (!type.equals(Symbol.LOGICO)) {
                throw new SemanticException(opLine,
                    "operador 'not' requer operando do tipo logico, encontrado '" + type + "'");
            }
            if (codegen != null) codegen.emitUnaryNot();
            return Symbol.LOGICO;
        } else if (check(TokenType.MINUS)) {
            int opLine = current.line;
            eat(TokenType.MINUS);
            String type = parseExpUnario();
            if (!type.equals(Symbol.INTEIRO) && !type.equals(Symbol.REAL)) {
                throw new SemanticException(opLine,
                    "operador '-' requer operando numerico, encontrado '" + type + "'");
            }
            if (codegen != null) codegen.emitUnaryNeg();
            return type;
        } else {
            return parsePrimario();
        }
    }

    private String parsePrimario() {
        if (check(TokenType.ID)) {
            Token idToken = eat(TokenType.ID);
            Symbol s = semantic.resolveVariable(idToken.value, idToken.line);
            if (codegen != null) {
                if (s.type.equals(Symbol.CARACTERE)) {
                    codegen.emitLeaDX(s.name);
                } else {
                    codegen.emitLoadVar(s.name, s.type);
                }
            }
            return s.type;
        } else if (check(TokenType.CONST_INT)) {
            Token t = eat(TokenType.CONST_INT);
            if (codegen != null) codegen.emitLoadInt(t.value);
            return Symbol.INTEIRO;
        } else if (check(TokenType.CONST_REAL)) {
            Token t = eat(TokenType.CONST_REAL);
            if (codegen != null) codegen.emitLoadReal(t.value);
            return Symbol.REAL;
        } else if (check(TokenType.CONST_STRING)) {
            Token t = eat(TokenType.CONST_STRING);
            if (codegen != null) codegen.emitLoadString(t.value);
            return Symbol.CARACTERE;
        } else if (check(TokenType.VERDADEIRO)) {
            eat(TokenType.VERDADEIRO);
            if (codegen != null) codegen.emitLoadTrue();
            return Symbol.LOGICO;
        } else if (check(TokenType.FALSO)) {
            eat(TokenType.FALSO);
            if (codegen != null) codegen.emitLoadFalse();
            return Symbol.LOGICO;
        } else if (check(TokenType.LPAREN)) {
            eat(TokenType.LPAREN);
            String type = parseExp();
            eat(TokenType.RPAREN);
            return type;
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
