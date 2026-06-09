package semantic;

public class Symbol {

    public final String name;
    public final String type;
    public final int declarationLine;
    public int address;

    public static final String INTEIRO   = "inteiro";
    public static final String REAL      = "real";
    public static final String LOGICO    = "logico";
    public static final String CARACTERE = "caractere";

    public Symbol(String name, String type, int declarationLine) {
        this.name = name;
        this.type = type;
        this.declarationLine = declarationLine;
        this.address = -1;
    }
}
