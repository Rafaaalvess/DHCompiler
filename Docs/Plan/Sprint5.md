# Sprint 5 — Integração, Testes de Sistema e Entrega

**Pré-requisito:** Sprints 1–4 completas — compilador funcional de ponta a ponta  
**Prazo de entrega:** 08/06/2026

---

## Objetivo

Transformar o compilador funcional em uma **entrega final robusta**. Nesta sprint não são implementadas funcionalidades novas. O foco é: corrigir regressões, padronizar mensagens de erro, testar todos os casos do enunciado, documentar o projeto e empacotar corretamente para entrega.

---

## Critério de Aceite

- [ ] O compilador executa via linha de comando com o nome `BRL`
- [ ] Aceita exatamente 2 argumentos: `.LC` e `.ASM`
- [ ] Todos os programas válidos compilam e geram `.ASM` funcional
- [ ] Todos os programas com erro geram a mensagem no formato exato especificado
- [ ] O arquivo `.ASM` gerado é montado pelo MASM sem warnings ou erros
- [ ] O executável gerado pelo MASM roda e produz a saída correta
- [ ] Todos os testes da bateria final passam com status esperado
- [ ] Cabeçalho com nomes dos integrantes está nas primeiras linhas do `Main.java`
- [ ] `README.txt` está completo e funcional
- [ ] ZIP com o nome correto está pronto para upload

---

## Tarefas

### 5.1 — Integração e Revisão do Pipeline

**O que fazer:** Garantir que as 4 fases estão encadeadas corretamente no `Main.java`.

**Fluxo esperado:**
```java
public static void main(String[] args) {
    // 1. Validar argumentos
    validarArgumentos(args);

    // 2. Ler arquivo fonte
    String source = lerArquivo(args[0]);

    // 3. Análise Léxica
    Lexer lexer = new Lexer(source);

    // 4. Análise Sintática + Semântica (integradas)
    SemanticAnalyzer semantic = new SemanticAnalyzer();
    Parser parser = new Parser(lexer, semantic);
    parser.parseProgram();

    // 5. Geração de Código
    CodeGenerator codegen = new CodeGenerator(semantic.getSymbolTable(), args[1]);
    codegen.generate(parser.getProgramNode()); // ou equivalente à estratégia escolhida

    System.out.println("Compilacao concluida: " + args[1]);
}
```

**Tratamento de exceções no Main:**
```java
try {
    // pipeline acima
} catch (LexicalException e) {
    System.err.println("Erro lexico na linha " + e.getLine() + ": " + e.getMessage());
    System.exit(1);
} catch (SyntaxException e) {
    System.err.println("Erro sintatico na linha " + e.getLine() + ": " + e.getMessage());
    System.exit(1);
} catch (SemanticException e) {
    System.err.println("Erro semantico na linha " + e.getLine() + ": " + e.getMessage());
    System.exit(1);
} catch (IOException e) {
    System.err.println("Erro de E/S: " + e.getMessage());
    System.exit(1);
}
```

**Verificar:**
- [ ] Erro léxico: não chega ao parser
- [ ] Erro sintático: não chega ao semântico
- [ ] Erro semântico: não gera arquivo `.ASM`
- [ ] Sucesso: `.ASM` é criado no caminho passado como `args[1]`

---

### 5.2 — Padronização das Mensagens de Erro

**O que fazer:** Revisar todas as mensagens de erro geradas e garantir que seguem o formato exigido.

**Formatos obrigatórios:**
```
Erro lexico na linha X: <descrição>
Erro sintatico na linha X: <descrição>
Erro semantico na linha X: <descrição>
```

**Exemplos concretos:**
```
Erro lexico na linha 3: caractere invalido '@'
Erro lexico na linha 7: string nao fechada
Erro lexico na linha 2: comentario nao fechado
Erro lexico na linha 5: identificador muito longo

Erro sintatico na linha 4: esperado ';', encontrado 'fim'
Erro sintatico na linha 6: esperado 'entao', encontrado 'inicio'
Erro sintatico na linha 9: expressao invalida

Erro semantico na linha 5: variavel 'x' nao declarada
Erro semantico na linha 3: variavel 'total' ja declarada na linha 1
Erro semantico na linha 8: tipos incompativeis na atribuicao
Erro semantico na linha 11: condicao do 'se' deve ser do tipo logico
```

