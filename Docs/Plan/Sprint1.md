# Sprint 1 — Infraestrutura do Projeto e Análise Léxica

**Pré-requisito:** Nenhum  
**Próxima sprint:** Sprint 2 só começa quando todos os critérios de aceite desta estiverem marcados

---

## Objetivo

Montar a estrutura do projeto Java e implementar o **Lexer** (scanner) completo: o componente que lê o arquivo `.LC` caractere a caractere e produz uma sequência de tokens. Ao final desta sprint, dado qualquer programa BRL válido, o compilador deve conseguir tokenizá-lo sem erros. Dado um programa com erro léxico, deve parar e exibir a mensagem no formato correto.

---

## Critério de Aceite

- [ ] O projeto compila sem erros com `javac`
- [ ] `Main.java` aceita exatamente 2 argumentos: `arquivo.LC` e `arquivo.ASM`
- [ ] `Main.java` valida extensão `.LC`, existência do arquivo e permissão de leitura
- [ ] O Lexer percorre o arquivo inteiro sem travar
- [ ] Todos os tipos de token são reconhecidos (palavras reservadas, IDs, constantes, operadores, delimitadores)
- [ ] Identificadores com até 512 caracteres são aceitos; acima disso gera erro léxico
- [ ] A linguagem é case-sensitive: `inicio` ≠ `Inicio` ≠ `INICIO`
- [ ] Comentários `/* ... */` de uma ou múltiplas linhas são completamente ignorados
- [ ] Espaços, tabs e quebras de linha são descartados (apenas delimitam lexemas)
- [ ] Qualquer caractere fora do conjunto permitido gera erro léxico com número de linha
- [ ] String sem fechamento de aspas gera erro léxico
- [ ] Comentário sem fechamento `*/` gera erro léxico
- [ ] Ao encontrar qualquer erro léxico, o compilador exibe a mensagem e encerra imediatamente

---

## Tarefas

### 1.1 — Setup do Projeto Java

**O que fazer:** Criar a estrutura de diretórios e o esqueleto do projeto.

**Estrutura de pastas:**
```
DHCompiler/
  src/
    main/
      Main.java
    lexer/
      Lexer.java
      Token.java
      TokenType.java
    parser/          ← vazio por enquanto
    semantic/        ← vazio por enquanto
    codegen/         ← vazio por enquanto
    error/
      CompilerException.java
  tests/
    validos/
    invalidos/
```

**Detalhamento:**
- Definir o pacote raiz (ex: `brl`)
- O projeto deve compilar com um único `javac -cp src src/main/Main.java` ou equivalente
- Nenhuma dependência externa (sem Maven, sem bibliotecas de terceiros)

**Resultado esperado:** `javac` sem erros, mesmo que o compilador ainda não faça nada útil.

---

### 1.2 — Main.java e Tratamento de Argumentos

**O que fazer:** Criar o ponto de entrada do compilador com validação completa dos argumentos.

**Lógica do Main:**
```
1. Verificar se args.length == 2
   → se não: exibir uso e encerrar com código de erro

2. Verificar se args[0] termina com ".LC" (case-sensitive)
   → se não: erro "arquivo fonte deve ter extensão .LC"

3. Verificar se o arquivo args[0] existe no sistema de arquivos
   → se não: erro "arquivo fonte não encontrado: <caminho>"

4. Verificar se o arquivo args[0] pode ser lido
   → se não: erro "sem permissão de leitura: <caminho>"

5. Verificar se args[1] termina com ".ASM"
   → se não: erro "arquivo de saída deve ter extensão .ASM"

6. Iniciar o pipeline: Lexer → (futuro) Parser → Semantic → CodeGen
```

**Observação crítica do enunciado:** O executável deve se chamar `BRL`. A invocação esperada é:
```
BRL arquivo_fonte.LC saida.ASM
```
Ao empacotar como JAR no Sprint 5, garantir que o manifest aponta para `Main` como classe principal.

**Resultado esperado:** Ao rodar com argumentos inválidos, exibe mensagem de erro descritiva e encerra. Com argumentos válidos, segue para o lexer.

---

### 1.3 — CompilerException.java

**O que fazer:** Criar a classe base de exceção do compilador, usada por todas as fases.

**Campos obrigatórios:**
```java
private final int line;       // linha onde ocorreu o erro
private final String message; // descrição do erro
```

**Subclasses a criar agora:**
- `LexicalException extends CompilerException` — erros do Lexer

**Subclasses a criar nas sprints seguintes:**
- `SyntaxException` — Sprint 2
- `SemanticException` — Sprint 3

**Formato da mensagem de erro** (seguir rigorosamente — a correção é automatizada):
```
Erro lexico na linha X: <descrição>
```
Exemplo:
```
Erro lexico na linha 7: caractere invalido '@'
Erro lexico na linha 12: string nao fechada
Erro lexico na linha 3: comentario nao fechado
Erro lexico na linha 5: identificador muito longo
```

> **Atenção:** Confirmar o formato exato com o professor antes da entrega. A avaliação é automatizada e qualquer diferença de formatação pode zerar a nota.

---

### 1.4 — TokenType.java

