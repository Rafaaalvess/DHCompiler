package semantic;

import error.SemanticException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, Symbol> table = new LinkedHashMap<>();

    public void insert(Symbol symbol) {
        if (table.containsKey(symbol.name)) {
            Symbol existing = table.get(symbol.name);
            throw new SemanticException(symbol.declarationLine,
                "variavel '" + symbol.name + "' ja declarada na linha " + existing.declarationLine);
        }
        table.put(symbol.name, symbol);
    }

    public Symbol lookup(String name, int usageLine) {
        Symbol s = table.get(name);
        if (s == null) {
            throw new SemanticException(usageLine,
                "variavel '" + name + "' nao declarada");
        }
        return s;
    }

    public Collection<Symbol> getAll() {
        return table.values();
    }
}
