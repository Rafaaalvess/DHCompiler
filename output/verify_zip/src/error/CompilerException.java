package error;

public class CompilerException extends RuntimeException {

    private final int line;

    public CompilerException(int line, String message) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
