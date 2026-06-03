# Sprint 3 — Análise Semântica

**Pré-requisito:** Sprint 2 completa — Parser aceita programas sintaticamente corretos  
**Próxima sprint:** Sprint 4 só começa quando todos os critérios de aceite desta estiverem marcados

---

## Objetivo

Implementar a **Análise Semântica**: verificar se o programa faz sentido dentro das regras da linguagem. Nesta fase a pergunta deixa de ser "a estrutura está correta?" e passa a ser "o significado está correto?". Um programa sintaticamente perfeito pode ser semanticamente inválido — por exemplo, usar uma variável sem declará-la ou atribuir uma string a um inteiro.

Ao final desta sprint, programas inválidos semanticamente são rejeitados com mensagem clara, e apenas programas completamente válidos seguem para a geração de código.

---

## Critério de Aceite

- [ ] Toda variável usada em uma instrução deve ter sido previamente declarada — erro se não
- [ ] Nenhuma variável pode ser declarada duas vezes no mesmo programa — erro se sim
- [ ] A tabela de símbolos armazena nome, tipo e linha de declaração de cada variável
- [ ] Atribuição `x := EXP` falha se o tipo de `EXP` é incompatível com o tipo declarado de `x`
- [ ] A condição de `se` e `enquanto` deve ser do tipo `logico`
- [ ] Operadores aritméticos (`+`, `-`, `*`, `/`, `div`, `mod`) só operam com `inteiro` ou `real`
- [ ] Operadores lógicos (`&&`, `ou`, `not`) só operam com `logico`
- [ ] Operador `+` sobre `caractere` é concatenação (válido); sobre outros tipos mistos é erro
- [ ] Comparações (`==`, `<>`, `<`, `>`, `<=`, `>=`) retornam tipo `logico`
- [ ] `leia` só recebe identificadores de variáveis declaradas
- [ ] `escreva` aceita qualquer tipo (inteiro, real, logico, caractere)
- [ ] Programas semanticamente inválidos nunca geram arquivo `.ASM`

---

## Tarefas

### 3.1 — SemanticException.java

**O que fazer:** Criar a exceção específica para erros semânticos.

```java
public class SemanticException extends CompilerException {
    public SemanticException(int line, String message) {
        super(line, message);
    }
}
```

**Formato das mensagens de erro** (seguir rigorosamente):
```
Erro semantico na linha X: variavel 'nome' nao declarada
Erro semantico na linha X: variavel 'nome' ja declarada
Erro semantico na linha X: tipos incompativeis na atribuicao
Erro semantico na linha X: condicao do 'se' deve ser do tipo logico
Erro semantico na linha X: condicao do 'enquanto' deve ser do tipo logico
Erro semantico na linha X: operacao invalida entre tipos 'inteiro' e 'caractere'
```

---

### 3.2 — Symbol.java

**O que fazer:** Criar a classe que representa uma entrada na tabela de símbolos.

**Campos:**
```java
public class Symbol {
    public final String name;           // nome da variável
    public final String type;           // "inteiro" | "real" | "logico" | "caractere"
    public final int declarationLine;   // linha onde foi declarada
    public int address;                 // endereço no segmento de dados (preenchido no Sprint 4)
}
```

**Construtor:**
```java
public Symbol(String name, String type, int declarationLine) {
    this.name = name;
    this.type = type;
    this.declarationLine = declarationLine;
    this.address = -1; // ainda não alocado
}
```

**Constantes úteis para tipo:**
```java
public static final String INTEIRO   = "inteiro";
public static final String REAL      = "real";
public static final String LOGICO    = "logico";
public static final String CARACTERE = "caractere";
```

---

### 3.3 — SymbolTable.java

**O que fazer:** Criar a tabela de símbolos com operações de inserção e busca.

**Estrutura interna:**
```java
private final Map<String, Symbol> table = new LinkedHashMap<>();
// LinkedHashMap preserva a ordem de inserção, útil para geração de código
```

**Método `insert`:**
```java
public void insert(Symbol symbol) throws SemanticException {
    if (table.containsKey(symbol.name)) {
        Symbol existing = table.get(symbol.name);
        throw new SemanticException(symbol.declarationLine,
            "variavel '" + symbol.name + "' ja declarada na linha " + existing.declarationLine);
    }
    table.put(symbol.name, symbol);
}
```

**Método `lookup`:**
```java
public Symbol lookup(String name, int usageLine) throws SemanticException {
    Symbol s = table.get(name);
    if (s == null) {
        throw new SemanticException(usageLine,
            "variavel '" + name + "' nao declarada");
    }
    return s;
}
```

