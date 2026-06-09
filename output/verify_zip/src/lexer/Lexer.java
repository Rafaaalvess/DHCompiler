package lexer;

import error.LexicalException;

import java.util.HashMap;
import java.util.Map;

public class Lexer {

    private final String source;
    private int pos;
    private int line;
    private Token peeked;
    private final Map<String, TokenType> reservedWords;

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.peeked = null;
        this.reservedWords = buildReservedWords();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Retorna o próximo token sem consumi-lo. Chamadas repetidas retornam o mesmo token. */
    public Token peek() {
        if (peeked == null) {
            peeked = readNextToken();
        }
        return peeked;
    }

    /** Retorna o tipo do próximo token sem consumi-lo (atalho para peek().type). */
    public TokenType peekType() {
        return peek().type;
    }

    /** Consome e retorna o próximo token. */
    public Token nextToken() {
        if (peeked != null) {
            Token t = peeked;
            peeked = null;
            return t;
        }
        return readNextToken();
    }

    // -------------------------------------------------------------------------
    // Leitura interna
    // -------------------------------------------------------------------------

    private Token readNextToken() {
        skipWhitespaceAndNewlines();

        if (isAtEnd()) {
            return new Token(TokenType.EOF, "", line);
        }

        char c = current();

        // Identificador ou palavra reservada
        if (Character.isLetter(c) || c == '_') {
            return readIdentifier();
        }

        // Constante numérica
        if (Character.isDigit(c)) {
            return readNumber();
        }

        // String literal
        if (c == '"') {
            return readString();
        }

        // Comentário /* ... */
        if (c == '/' && lookAhead() == '*') {
            skipComment();
            return readNextToken();
        }

        // Operadores e delimitadores
        return readSymbol();
    }

    // -------------------------------------------------------------------------
    // Reconhecimento de lexemas
    // -------------------------------------------------------------------------

    private Token readIdentifier() {
        int startLine = line;
        int start = pos;

        while (!isAtEnd() && (Character.isLetterOrDigit(current()) || current() == '_')) {
            advance();
        }

        String lexeme = source.substring(start, pos);

        if (lexeme.length() > 512) {
            throw new LexicalException(startLine, "identificador muito longo");
        }

        TokenType type = reservedWords.getOrDefault(lexeme, TokenType.ID);
        return new Token(type, lexeme, startLine);
    }

    private Token readNumber() {
        int startLine = line;
        int start = pos;

        while (!isAtEnd() && Character.isDigit(current())) {
            advance();
        }

        // Verifica parte decimal: dígitos '.' dígitos
        if (!isAtEnd() && current() == '.' && Character.isDigit(lookAhead())) {
            advance(); // consome '.'
            while (!isAtEnd() && Character.isDigit(current())) {
                advance();
            }
            return new Token(TokenType.CONST_REAL, source.substring(start, pos), startLine);
        }

        return new Token(TokenType.CONST_INT, source.substring(start, pos), startLine);
    }

    private Token readString() {
        int startLine = line;
        advance(); // consome '"' de abertura
        int start = pos;

        while (!isAtEnd() && current() != '"') {
            if (current() == '\n' || current() == '\r') {
                throw new LexicalException(startLine, "string nao fechada");
            }
            advance();
        }

        if (isAtEnd()) {
            throw new LexicalException(startLine, "string nao fechada");
        }

        String content = source.substring(start, pos);

        if (content.length() > 255) {
            throw new LexicalException(startLine, "string muito longa (maximo 255 caracteres)");
        }

        advance(); // consome '"' de fechamento
        return new Token(TokenType.CONST_STRING, content, startLine);
    }

    private void skipComment() {
        int startLine = line;
        advance(); // consome '/'
        advance(); // consome '*'

        while (!isAtEnd()) {
            if (current() == '*' && lookAhead() == '/') {
                advance(); // consome '*'
                advance(); // consome '/'
                return;
            }
            advance();
        }

        throw new LexicalException(startLine, "comentario nao fechado");
    }

