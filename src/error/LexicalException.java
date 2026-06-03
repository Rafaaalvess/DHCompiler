package error;

public class LexicalException extends CompilerException {

    public LexicalException(int line, String message) {
        super(line, message);
    }
}