**Checklist de revisão:**
- [ ] Mensagens saem para `System.err` (não `System.out`)
- [ ] Formato "Erro lexico/sintatico/semantico" — sem acento em "léxico/sintático/semântico"
- [ ] Número de linha sempre correto
- [ ] Confirmar formato exato com o professor (correção automatizada)

---

### 5.3 — Bateria Final de Testes

**O que fazer:** Executar todos os casos de teste e registrar o resultado.

**Tabela de testes válidos:**

| Arquivo | Descrição | Esperado | Status |
|---|---|---|---|
| `validos/programa_vazio.LC` | Estrutura mínima | Compila, ASM gerado | |
| `validos/todos_os_tipos.LC` | 4 tipos declarados | Compila | |
| `validos/operadores.LC` | Todos os operadores | Compila | |
| `validos/comparacoes.LC` | Todos os RELOP | Compila | |
| `validos/string.LC` | String literal | Compila | |
| `validos/comentarios.LC` | Comentários ignorados | Compila | |
| `validos/logico_e_not.LC` | Lógicos e not | Compila | |
| `validos/se_sem_senao.LC` | se sem senao | Compila e executa correto | |
| `validos/se_com_senao.LC` | se com senao | Compila e executa correto | |
| `validos/enquanto.LC` | Loop enquanto | Compila e executa correto | |
| `validos/leia_multiplos.LC` | leia(a, b) | Compila | |
| `validos/cod_escreva_string.LC` | escreva string | Executa, imprime correto | |
| `validos/cod_enquanto_contador.LC` | Contador 1 a 5 | Executa, saída correta | |
| `validos/cod_leia_e_escreva.LC` | Lê e imprime | Executa, saída = entrada | |

**Tabela de testes inválidos:**

| Arquivo | Descrição | Erro esperado | Status |
|---|---|---|---|
| `invalidos/caractere_invalido.LC` | `@` no fonte | Erro léxico linha correta | |
| `invalidos/string_sem_fechamento.LC` | String aberta | Erro léxico | |
| `invalidos/comentario_sem_fechamento.LC` | Comentário aberto | Erro léxico | |
| `invalidos/identificador_longo.LC` | ID > 512 chars | Erro léxico | |
| `invalidos/sem_fim.LC` | Falta `fim` | Erro sintático | |
| `invalidos/sem_ponto_virgula.LC` | Falta `;` | Erro sintático | |
| `invalidos/tipo_invalido.LC` | Tipo desconhecido | Erro sintático | |
| `invalidos/se_sem_entao.LC` | Falta `entao` | Erro sintático | |
| `invalidos/sem_variavel_nao_declarada.LC` | Variável não declarada | Erro semântico | |
| `invalidos/sem_variavel_duplicada.LC` | Variável duplicada | Erro semântico | |
| `invalidos/sem_tipo_incompativel_atrib.LC` | Tipos incompatíveis | Erro semântico | |
| `invalidos/sem_condicao_se_invalida.LC` | Condição não lógica | Erro semântico | |
| `invalidos/sem_div_com_real.LC` | `div` com real | Erro semântico | |

---

### 5.4 — Revisão de Conformidade com o Enunciado

**O que fazer:** Percorrer o enunciado item a item e verificar cada requisito.

- [ ] Executável se chama `BRL`
- [ ] Aceita exatamente 2 parâmetros de linha de comando
- [ ] Argumento 1 é o arquivo fonte `.LC`
- [ ] Argumento 2 é o arquivo assembly `.ASM`
- [ ] Erros reportados e compilação encerrada ao encontrar primeiro erro
- [ ] `inteiro` ocupa 4 bytes (`DD`) no segmento de dados
- [ ] `real` ocupa 4 bytes (`DD`) com 7 dígitos de precisão
- [ ] `logico` ocupa 1 byte (`DB`), 0h=falso, FFh=verdadeiro
- [ ] `caractere` ocupa 512 bytes (`DB 512 DUP(?)`), terminado por `$`
- [ ] Identificadores: começa com letra ou `_`, máximo 512 chars, case-sensitive
- [ ] Comentários `/* */` ignorados (uma ou múltiplas linhas)
- [ ] Todos os operadores do enunciado estão implementados
- [ ] Precedência: parênteses > `not` > `*/&&/div/mod` > `+-ou` > relacionais
- [ ] `leia(id [, id])` aceita múltiplos identificadores
- [ ] Confirmar com professor: keyword `leitura` ou `leia`? `escrita` ou `escreva`?