**O que fazer:** Criar o enum com todos os tipos de token da linguagem BRL.

**Lista completa obrigatória:**

```java
public enum TokenType {
    // Palavras reservadas
    INTEIRO, CARACTERE, LOGICO, REAL,
    SE, ENTAO, SENAO,
    ENQUANTO, FACA,
    INICIO, FIM,
    LEIA, ESCREVA,
    OU, DIV, MOD,
    VERDADEIRO, FALSO,
    NOT,

    // Operadores relacionais
    EQUAL,   // ==
    NEQ,     // <>
    LT,      // <
    GT,      // >
    LE,      // <=
    GE,      // >=

    // Operadores de atribuição e lógicos
    ASSIGN,  // :=
    AND,     // &&

    // Operadores aritméticos
    PLUS,    // +
    MINUS,   // -
    STAR,    // *
    SLASH,   // /

    // Delimitadores
    LPAREN,    // (
    RPAREN,    // )
    SEMICOLON, // ;
    COLON,     // :
    COMMA,     // ,

    // Literais
    ID,           // identificador
    CONST_INT,    // constante inteira
    CONST_REAL,   // constante real
    CONST_STRING, // constante string/caractere

    // Controle
    EOF
}
```

> **Nota sobre `leitura`/`escrita`:** O enunciado lista `leitura` e `escrita` na tabela de palavras reservadas, mas define as instruções como `leia(...)` e `escreva(...)`. Implementar com `LEIA`/`ESCREVA` conforme a definição formal e confirmar com o professor.

---

### 1.5 — Token.java

**O que fazer:** Criar a classe que representa um token individual.

**Campos:**
```java
public class Token {
    public final TokenType type;
    public final String value;  // lexema original do fonte
    public final int line;      // linha no arquivo fonte
}
```

**Construtor:**
```java
public Token(TokenType type, String value, int line)
```

**Método útil:**
```java
@Override
public String toString() {
    return String.format("Token(%s, \"%s\", linha=%d)", type, value, line);
}
```

---

### 1.6 — Lexer.java — Estrutura Base

**O que fazer:** Criar a classe Lexer com a lógica de leitura caractere a caractere.

**Estado interno necessário:**
```java
private final String source;  // conteúdo completo do arquivo
private int pos;              // posição atual no source
private int line;             // linha atual (começa em 1)
private final Map<String, TokenType> reservedWords;
```

**Métodos principais:**
```java
// Retorna o próximo token e avança o cursor
public Token nextToken()

// Olha o próximo token sem consumir (lookahead)
public Token peek()

// Retorna true se chegou ao fim do arquivo
public boolean isEOF()
```

**Método auxiliar de leitura:**
```java
// Retorna char atual e avança pos
private char advance()

// Retorna char atual sem avançar
private char current()

// Retorna char seguinte sem avançar (lookahead de 1)
private char lookAhead()
```

**Carregamento do arquivo:** Ler o arquivo inteiro para uma `String source` no construtor. Simples e suficiente para os tamanhos de arquivo esperados.

---

### 1.7 — Lexer.java — Reconhecimento de Tokens

**O que fazer:** Implementar o método `nextToken()` com todos os casos.

**Fluxo geral do nextToken():**
```
1. Pular whitespace e quebras de linha
2. Se chegou ao EOF → retornar Token(EOF, "", line)
3. Olhar o caractere atual e decidir o tipo:
   - letra ou '_' → identificador ou palavra reservada
   - dígito ou ('+'/'-' seguido de dígito) → constante numérica
   - '"' → constante string
   - '/' seguido de '*' → comentário
   - operador ou delimitador → token específico
   - qualquer outro → lançar LexicalException
```

**Reconhecimento de identificadores e palavras reservadas:**
```
1. Ler sequência de letras, dígitos e '_'
2. Se tamanho > 512 → LexicalException "identificador muito longo"
3. Consultar reservedWords.get(lexema)
4. Se encontrado → retornar Token(tipoReservado, lexema, line)
5. Se não → retornar Token(ID, lexema, line)
```

**Reconhecimento de inteiros e reais:**
```
1. Consumir dígitos
2. Se próximo char é '.' → é real: consumir '.' e mais dígitos
3. Retornar CONST_INT ou CONST_REAL
```
> O sinal `+`/`-` antes de um número é tratado pelo **parser** como operador unário, não pelo lexer. O lexer só produz `PLUS`/`MINUS` seguido de `CONST_INT`/`CONST_REAL`.

**Reconhecimento de strings:**
```
1. Consumir '"' inicial
2. Acumular chars até encontrar '"' de fechamento
3. Se encontrar quebra de linha antes do '"' → LexicalException "string nao fechada"
4. Se chegar no EOF antes do '"' → LexicalException "string nao fechada"
5. Verificar tamanho do conteúdo ≤ 255 chars úteis
6. Consumir '"' final
7. Retornar Token(CONST_STRING, conteúdo_sem_aspas, line)
```

