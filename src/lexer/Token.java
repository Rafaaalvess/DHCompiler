package lexer;

public class Token {

    public final TokenType type;
    public final String value;
    public final int line;

    public Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format("Token(%-14s | %-20s | linha %d)", type, "\"" + value + "\"", line);
    }
}