---

### 5.5 — Documentação

**O que fazer:** Escrever a documentação mínima exigida e o cabeçalho com nomes.

**Cabeçalho obrigatório em `Main.java` (primeiras linhas):**
```java
/*
 * Compiladores — Trabalho Prático
 * Faculdade Dom Helder — Ciência da Computação
 * Professor: Prof. Dr. Marcos W. Rodrigues
 *
 * Integrantes:
 *   Nome Completo 1
 *   Nome Completo 2
 *   Nome Completo 3
 *   Nome Completo 4
 */
```

**`README.txt` deve conter:**
```
1. INTEGRANTES
   Nome 1, Nome 2, Nome 3, Nome 4

2. COMO COMPILAR
   javac -d bin src/**/*.java
   (ou comando equivalente ao projeto)

3. COMO EXECUTAR
   java -cp bin main.Main <arquivo_fonte.LC> <saida.ASM>

4. EXEMPLO DE USO
   java -cp bin main.Main programa.LC saida.ASM

5. ESTRUTURA DO PROJETO
   src/main/    - ponto de entrada
   src/lexer/   - análise léxica
   src/parser/  - análise sintática
   src/semantic/- análise semântica
   src/codegen/ - geração de código Assembly
   src/error/   - exceções do compilador
   tests/       - casos de teste

6. FASES DO COMPILADOR
   Léxica:    transforma o fonte em tokens
   Sintática: valida a estrutura gramatical
   Semântica: verifica tipos e declarações
   Codegen:   gera o arquivo .ASM para MASM

7. LIMITAÇÕES CONHECIDAS
   - <listar aqui qualquer limitação identificada>
```

**Qualidade do código — critério de nota:**
- [ ] Indentação consistente em todo o código
- [ ] Nomes de classes, métodos e variáveis descritivos
- [ ] Sem código morto (métodos não usados, imports desnecessários)
- [ ] Métodos recebem contexto por parâmetro, não via variáveis globais desnecessárias

**Apresentação individual — critério de nota:**
- [ ] Cada integrante deve ser capaz de explicar o Lexer
- [ ] Cada integrante deve ser capaz de explicar o Parser e a gramática
- [ ] Cada integrante deve ser capaz de explicar a tabela de símbolos
- [ ] Cada integrante deve ser capaz de explicar a geração de Assembly
- [ ] Cada integrante deve ser capaz de alterar qualquer parte do código a pedido do professor

---

### 5.6 — Empacotamento e Entrega

**O que fazer:** Criar o arquivo compactado no formato exato exigido.

**Nome do arquivo:**
```
nome1_nome2_nome3_nome4.zip
```
Usar nome ou RA dos integrantes separados por `_` (exatamente como o enunciado especifica).

**Conteúdo obrigatório do ZIP:**
```
nome1_nome2.zip
  ├── src/
  │     ├── main/Main.java      ← com cabeçalho de nomes
  │     ├── lexer/
  │     ├── parser/
  │     ├── semantic/
  │     ├── codegen/
  │     └── error/
  ├── tests/
  │     ├── validos/
  │     └── invalidos/
  └── README.txt
```

**Checklist final antes do upload:**
- [ ] Todos os arquivos `.java` estão no ZIP
- [ ] `README.txt` está incluído
- [ ] Arquivos de teste estão incluídos
- [ ] Nome do ZIP segue o padrão
- [ ] Nomes dos integrantes estão em `Main.java`
- [ ] O projeto compila a partir do zero com `javac`
- [ ] O executável final funciona com os exemplos do enunciado
- [ ] Upload realizado no ambiente definido pelo professor até **08/06/2026**