    private Token readSymbol() {
        int startLine = line;
        char c = current();

        switch (c) {
            case '+': advance(); return new Token(TokenType.PLUS,      "+",  startLine);
            case '-': advance(); return new Token(TokenType.MINUS,     "-",  startLine);
            case '*': advance(); return new Token(TokenType.STAR,      "*",  startLine);
            case '/': advance(); return new Token(TokenType.SLASH,     "/",  startLine);
            case ';': advance(); return new Token(TokenType.SEMICOLON, ";",  startLine);
            case ',': advance(); return new Token(TokenType.COMMA,     ",",  startLine);
            case '(': advance(); return new Token(TokenType.LPAREN,    "(",  startLine);
            case ')': advance(); return new Token(TokenType.RPAREN,    ")",  startLine);

            case ':':
                advance();
                if (!isAtEnd() && current() == '=') {
                    advance();
                    return new Token(TokenType.ASSIGN, ":=", startLine);
                }
                return new Token(TokenType.COLON, ":", startLine);

            case '<':
                advance();
                if (!isAtEnd() && current() == '>') {
                    advance();
                    return new Token(TokenType.NEQ, "<>", startLine);
                }
                if (!isAtEnd() && current() == '=') {
                    advance();
                    return new Token(TokenType.LE, "<=", startLine);
                }
                return new Token(TokenType.LT, "<", startLine);

            case '>':
                advance();
                if (!isAtEnd() && current() == '=') {
                    advance();
                    return new Token(TokenType.GE, ">=", startLine);
                }
                return new Token(TokenType.GT, ">", startLine);

            case '=':
                advance();
                if (!isAtEnd() && current() == '=') {
                    advance();
                    return new Token(TokenType.EQUAL, "==", startLine);
                }
                throw new LexicalException(startLine, "caractere invalido '='");

            case '&':
                advance();
                if (!isAtEnd() && current() == '&') {
                    advance();
                    return new Token(TokenType.AND, "&&", startLine);
                }
                throw new LexicalException(startLine, "caractere invalido '&'");

            default:
                advance();
                throw new LexicalException(startLine, "caractere invalido '" + c + "'");
        }
    }

    // -------------------------------------------------------------------------
    // Auxiliares de navegação
    // -------------------------------------------------------------------------

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private char current() {
        if (isAtEnd()) return '\0';
        return source.charAt(pos);
    }

    private char lookAhead() {
        if (pos + 1 >= source.length()) return '\0';
        return source.charAt(pos + 1);
    }

    /** Avança uma posição. Incrementa o contador de linha ao encontrar '\n'. */
    private char advance() {
        char c = source.charAt(pos++);
        if (c == '\n') line++;
        return c;
    }

    private void skipWhitespaceAndNewlines() {
        while (!isAtEnd()) {
            char c = current();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else {
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tabela de palavras reservadas
    // -------------------------------------------------------------------------

    private Map<String, TokenType> buildReservedWords() {
        Map<String, TokenType> map = new HashMap<>();
        map.put("inteiro",    TokenType.INTEIRO);
        map.put("caractere",  TokenType.CARACTERE);
        map.put("logico",     TokenType.LOGICO);
        map.put("real",       TokenType.REAL);
        map.put("se",         TokenType.SE);
        map.put("entao",      TokenType.ENTAO);
        map.put("senao",      TokenType.SENAO);
        map.put("enquanto",   TokenType.ENQUANTO);
        map.put("faca",       TokenType.FACA);
        map.put("inicio",     TokenType.INICIO);
        map.put("fim",        TokenType.FIM);
        map.put("leia",       TokenType.LEIA);
        map.put("escreva",    TokenType.ESCREVA);
        map.put("ou",         TokenType.OU);
        map.put("div",        TokenType.DIV);
        map.put("mod",        TokenType.MOD);
        map.put("verdadeiro", TokenType.VERDADEIRO);
        map.put("falso",      TokenType.FALSO);
        map.put("not",        TokenType.NOT);
        return map;
    }
}
