# Sprint 2 — Análise Sintática (Parser)

**Pré-requisito:** Sprint 1 completa — Lexer funcional, todos os tokens reconhecidos  
**Próxima sprint:** Sprint 3 só começa quando todos os critérios de aceite desta estiverem marcados

---

## Objetivo

Implementar o **Parser** por descida recursiva que recebe a sequência de tokens produzida pelo Lexer e verifica se o programa segue a gramática da linguagem BRL. Ao final desta sprint, qualquer programa sintaticamente correto deve ser aceito sem erros. Qualquer programa com erro de estrutura deve ser rejeitado com mensagem indicando linha e o que era esperado.

O parser desta sprint **não precisa** gerar código nem validar tipos — isso é responsabilidade das sprints seguintes.

---

## Critério de Aceite

- [ ] O parser aceita todos os programas válidos da Sprint 1 sem erro
- [ ] O parser reconhece a estrutura `inicio ID ; declaracoes instrucoes fim`
- [ ] Declarações no formato `id [, id] : tipo ;` são reconhecidas
- [ ] Os 5 tipos de instrução são reconhecidos: atribuição, `se`, `leia`, `escreva`, `enquanto`
- [ ] `se` com e sem `senao` é aceito
- [ ] Expressões respeitam a hierarquia de precedência definida no enunciado
- [ ] `not` como operador unário é reconhecido
- [ ] Erros sintáticos param o compilador e exibem mensagem com linha e token inesperado
- [ ] O parser **não** tenta recuperar de erros — para no primeiro erro encontrado
- [ ] Programas sem nenhuma declaração e sem nenhuma instrução são aceitos (`inicio id; fim`)

---

## Tarefas

### 2.1 — Gramática Formal da Linguagem BRL

**O que fazer:** Escrever a gramática antes de codificar. Serve como guia direto para os métodos do parser.

**Gramática em EBNF:**

```
programa        → 'inicio' ID ';' declaracoes instrucoes 'fim'

declaracoes     → { declaracao }
declaracao      → id_list ':' tipo ';'
id_list         → ID { ',' ID }
tipo            → 'inteiro' | 'real' | 'logico' | 'caractere'

instrucoes      → instrucao { ';' instrucao }
instrucao       → atribuicao
                | se
                | leia
                | escreva
                | enquanto

atribuicao      → ID ':=' exp ';'
se              → 'se' exp 'entao' 'inicio' instrucoes 'fim'
                  [ 'senao' 'inicio' instrucoes 'fim' ]
leia            → 'leia' '(' ID { ',' ID } ')'
escreva         → 'escreva' '(' exp ')'
enquanto        → 'enquanto' exp 'faca' 'inicio' instrucoes 'fim'

exp             → exp_add { RELOP exp_add }
RELOP           → '==' | '<>' | '<' | '>' | '<=' | '>='

exp_add         → exp_mul { ADDOP exp_mul }
ADDOP           → '+' | '-' | 'ou'

exp_mul         → exp_unario { MULOP exp_unario }
MULOP           → '*' | '/' | 'div' | 'mod' | '&&'

exp_unario      → 'not' exp_unario
                | '-' exp_unario
                | primario

primario        → ID
                | CONST_INT
                | CONST_REAL
                | CONST_STRING
                | 'verdadeiro'
                | 'falso'
                | '(' exp ')'
```

**Importante:** Cada regra da gramática vira exatamente um método `parse<Regra>()` no parser. Manter essa correspondência 1-para-1 facilita debugar e explicar o código.

---

### 2.2 — SyntaxException.java

**O que fazer:** Criar a exceção de erro sintático.

```java
public class SyntaxException extends CompilerException {
    public SyntaxException(int line, TokenType expected, TokenType found) {
        super(line, String.format(
            "esperado '%s', encontrado '%s'", expected, found
        ));
    }

    public SyntaxException(int line, String message) {
        super(line, message);
    }
}
```

**Formato da mensagem de erro:**
```
Erro sintatico na linha X: esperado ';', encontrado 'fim'
Erro sintatico na linha X: esperado identificador, encontrado '+'
Erro sintatico na linha X: instrucao invalida
```

---

### 2.3 — Parser.java — Estrutura Base

**O que fazer:** Criar a classe Parser com os métodos auxiliares de navegação nos tokens.

**Estado interno:**
```java
private final Lexer lexer;
private Token current; // token atual
```

**Construtor:**
```java
public Parser(Lexer lexer) {
    this.lexer = lexer;
    this.current = lexer.nextToken(); // carrega o primeiro token
}
```

**Método `eat(TokenType expected)`:**
```java
// Verifica se o token atual é do tipo esperado.
// Se sim: avança para o próximo token e retorna o token consumido.
// Se não: lança SyntaxException.
private Token eat(TokenType expected) {
    if (current.type == expected) {
        Token consumed = current;
        current = lexer.nextToken();
        return consumed;
    }
    throw new SyntaxException(current.line, expected, current.type);
}
```

**Método `check(TokenType type)`:**
```java
// Verifica sem consumir se o token atual é do tipo informado.
private boolean check(TokenType type) {
    return current.type == type;
}
```