**Método `getAll`:** (usado pelo CodeGenerator no Sprint 4)
```java
public Collection<Symbol> getAll() {
    return table.values();
}
```

---

### 3.4 — SemanticAnalyzer.java — Estrutura Base

**O que fazer:** Criar o analisador semântico que percorre a AST (ou é integrado ao parser como segunda passagem).

**Estratégia de integração:** Duas abordagens possíveis:

**Opção A — Integrado ao Parser (mais simples):**
O próprio Parser popula a tabela de símbolos e faz as verificações de tipo ao parsear. Menos código, mais rápido de implementar.

**Opção B — Passagem separada sobre a AST:**
O Parser gera uma AST, depois o SemanticAnalyzer visita a AST. Mais organizado, mas requer criar classes de nó AST.

> **Recomendação:** Para o escopo deste trabalho, **Opção A** é suficiente e mais fácil de implementar e explicar.

**Com Opção A, o SemanticAnalyzer vira um helper do Parser:**
```java
public class SemanticAnalyzer {
    private final SymbolTable symbolTable = new SymbolTable();

    // Chamado quando o parser processa uma declaração
    public void declareVariable(String name, String type, int line)

    // Chamado quando o parser usa um ID em instrução
    public Symbol resolveVariable(String name, int line)

    // Chamado para verificar compatibilidade de tipos em atribuição
    public void checkAssignment(String targetType, String expType, int line)

    // Chamado para verificar que uma expressão é do tipo logico
    public void checkLogicCondition(String expType, String context, int line)

    // Retorna o tipo resultante de uma operação binária
    public String inferBinaryType(String leftType, String op, String rightType, int line)

    // Retorna a tabela de símbolos completa (para o CodeGen)
    public SymbolTable getSymbolTable()
}
```

---

### 3.5 — Registro de Declarações

**O que fazer:** Ao parsear cada declaração, inserir os identificadores na tabela de símbolos.

**No `parseDeclaracao()` do Parser (modificar para capturar informações):**
```java
void parseDeclaracao() {
    List<Token> ids = new ArrayList<>();
    ids.add(eat(ID));
    while (check(COMMA)) {
        eat(COMMA);
        ids.add(eat(ID));
    }
    eat(COLON);
    String type = parseTipoReturningString(); // retorna "inteiro", "real", etc.
    eat(SEMICOLON);

    // Registrar todos os identificadores com o tipo
    for (Token id : ids) {
        semantic.declareVariable(id.value, type, id.line);
    }
}
```

---

### 3.6 — Validação de Uso de Variáveis

**O que fazer:** Ao encontrar um `ID` em qualquer expressão ou instrução, verificar que foi declarado.

**No `parsePrimario()` (modificar):**
```java
// Quando encontrar ID
Token idToken = eat(ID);
Symbol s = semantic.resolveVariable(idToken.value, idToken.line);
// s.type é o tipo inferido desta subexpressão
```

**No `parseAtribuicao()` (modificar):**
```java
Token idToken = eat(ID);
Symbol target = semantic.resolveVariable(idToken.value, idToken.line);
eat(ASSIGN);
String expType = parseExpReturningType(); // versão que retorna o tipo
eat(SEMICOLON);
semantic.checkAssignment(target.type, expType, idToken.line);
```

---

### 3.7 — Inferência de Tipos em Expressões

**O que fazer:** Cada método `parseExp*()` deve retornar o tipo da expressão avaliada, para que as verificações de compatibilidade possam ocorrer.

**Modificar os métodos parse para retornar `String` (tipo):**
```java
String parseExp()      // retorna tipo da expressão
String parseExpAdd()   // retorna tipo
String parseExpMul()   // retorna tipo
String parseExpUnario() // retorna tipo
String parsePrimario() // retorna tipo
```

**Regras de inferência para `inferBinaryType()`:**

| Operador | Tipos permitidos | Tipo resultante |
|---|---|---|
| `+` `-` `*` `/` | inteiro + inteiro | inteiro |
| `+` `-` `*` `/` | real + real | real |
| `+` `-` `*` `/` | inteiro + real ou real + inteiro | real |
| `div` `mod` | inteiro + inteiro | inteiro |
| `div` `mod` | qualquer outro | **erro semântico** |
| `&&` `ou` | logico + logico | logico |
| `&&` `ou` | qualquer outro | **erro semântico** |
| `+` | caractere + caractere | caractere (concatenação) |
| `==` | inteiro/real/logico/caractere + mesmo tipo | logico |
| `<>` `<` `>` `<=` `>=` | inteiro/real + inteiro/real | logico |
| `<>` `<` `>` `<=` `>=` | tipos incompatíveis | **erro semântico** |

