package error;

import lexer.TokenType;

public class SyntaxException extends CompilerException {

    public SyntaxException(int line, TokenType expected, TokenType found) {
        super(line, String.format(
            "esperado '%s', encontrado '%s'", expected.display(), found.display()
        ));
    }

    public SyntaxException(int line, String message) {
        super(line, message);
    }
}