**Método `match(TokenType type)`:**
```java
// Consome e retorna true se o token atual for do tipo informado.
// Não avança se não for.
private boolean match(TokenType type) {
    if (check(type)) { eat(type); return true; }
    return false;
}
```

---

### 2.4 — parseProgram() — Estrutura Principal

**O que fazer:** Implementar a regra de produção do programa completo.

```
programa → 'inicio' ID ';' declaracoes instrucoes 'fim'
```

**Implementação:**
```java
public void parseProgram() {
    eat(INICIO);
    eat(ID);          // nome do programa
    eat(SEMICOLON);
    parseDeclaracoes();
    parseInstrucoes();
    eat(FIM);
    eat(EOF);         // garantir fim de arquivo
}
```

**Ponto de atenção:** O bloco `inicio...fim` pode conter zero declarações e zero instruções. `parseDeclaracoes()` e `parseInstrucoes()` devem aceitar sequências vazias.

---

### 2.5 — parseDeclaracoes() e parseDeclaracao()

**O que fazer:** Reconhecer zero ou mais declarações de variáveis.

```
declaracoes → { declaracao }
declaracao  → id_list ':' tipo ';'
id_list     → ID { ',' ID }
tipo        → 'inteiro' | 'real' | 'logico' | 'caractere'
```

**Lógica de `parseDeclaracoes()`:**
```
Enquanto o token atual for ID e o token seguinte for ':' ou ',':
    → chamar parseDeclaracao()
```

> **Dificuldade:** Como saber se o ID atual é início de declaração ou início de instrução (atribuição)? A diferença está no token seguinte: declaração tem `:` após o ID (ou `,` para lista), instrução tem `:=`. Usar `peek()` do Lexer para olhar sem consumir.

**Lógica de `parseDeclaracao()`:**
```java
void parseDeclaracao() {
    eat(ID);                    // primeiro identificador
    while (check(COMMA)) {
        eat(COMMA);
        eat(ID);                // identificadores adicionais
    }
    eat(COLON);
    parseTipo();
    eat(SEMICOLON);
}
```

**Lógica de `parseTipo()`:**
```java
void parseTipo() {
    if (check(INTEIRO)) eat(INTEIRO);
    else if (check(REAL)) eat(REAL);
    else if (check(LOGICO)) eat(LOGICO);
    else if (check(CARACTERE)) eat(CARACTERE);
    else throw new SyntaxException(current.line, "tipo esperado (inteiro, real, logico, caractere)");
}
```

---

### 2.6 — parseInstrucoes() e parseInstrucao()

**O que fazer:** Reconhecer zero ou mais instruções separadas por `;`.

```
instrucoes → instrucao { ';' instrucao }
```

**Lógica de `parseInstrucoes()`:**
```
Enquanto o token atual puder iniciar uma instrução (ID, SE, LEIA, ESCREVA, ENQUANTO):
    → parseInstrucao()
    → se o próximo token for ';': consumir e continuar
    → se não for ';' e não for FIM/SENAO: lançar erro
```

**Lógica de `parseInstrucao()`** — dispatcher:
```java
void parseInstrucao() {
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
```

---

### 2.7 — parseAtribuicao()

```
atribuicao → ID ':=' exp ';'
```

```java
void parseAtribuicao() {
    eat(ID);
    eat(ASSIGN);
    parseExp();
    eat(SEMICOLON);
}
```

---

### 2.8 — parseSe()

```
se → 'se' exp 'entao' 'inicio' instrucoes 'fim'
     [ 'senao' 'inicio' instrucoes 'fim' ]
```

```java
void parseSe() {
    eat(SE);
    parseExp();
    eat(ENTAO);
    eat(INICIO);
    parseInstrucoes();
    eat(FIM);
    if (check(SENAO)) {
        eat(SENAO);
        eat(INICIO);
        parseInstrucoes();
        eat(FIM);
    }
}
```

---

### 2.9 — parseLeia()

```
leia → 'leia' '(' ID { ',' ID } ')'
```

```java
void parseLeia() {
    eat(LEIA);
    eat(LPAREN);
    eat(ID);
    while (check(COMMA)) {
        eat(COMMA);
        eat(ID);
    }
    eat(RPAREN);
}
```

---

### 2.10 — parseEscreva()

```
escreva → 'escreva' '(' exp ')'
```

```java
void parseEscreva() {
    eat(ESCREVA);
    eat(LPAREN);
    parseExp();
    eat(RPAREN);
}
```

---

### 2.11 — parseEnquanto()

```
enquanto → 'enquanto' exp 'faca' 'inicio' instrucoes 'fim'
```

```java
void parseEnquanto() {
    eat(ENQUANTO);
    parseExp();
    eat(FACA);
    eat(INICIO);
    parseInstrucoes();
    eat(FIM);
}
```

---

### 2.12 — Hierarquia de Expressões (precedência)

**O que fazer:** Implementar a hierarquia completa de expressões respeitando a precedência definida no enunciado.

**Precedência (do menor para o maior):**
```
5. Comparação:      == <> < > <= >=
4. Adição:          + - ou
3. Multiplicação:   * / div mod &&
2. Unário:          not  -
1. Primário:        ID  CONST  ( exp )
```

