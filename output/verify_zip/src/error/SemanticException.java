package error;

public class SemanticException extends CompilerException {
    public SemanticException(int line, String message) {
        super(line, message);
    }
}