**Reconhecimento de comentários:**
```
1. Consumir '/' e '*'
2. Ler chars até encontrar '*' seguido de '/'
3. Contabilizar quebras de linha encontradas dentro do comentário
4. Se chegar no EOF sem fechar → LexicalException "comentario nao fechado"
5. Comentário é descartado — nextToken() continua para o próximo lexema
```

**Reconhecimento de operadores (tratar os compostos primeiro):**
```
':' seguido de '=' → ASSIGN (:=)
'<' seguido de '>' → NEQ (<>)
'<' seguido de '=' → LE (<=)
'>' seguido de '=' → GE (>=)
'&' seguido de '&' → AND (&&)
'=' seguido de '=' → EQUAL (==)
'<' sozinho       → LT
'>' sozinho       → GT
'+' → PLUS
'-' → MINUS
'*' → STAR
'/' → SLASH
';' → SEMICOLON
':' → COLON
',' → COMMA
'(' → LPAREN
')' → RPAREN
```

**Conjunto de caracteres válidos no fonte:**
Letra, dígito, espaço, `_`, `.`, `,`, `;`, `:`, `(`, `)`, `[`, `]`, `{`, `}`, `+`, `-`, `"`, `'`, `/`, `\`, `|`, `!`, `?`, `>`, `<`, `=`, `*`, `&`, e quebra de linha (`0Dh`/`0Ah`). Qualquer outro caractere → `LexicalException`.

---

### 1.8 — Tabela de Palavras Reservadas

**O que fazer:** Popular o `HashMap<String, TokenType>` no construtor do Lexer.

**Mapeamento completo:**
```
"inteiro"    → INTEIRO
"caractere"  → CARACTERE
"logico"     → LOGICO
"real"       → REAL
"se"         → SE
"entao"      → ENTAO
"senao"      → SENAO
"enquanto"   → ENQUANTO
"faca"       → FACA
"inicio"     → INICIO
"fim"        → FIM
"leia"       → LEIA       ← confirmar com professor (enunciado tem ambiguidade)
"escreva"    → ESCREVA    ← confirmar com professor (enunciado tem ambiguidade)
"ou"         → OU
"div"        → DIV
"mod"        → MOD
"verdadeiro" → VERDADEIRO
"falso"      → FALSO
"not"        → NOT
```

**Atenção:** A linguagem é case-sensitive. `"inicio"` é palavra reservada; `"Inicio"` e `"INICIO"` são identificadores válidos.

---

## Testes

### Testes de programas válidos

**`tests/validos/programa_vazio.LC`**
```
inicio vazio;
fim
```
Esperado: tokens `INICIO`, `ID("vazio")`, `SEMICOLON`, `FIM`, `EOF`

---

**`tests/validos/todos_os_tipos.LC`**
```
inicio tipos;
  a : inteiro;
  b : real;
  c : logico;
  d : caractere;
fim
```
Esperado: todos os tokens reconhecidos, incluindo os 4 tipos

---

**`tests/validos/operadores.LC`**
```
inicio ops;
  x : inteiro;
  x := 1 + 2 - 3 * 4 / 5;
  x := 10 div 3;
  x := 10 mod 3;
fim
```
Esperado: todos os operadores tokenizados corretamente

---

**`tests/validos/comparacoes.LC`**
```
inicio cmp;
  a : inteiro;
  b : logico;
  b := a == 10;
  b := a <> 5;
  b := a < 3;
  b := a > 3;
  b := a <= 3;
  b := a >= 3;
fim
```

---

**`tests/validos/string.LC`**
```
inicio str;
  nome : caractere;
  nome := "Rafael";
fim
```

---

**`tests/validos/comentarios.LC`**
```
/* Este e um comentario de uma linha */
inicio comentarios;
  /* comentario
     de multiplas
     linhas */
  x : inteiro;
fim
```
Esperado: comentários completamente ignorados

---

**`tests/validos/logico_e_not.LC`**
```
inicio logicos;
  a : logico;
  b : logico;
  a := verdadeiro;
  b := falso;
  a := not b;
  a := a && b;
  a := a ou b;
fim
```

---

**`tests/validos/identificador_sublinhado.LC`**
```
inicio ids;
  _var : inteiro;
  nome_completo : caractere;
  __x : logico;
fim
```

---

### Testes de erros léxicos

**`tests/invalidos/caractere_invalido.LC`**
```
inicio teste;
  x := @10;
fim
```
Esperado: `Erro lexico na linha 2: caractere invalido '@'`

---

**`tests/invalidos/string_sem_fechamento.LC`**
```
inicio teste;
  x := "Rafael;
fim
```
Esperado: `Erro lexico na linha 2: string nao fechada`

---

**`tests/invalidos/comentario_sem_fechamento.LC`**
```
inicio teste;
/* comentario sem fechar
  x : inteiro;
fim
```
Esperado: `Erro lexico na linha 2: comentario nao fechado`

---

**`tests/invalidos/identificador_longo.LC`**  
Criar um arquivo com um identificador de 513 caracteres.  
Esperado: `Erro lexico na linha 1: identificador muito longo`

---

**`tests/invalidos/string_com_quebra.LC`**
```
inicio teste;
  x := "texto
com quebra";
fim
```
Esperado: `Erro lexico na linha 2: string nao fechada`