**parseExp() — nível comparação:**
```java
void parseExp() {
    parseExpAdd();
    while (isRelop()) {
        eatRelop();
        parseExpAdd();
    }
}

private boolean isRelop() {
    return check(EQUAL) || check(NEQ) || check(LT) ||
           check(GT) || check(LE) || check(GE);
}
```

**parseExpAdd() — nível adição:**
```java
void parseExpAdd() {
    parseExpMul();
    while (check(PLUS) || check(MINUS) || check(OU)) {
        eat(current.type);
        parseExpMul();
    }
}
```

**parseExpMul() — nível multiplicação:**
```java
void parseExpMul() {
    parseExpUnario();
    while (check(STAR) || check(SLASH) || check(DIV) ||
           check(MOD) || check(AND)) {
        eat(current.type);
        parseExpUnario();
    }
}
```

**parseExpUnario() — negação:**
```java
void parseExpUnario() {
    if (check(NOT)) {
        eat(NOT);
        parseExpUnario(); // recursivo: not not x é válido
    } else if (check(MINUS)) {
        eat(MINUS);
        parseExpUnario();
    } else {
        parsePrimario();
    }
}
```

**parsePrimario() — folha da expressão:**
```java
void parsePrimario() {
    if (check(ID))           eat(ID);
    else if (check(CONST_INT))    eat(CONST_INT);
    else if (check(CONST_REAL))   eat(CONST_REAL);
    else if (check(CONST_STRING)) eat(CONST_STRING);
    else if (check(VERDADEIRO))   eat(VERDADEIRO);
    else if (check(FALSO))        eat(FALSO);
    else if (check(LPAREN)) {
        eat(LPAREN);
        parseExp();
        eat(RPAREN);
    } else {
        throw new SyntaxException(current.line, "expressao invalida");
    }
}
```

---

## Testes

### Testes de programas válidos

**`tests/validos/estrutura_minima.LC`**
```
inicio minimo;
fim
```
Esperado: aceito sem erros

---

**`tests/validos/declaracoes_multiplas.LC`**
```
inicio decl;
  a, b, c : inteiro;
  x, y : real;
  nome : caractere;
  ativo : logico;
fim
```
Esperado: aceito

---

**`tests/validos/atribuicao_simples.LC`**
```
inicio atr;
  x : inteiro;
  x := 10;
fim
```

---

**`tests/validos/expressao_complexa.LC`**
```
inicio expr;
  a : inteiro;
  b : inteiro;
  c : inteiro;
  c := (a + b) * 2 - 1;
  c := a div b mod 3;
fim
```

---

**`tests/validos/se_sem_senao.LC`**
```
inicio condicional;
  x : inteiro;
  x := 5;
  se x == 5 entao inicio
    x := 10;
  fim
fim
```

---

**`tests/validos/se_com_senao.LC`**
```
inicio condicional;
  x : inteiro;
  x := 5;
  se x > 3 entao inicio
    x := 1;
  fim
  senao inicio
    x := 0;
  fim
fim
```

---

**`tests/validos/enquanto.LC`**
```
inicio loop;
  i : inteiro;
  i := 0;
  enquanto i < 10 faca inicio
    i := i + 1;
  fim
fim
```

---

**`tests/validos/leia_multiplos.LC`**
```
inicio entrada;
  a : inteiro;
  b : inteiro;
  leia(a, b);
fim
```

---

**`tests/validos/not_e_logico.LC`**
```
inicio logico;
  a : logico;
  b : logico;
  a := not b;
  a := not not b;
  a := a && b;
  a := a ou b;
fim
```

---

### Testes de erros sintáticos

**`tests/invalidos/sem_fim.LC`**
```
inicio teste;
  x : inteiro;
```
Esperado: `Erro sintatico na linha 3: esperado 'fim', encontrado 'EOF'`

---

**`tests/invalidos/sem_ponto_virgula.LC`**
```
inicio teste;
  x : inteiro
  x := 10;
fim
```
Esperado: `Erro sintatico na linha 3: esperado ';', encontrado 'ID'`

---

**`tests/invalidos/tipo_invalido.LC`**
```
inicio teste;
  x : numero;
fim
```
Esperado: `Erro sintatico na linha 2: tipo esperado (inteiro, real, logico, caractere)`

---

**`tests/invalidos/atribuicao_sem_assign.LC`**
```
inicio teste;
  x : inteiro;
  x = 10;
fim
```
Esperado: `Erro sintatico na linha 3: esperado ':=', encontrado 'EQUAL'`

---

**`tests/invalidos/se_sem_entao.LC`**
```
inicio teste;
  x : inteiro;
  se x > 0 inicio
    x := 1;
  fim
fim
```
Esperado: `Erro sintatico na linha 3: esperado 'entao', encontrado 'inicio'`

---

**`tests/invalidos/parentese_sem_fechar.LC`**
```
inicio teste;
  x : inteiro;
  x := (10 + 5;
fim
```
Esperado: `Erro sintatico na linha 3: esperado ')', encontrado ';'`