**Regras de inferência para `not`:**
```
not logico → logico
not <outro> → erro semântico
```

**Regras de inferência para unário `-`:**
```
-inteiro → inteiro
-real    → real
-<outro> → erro semântico
```

---

### 3.8 — Validação de Atribuição

**Regras de compatibilidade para atribuição `x := EXP`:**

| Tipo de `x` | Tipo de `EXP` | Válido? |
|---|---|---|
| inteiro | inteiro | Sim |
| real | real | Sim |
| real | inteiro | Sim (promoção implícita) |
| inteiro | real | **Não** |
| logico | logico | Sim |
| caractere | caractere | Sim |
| qualquer | tipo diferente (não listado acima) | **Não** |

---

### 3.9 — Validação de Condições

**No `parseSe()` e `parseEnquanto()`:**
```java
String condType = parseExpReturningType();
semantic.checkLogicCondition(condType, "se", current.line);
```

**Implementação de `checkLogicCondition`:**
```java
public void checkLogicCondition(String type, String context, int line) {
    if (!type.equals(Symbol.LOGICO)) {
        throw new SemanticException(line,
            "condicao do '" + context + "' deve ser do tipo logico, encontrado '" + type + "'");
    }
}
```

---

### 3.10 — Validação de leia e escreva

**`leia`:** cada ID dentro dos parênteses deve ser uma variável declarada.  
Já coberto pelo `resolveVariable()` em `parsePrimario()` — nenhum código extra necessário.

**`escreva`:** aceita qualquer expressão de qualquer tipo válido.  
O tipo retornado por `parseExp()` deve ser um dos 4 tipos da linguagem — se for válido, não lançar erro.

---

## Testes

### Testes semânticos válidos

**`tests/validos/sem_declaracao_e_uso.LC`**
```
inicio ok;
  x : inteiro;
  y : inteiro;
  x := 10;
  y := x + 5;
  escreva(y);
fim
```
Esperado: aceito

---

**`tests/validos/sem_tipos_compativeis.LC`**
```
inicio tipos;
  i : inteiro;
  r : real;
  l : logico;
  c : caractere;
  i := 42;
  r := 3.14;
  r := i;          /* inteiro promovido para real */
  l := verdadeiro;
  l := i > 0;
  c := "Ola";
fim
```
Esperado: aceito

---

**`tests/validos/sem_condicoes.LC`**
```
inicio cond;
  x : inteiro;
  ativo : logico;
  ativo := verdadeiro;
  se ativo entao inicio
    x := 1;
  fim
  enquanto x < 10 faca inicio
    x := x + 1;
  fim
fim
```
Esperado: aceito

---

### Testes de erros semânticos

**`tests/invalidos/sem_variavel_nao_declarada.LC`**
```
inicio teste;
  x := 10;
fim
```
Esperado: `Erro semantico na linha 2: variavel 'x' nao declarada`

---

**`tests/invalidos/sem_variavel_duplicada.LC`**
```
inicio teste;
  x : inteiro;
  x : real;
fim
```
Esperado: `Erro semantico na linha 3: variavel 'x' ja declarada na linha 2`

---

**`tests/invalidos/sem_tipo_incompativel_atrib.LC`**
```
inicio teste;
  x : inteiro;
  x := "texto";
fim
```
Esperado: `Erro semantico na linha 3: tipos incompativeis na atribuicao`

---

**`tests/invalidos/sem_inteiro_para_real_invalido.LC`**
```
inicio teste;
  x : inteiro;
  y : real;
  x := y;
fim
```
Esperado: erro semântico (real não pode ser atribuído a inteiro sem conversão explícita)

---

**`tests/invalidos/sem_condicao_se_invalida.LC`**
```
inicio teste;
  x : inteiro;
  se x entao inicio
    x := 1;
  fim
fim
```
Esperado: `Erro semantico na linha 3: condicao do 'se' deve ser do tipo logico`

---

**`tests/invalidos/sem_condicao_enquanto_invalida.LC`**
```
inicio teste;
  x : inteiro;
  enquanto x faca inicio
    x := x - 1;
  fim
fim
```
Esperado: `Erro semantico na linha 3: condicao do 'enquanto' deve ser do tipo logico`

---

**`tests/invalidos/sem_div_com_real.LC`**
```
inicio teste;
  x : real;
  y : real;
  x := x div y;
fim
```
Esperado: erro semântico (`div` e `mod` só operam com inteiros)

---

**`tests/invalidos/sem_not_em_inteiro.LC`**
```
inicio teste;
  x : inteiro;
  x := not x;
fim
```
Esperado: erro semântico (`not` só opera com logico)
