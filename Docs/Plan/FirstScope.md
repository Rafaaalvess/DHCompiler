 
## Objetivo do trabalho

  

Desenvolver um compilador em **Java** capaz de ler um programa fonte da linguagem **BRL/LC** e gerar um arquivo **Assembly `.ASM`** para a arquitetura **80x86/MASM**.

  

O compilador deve ser executado por linha de comando com dois argumentos:

  

```bash

java -jar compilador.jar entrada.LC saida.ASM

```

  

Onde:

  

- `entrada.LC` é o arquivo fonte na linguagem BRL/LC.

- `saida.ASM` é o arquivo Assembly gerado pelo compilador.

  

---

  

# Estratégia geral

  

O trabalho será dividido em poucas entregas principais, mas cada entrega será quebrada em tarefas pequenas, com:

  

- Objetivo claro.

- Resultado esperado.

- Subtarefas.

- Critério de pronto.

- Testes obrigatórios.

- Perguntas de entendimento.

  

A ideia é não pensar apenas em “fazer um compilador”, mas sim em uma sequência de transformações:

  

```text

Texto do arquivo .LC

        ↓

Tokens

        ↓

AST / estrutura sintática

        ↓

Programa semanticamente validado

        ↓

Código Assembly .ASM

        ↓

Teste no MASM

```

  

---

  

# Visão geral das entregas

  

| Entrega | Nome | Resultado concreto |

|---|---|---|

| **0** | Contrato do compilador | Projeto Java, CLI, estrutura, documentos de tokens, gramática e testes |

| **1** | Analisador léxico | `.LC` vira lista de tokens ou erro léxico |

| **2** | Analisador sintático + AST | Tokens viram árvore/estrutura do programa ou erro sintático |

| **3** | Analisador semântico | AST é validada por tipos, declarações e tabela de símbolos |

| **4** | Gerador de Assembly | Programa válido gera `.ASM` funcional |

| **5** | Integração, testes e documentação | ZIP final, testes válidos/inválidos, README e apresentação |

  

---

  

# Entrega 0 — Contrato do compilador

  

## Objetivo

  

Montar a base do projeto e transformar o enunciado em uma especificação de implementação.

  

Nesta entrega ainda não será implementado o compilador real. O foco é preparar a estrutura e deixar claro o que será implementado.

  

## Resultado esperado

  

Ao final dessa entrega, deve ser possível rodar:

  

```bash

java -jar compilador.jar entrada.LC saida.ASM

```

  

Mesmo que o compilador ainda não gere Assembly real.

  

---

  

## Subentregas

  

### 0.1 — Criar projeto Java

  

Estrutura sugerida:

  

```text

compilador-brl/

  src/

    main/

      java/

        brl/

          Main.java

          lexer/

          parser/

          ast/

          semantic/

          codegen/

          error/

  tests/

    validos/

    invalidos/

  docs/

```

  

---

  

### 0.2 — Criar entrada por linha de comando

  

O `Main` precisa validar:

  

```text

args.length == 2

args[0] termina com .LC

args[1] termina com .ASM

arquivo .LC existe

arquivo .LC pode ser lido

arquivo .ASM pode ser criado

```

  

---

  

### 0.3 — Criar documento de tokens

  

Arquivo:

  

```text

docs/tokens.md

```

  

Deve listar:

  

```text

Palavras reservadas

Operadores

Delimitadores

Identificadores

Constantes

Comentários

Caracteres inválidos

```

  

---

  

### 0.4 — Criar documento de gramática inicial

  

Arquivo:

  

```text

docs/gramatica.md

```

  

Deve conter:

  

```text

programa

declarações

tipos

instruções

atribuição

leia

escreva

se/senao

enquanto

expressões

```

  

---

  

### 0.5 — Criar primeiros arquivos de teste

  

Criar pelo menos:

  

```text

tests/validos/programa_vazio.LC

tests/validos/declaracoes.LC

tests/validos/atribuicoes.LC

tests/validos/se_senao.LC

tests/validos/enquanto.LC

  

tests/invalidos/caractere_invalido.LC

tests/invalidos/string_sem_fechamento.LC

tests/invalidos/fim_faltando.LC

tests/invalidos/variavel_nao_declarada.LC

tests/invalidos/tipo_incompativel.LC

```

  

---

  

## Critério de pronto

  

A Entrega 0 só está pronta quando:

  

```text

[ ] Projeto Java criado

[ ] Main recebe dois argumentos

[ ] Main valida extensão .LC e .ASM

[ ] Main lê arquivo de entrada

[ ] Main cria arquivo de saída

[ ] docs/tokens.md existe

[ ] docs/gramatica.md existe

[ ] Pasta tests/validos existe

[ ] Pasta tests/invalidos existe

[ ] Pelo menos 10 arquivos de teste foram criados

```

  

---

  

## Perguntas de entendimento

  

Antes de passar para a Entrega 1, responder:

  

```text

O que entra no compilador?

O que sai do compilador?

Qual é o papel do arquivo .LC?

Qual é o papel do arquivo .ASM?

Quais são as fases do compilador?

O que é token?

O que é erro léxico?

O que é erro sintático?

O que é erro semântico?

```

  

---

  

# Entrega 1 — Analisador léxico

  

## Objetivo

  

Transformar o texto do arquivo `.LC` em uma sequência de tokens.

  

A análise léxica é a primeira fase do front-end. Ela reconhece lexemas, tokens, palavras reservadas, identificadores, constantes e símbolos.

  

## Resultado esperado

  

Ao rodar:

  

```bash

java -jar compilador.jar programa.LC saida.ASM

```

  

O compilador deve conseguir produzir internamente uma lista de tokens.

  

Neste momento ainda não é necessário gerar Assembly real.

  

---

  

## Subentregas

  

### 1.1 — Criar classes base do lexer

  

Criar:

  

```text

Token.java

TokenType.java

Lexer.java

LexicalException.java

```

  

`Token` deve guardar:

  

```text

tipo

lexema

linha

coluna

```

  

---

  

### 1.2 — Reconhecer palavras reservadas

  

Palavras iniciais:

  

```text

inicio

fim

inteiro

caractere

logico

real

se

entao

senao

enquanto

faca

leia

escreva

verdadeiro

falso

div

mod

ou

```

  

---

  

### 1.3 — Reconhecer identificadores

  

Regras:

  

```text

começa com letra ou sublinhado

pode conter letra, dígito ou sublinhado

linguagem é sensitive-case

```

  

Exemplo:

  

```text

valor

Valor

VALOR

```

  

Podem ser identificadores diferentes.

  

---

  

### 1.4 — Reconhecer números

  

Separar:

  

```text

inteiro: 10

real: 10.5

real negativo: -10.5

inteiro positivo: +10

```

  

Observação: inicialmente é aceitável tokenizar `+` e `-` como operadores separados e deixar o parser tratar expressões negativas.

  

---

  

### 1.5 — Reconhecer textos/caracteres entre aspas

  

Exemplos:

  

```text

"Rafael"

"teste"

"Compiladores"

```

  

Detectar erros:

  

```text

string sem fechamento

quebra de linha dentro da string

```

  

---

  

### 1.6 — Reconhecer operadores

  

Operadores principais:

  

```text

+

-

*

/

div

mod

&&

ou

=

<>

<

>

<=

>=

:=

```

  

---

  

### 1.7 — Reconhecer delimitadores

  

Delimitadores:

  

```text

;

,

:

(

)

[

]

{

}

```

  

---

  

### 1.8 — Ignorar espaços, tabs e quebras de linha

  

Eles delimitam lexemas, mas não viram tokens.

  

---

  

### 1.9 — Ignorar comentários

  

Comentários:

  

```text

/* comentário */

```

  

Validar também erro de comentário sem fechamento.

  

---

  

### 1.10 — Erros léxicos

  

Detectar:

  

```text

caractere inválido

string sem fechamento

comentário sem fechamento

identificador acima do tamanho permitido

constante inválida

```

  

---

  

## Testes obrigatórios da entrega

  

```text

[ ] programa vazio válido

[ ] declaração de inteiro

[ ] declaração de real

[ ] declaração de logico

[ ] declaração de caractere

[ ] identificador com sublinhado

[ ] palavra reservada

[ ] número inteiro

[ ] número real

[ ] string válida

[ ] comentário válido

[ ] caractere inválido

[ ] string sem fechamento

[ ] comentário sem fechamento

```

  

---

  

## Critério de pronto

  

```text

[ ] Lexer percorre o arquivo inteiro

[ ] Lexer gera tokens com linha e coluna

[ ] Palavras reservadas são reconhecidas

[ ] Identificadores são reconhecidos

[ ] Constantes são reconhecidas

[ ] Operadores são reconhecidos

[ ] Delimitadores são reconhecidos

[ ] Comentários são ignorados

[ ] Espaços e quebras de linha são ignorados

[ ] Erros léxicos param o compilador

[ ] Há testes válidos e inválidos para o lexer

```

  

---

  

## Perguntas de entendimento

  

```text

Qual a diferença entre lexema e token?

Por que "inicio" não deve ser classificado como ID?

Como diferenciar ID de palavra reservada?

Como detectar string sem fechamento?

Por que linha e coluna são importantes?

O que o lexer não deve tentar resolver?

```

  

---

  

# Entrega 2 — Analisador sintático + AST

  

## Objetivo

  

Transformar a lista de tokens em uma estrutura sintática do programa.

  

Aqui é verificado se os tokens estão na ordem correta. A análise sintática agrupa os tokens conforme a gramática e pode produzir uma árvore sintática abstrata, a AST.

  

## Resultado esperado

  

Arquivos `.LC` sintaticamente corretos devem ser aceitos.

  

Arquivos com erro de estrutura devem ser rejeitados.

  

---

  

## Subentregas

  

### 2.1 — Criar parser base

  

Criar:

  

```text

Parser.java

SyntaxException.java

```

  

O parser recebe:

  

```text

List<Token>

```

  

E retorna:

  

```text

ProgramNode

```

  

ou lança erro sintático.

  

---

  

### 2.2 — Reconhecer estrutura principal

  

Formato geral:

  

```text

inicio identificador;

  declarações

  instruções

fim

```

  

Tasks:

  

```text

[ ] Consumir token inicio

[ ] Consumir identificador do programa

[ ] Consumir ponto-e-vírgula

[ ] Ler declarações

[ ] Ler instruções

[ ] Consumir fim

[ ] Garantir fim de arquivo

```

  

---

  

### 2.3 — Reconhecer declarações

  

Formato:

  

```text

id [, id] : tipo ;

```

  

Exemplos:

  

```text

x : inteiro;

a, b, c : real;

nome : caractere;

acesso : logico;

```

  

Tasks:

  

```text

[ ] Ler lista de identificadores

[ ] Exigir dois-pontos

[ ] Ler tipo

[ ] Exigir ponto-e-vírgula

[ ] Criar nó de declaração

```

  

---

  

### 2.4 — Reconhecer comandos simples

  

Comandos:

  

```text

id := EXP;

leia(id);

escreva(EXP);

```

  

Tasks:

  

```text

[ ] Parser de atribuição

[ ] Parser de leia

[ ] Parser de escreva

[ ] Exigir parênteses onde precisa

[ ] Exigir ponto-e-vírgula onde precisa

```

  

---

  

### 2.5 — Reconhecer comandos compostos

  

Comandos:

  

```text

se EXP entao inicio instrucoes fim

  

se EXP entao inicio instrucoes fim

senao inicio instrucao fim

  

enquanto EXP faca inicio instrucoes fim

```

  

Tasks:

  

```text

[ ] Parser de se

[ ] Parser de senao opcional

[ ] Parser de enquanto

[ ] Parser de bloco inicio/fim

[ ] Permitir lista de instruções dentro do bloco

```

  

---

  

### 2.6 — Reconhecer expressões

  

Separar por precedência:

  

```text

comparação

soma/subtração/ou/concatenação

multiplicação/divisão/div/mod/&&

negação

primário

```

  

Primários:

  

```text

identificador

constante

verdadeiro

falso

expressão entre parênteses

```

  

---

  

### 2.7 — Criar AST

  

Criar nós como:

  

```text

ProgramNode

DeclarationNode

AssignmentNode

ReadNode

WriteNode

IfNode

WhileNode

BinaryExpressionNode

UnaryExpressionNode

LiteralNode

IdentifierNode

```

  

---

  

## Testes obrigatórios da entrega

  

```text

[ ] programa sem declaração

[ ] programa com uma declaração

[ ] programa com várias declarações

[ ] atribuição simples

[ ] expressão com soma

[ ] expressão com multiplicação

[ ] expressão com parênteses

[ ] leia

[ ] escreva

[ ] se sem senao

[ ] se com senao

[ ] enquanto

[ ] erro: falta ;

[ ] erro: falta fim

[ ] erro: falta )

[ ] erro: comando inválido

```

  

---

  

## Critério de pronto

  

```text

[ ] Parser aceita programas válidos

[ ] Parser rejeita programas sintaticamente inválidos

[ ] Parser gera AST

[ ] Expressões respeitam precedência

[ ] Blocos inicio/fim são reconhecidos

[ ] Mensagens de erro indicam linha e coluna

[ ] Testes sintáticos foram criados

```

  

---

  

## Perguntas de entendimento

  

```text

Qual a diferença entre erro léxico e erro sintático?

Por que o parser depende do lexer?

O que é AST?

Por que AST não precisa guardar todos os símbolos, como ; e ,?

Como a precedência dos operadores afeta a árvore?

Por que expressão é a parte mais delicada do parser?

```

  

---

  

# Entrega 3 — Analisador semântico

  

## Objetivo

  

Verificar se o programa faz sentido.

  

Aqui a pergunta deixa de ser “a frase está bem escrita?” e passa a ser “essa frase faz sentido dentro da linguagem?”.

  

A análise semântica verifica contexto, declarações, escopo, tabela de símbolos e compatibilidade de tipos.

  

## Resultado esperado

  

Um programa sintaticamente correto, mas semanticamente errado, deve ser rejeitado.

  

Exemplo:

  

```text

inicio teste;

  x := 10;

fim

```

  

Deve gerar erro porque `x` não foi declarado.

  

---

  

## Subentregas

  

### 3.1 — Criar tabela de símbolos

  

Criar:

  

```text

Symbol.java

SymbolTable.java

SemanticAnalyzer.java

SemanticException.java

```

  

Cada símbolo deve ter:

  

```text

nome

tipo

classe

endereço futuro

linha da declaração

```

  

Inicialmente, endereço pode ficar vazio.

  

---

  

### 3.2 — Registrar declarações

  

Ao visitar declarações:

  

```text

x : inteiro;

nome : caractere;

```

  

Inserir na tabela:

  

```text

x -> inteiro

nome -> caractere

```

  

Detectar duplicidade:

  

```text

x : inteiro;

x : real;

```

  

---

  

### 3.3 — Validar uso de variáveis

  

Detectar:

  

```text

variável usada sem declaração

variável usada antes de existir

```

  

---

  

### 3.4 — Validar atribuições

  

Exemplos válidos:

  

```text

x : inteiro;

x := 10;

  

r : real;

r := 10.5;

  

ok : logico;

ok := verdadeiro;

```

  

Exemplos inválidos:

  

```text

x : inteiro;

x := "Rafael";

  

ok : logico;

ok := 10;

```

  

---

  

### 3.5 — Validar expressões aritméticas

  

Operadores:

  

```text

+

-

*

/

div

mod

```

  

Validar:

  

```text

inteiro com inteiro

real com real

inteiro com real

```

  

Regra sugerida:

  

```text

inteiro + real gera real

real + inteiro gera real

inteiro div inteiro gera inteiro

inteiro mod inteiro gera inteiro

```

  

---

  

### 3.6 — Validar expressões lógicas

  

Operadores:

  

```text

&&

ou

not

```

  

Devem operar com tipo lógico.

  

---

  

### 3.7 — Validar comparações

  

Operadores:

  

```text

=

<>

<

>

<=

>=

```

  

Resultado deve ser lógico.

  

Exemplos:

  

```text

x > 10        -> logico

nome = "A"    -> logico

```

  

---

  

### 3.8 — Validar condições

  

As condições de `se` e `enquanto` precisam resultar em `logico`.

  

Exemplo inválido:

  

```text

se 10 entao inicio

fim

```

  

---

  

### 3.9 — Validar leia/escreva

  

`leia` deve receber identificadores declarados.

  

`escreva` deve receber expressão válida.

  

---

  

## Testes obrigatórios da entrega

  

```text

[ ] variável declarada e usada

[ ] variável duplicada

[ ] variável não declarada

[ ] atribuição inteiro válida

[ ] atribuição real válida

[ ] atribuição lógica válida

[ ] atribuição incompatível

[ ] expressão aritmética válida

[ ] expressão lógica válida

[ ] comparação válida

[ ] condição de se válida

[ ] condição de se inválida

[ ] condição de enquanto válida

[ ] leia com variável declarada

[ ] leia com variável não declarada

```

  

---

  

## Critério de pronto

  

```text

[ ] Tabela de símbolos funciona

[ ] Declarações são registradas

[ ] Duplicidades são detectadas

[ ] Variáveis não declaradas são detectadas

[ ] Tipos de expressões são inferidos

[ ] Atribuições são verificadas

[ ] Condições são verificadas

[ ] leia/escreva são verificados

[ ] Programas semanticamente inválidos não geram ASM

```

  

---

  

## Perguntas de entendimento

  

```text

Qual a diferença entre erro sintático e erro semântico?

Por que x := "texto" pode ser sintaticamente correto e semanticamente errado?

O que é tabela de símbolos?

O que precisa ser guardado na tabela de símbolos?

Como descobrir o tipo de uma expressão?

Por que a geração de código depende da análise semântica?

```

  

---

  

# Entrega 4 — Geração de Assembly

  

## Objetivo

  

Gerar o arquivo `.ASM` a partir da AST validada.

  

A geração de código é a fase que transforma o programa fonte em comandos da linguagem alvo, neste caso Assembly 80x86/MASM.

  

## Resultado esperado

  

Um programa `.LC` válido deve gerar um `.ASM` que possa ser montado pelo MASM.

  

---

  

## Subentregas

  

### 4.1 — Gerar estrutura base do ASM

  

Criar:

  

```text

AsmGenerator.java

```

  

Gerar:

  

```text

segmento de pilha

segmento de dados

segmento de código

início do programa

fim do programa

```

  

---

  

### 4.2 — Gerar declarações

  

Mapear tipos:

  

```text

inteiro   -> espaço para número inteiro

real      -> representação definida pelo projeto

logico    -> 0 ou FFh

caractere -> área de string/caractere

```

  

---

  

### 4.3 — Gerar atribuições simples

  

Exemplo:

  

```text

x := 10;

```

  

Deve virar instruções ASM que armazenam `10` no endereço de `x`.

  

---

  

### 4.4 — Gerar expressões aritméticas

  

Suportar:

  

```text

x := a + b;

x := a - b;

x := a * b;

x := a / b;

```

  

---

  

### 4.5 — Gerar escrita

  

Suportar:

  

```text

escreva(x);

escreva("teste");

```

  

---

  

### 4.6 — Gerar leitura

  

Suportar:

  

```text

leia(x);

```

  

---

  

### 4.7 — Gerar condições

  

Suportar:

  

```text

se x > 10 entao inicio

  escreva(x);

fim

```

  

Criar rótulos:

  

```text

R1

R2

R3

```

  

---

  

### 4.8 — Gerar se/senao

  

Suportar:

  

```text

se x > 10 entao inicio

  escreva(x);

fim

senao inicio

  escreva(0);

fim

```

  

---

  

### 4.9 — Gerar enquanto

  

Suportar:

  

```text

enquanto x < 10 faca inicio

  x := x + 1;

fim

```

  

---

  

### 4.10 — Testar no MASM

  

Checklist:

  

```text

[ ] ASM foi gerado

[ ] MASM montou

[ ] Linkou

[ ] Executou

[ ] Saída foi correta

```

  

---

  

## Testes obrigatórios da entrega

  

```text

[ ] programa só com declaração

[ ] atribuição de inteiro

[ ] atribuição com soma

[ ] atribuição com multiplicação

[ ] escreva string

[ ] escreva inteiro

[ ] leia inteiro

[ ] se verdadeiro

[ ] se falso

[ ] se/senao

[ ] enquanto com contador

```

  

---

  

## Critério de pronto

  

```text

[ ] ASM é gerado em arquivo .ASM

[ ] Declarações aparecem no segmento de dados

[ ] Atribuições funcionam

[ ] Expressões funcionam

[ ] Entrada funciona

[ ] Saída funciona

[ ] Se/senao funciona

[ ] Enquanto funciona

[ ] Arquivo monta no MASM

[ ] Executável roda

```

  

---

  

## Perguntas de entendimento

  

```text

O que é código alvo?

Por que o gerador precisa da tabela de símbolos?

O que é segmento de dados?

O que é segmento de código?

O que é rótulo?

Como um if vira salto condicional?

Como um while vira rótulo + salto?

Por que expressões precisam de temporários?

```

  

---

  

# Entrega 5 — Integração, testes e documentação

  

## Objetivo

  

Transformar o projeto em uma entrega final robusta.

  

Nesta etapa não devem ser implementadas funcionalidades novas. O foco é corrigir, documentar, testar e empacotar.

  

---

  

## Subentregas

  

### 5.1 — Bateria final de testes

  

Criar uma tabela:

  

| Teste | Esperado | Status |

|---|---|---|

| `programa_vazio.LC` | compila | OK |

| `declaracoes.LC` | compila | OK |

| `variavel_nao_declarada.LC` | erro semântico | OK |

| `fim_faltando.LC` | erro sintático | OK |

| `caractere_invalido.LC` | erro léxico | OK |

  

---

  

### 5.2 — Padronizar mensagens de erro

  

Sugestão:

  

```text

Erro léxico na linha X, coluna Y: caractere inválido '@'

Erro sintático na linha X, coluna Y: esperado ';', encontrado 'fim'

Erro semântico na linha X, coluna Y: variável 'x' não declarada

```

  

---

  

### 5.3 — Criar README

  

O README deve conter:

  

```text

Nome dos integrantes

Como compilar o projeto

Como executar

Exemplos de entrada

Exemplos de saída

Limitações conhecidas

Estrutura do projeto

Descrição das fases do compilador

```

  

---

  

### 5.4 — Revisar organização

  

Checklist:

  

```text

[ ] Código fonte dentro do ZIP/RAR

[ ] README incluído

[ ] Arquivos de teste incluídos

[ ] Nenhum arquivo desnecessário

[ ] Nome do arquivo compactado no padrão pedido

[ ] Nome dos integrantes no arquivo principal

```

  

---

  

### 5.5 — Simular apresentação

  

Cada integrante precisa saber explicar:

  

```text

Lexer

Parser

AST

Tabela de símbolos

Semântico

Geração ASM

Testes

Principais dificuldades

```

  

---

  

## Critério de pronto

  

```text

[ ] Todos os testes válidos passam

[ ] Todos os testes inválidos geram erro esperado

[ ] README está completo

[ ] ZIP/RAR está no padrão

[ ] Código está organizado

[ ] Mensagens estão padronizadas

[ ] Todos conseguem explicar o projeto

```

  

---

  

# Planejamento em formato de sprint

  

## Sprint 0 — Base

  

**Foco:** entender o trabalho e preparar o projeto.

  

Entregáveis:

  

```text

Projeto Java

Main com argumentos

docs/tokens.md

docs/gramatica.md

arquivos .LC de teste

```

  

Não avançar sem isso.

  

---

  

## Sprint 1 — Lexer

  

**Foco:** transformar texto em tokens.

  

Entregáveis:

  

```text

Lexer funcionando

TokenType completo

Erros léxicos

Testes léxicos

```

  

Não avançar para parser antes de o lexer estar confiável.

  

---

  

## Sprint 2 — Parser/AST

  

**Foco:** transformar tokens em estrutura.

  

Entregáveis:

  

```text

Parser funcionando

AST criada

Expressões com precedência

Erros sintáticos

Testes sintáticos

```

  

Não avançar para semântico antes de o parser aceitar programas completos.

  

---

  

## Sprint 3 — Semântico

  

**Foco:** validar significado.

  

Entregáveis:

  

```text

Tabela de símbolos

Verificação de declaração

Verificação de tipos

Erros semânticos

Testes semânticos

```

  

Não avançar para geração ASM antes de programas inválidos serem bloqueados.

  

---

  

## Sprint 4 — Geração ASM

  

**Foco:** transformar AST em Assembly.

  

Entregáveis:

  

```text

ASM base

Declarações

Atribuições

Expressões

Entrada/saída

Se/senao

Enquanto

Testes no MASM

```

  

---

  

## Sprint 5 — Fechamento

  

**Foco:** entregar bem.

  

Entregáveis:

  

```text

README

ZIP/RAR

Testes finais

Revisão do código

Simulação da apresentação

```

  

---

  

# Kanban do projeto

  

## Backlog

  

```text

[0.1] Criar projeto Java

[0.2] Criar Main com args

[0.3] Criar docs/tokens.md

[0.4] Criar docs/gramatica.md

[0.5] Criar testes .LC

  

[1.1] Criar Token

[1.2] Criar TokenType

[1.3] Criar Lexer

[1.4] Reconhecer palavras reservadas

[1.5] Reconhecer IDs

[1.6] Reconhecer números

[1.7] Reconhecer strings

[1.8] Reconhecer operadores

[1.9] Reconhecer comentários

[1.10] Implementar erros léxicos

  

[2.1] Criar Parser

[2.2] Parsear programa

[2.3] Parsear declarações

[2.4] Parsear atribuição

[2.5] Parsear leia

[2.6] Parsear escreva

[2.7] Parsear se/senao

[2.8] Parsear enquanto

[2.9] Parsear expressões

[2.10] Criar AST

  

[3.1] Criar Symbol

[3.2] Criar SymbolTable

[3.3] Registrar declarações

[3.4] Detectar duplicidade

[3.5] Detectar variável não declarada

[3.6] Inferir tipos de expressão

[3.7] Validar atribuições

[3.8] Validar condições

[3.9] Validar leia/escreva

  

[4.1] Criar AsmGenerator

[4.2] Gerar cabeçalho ASM

[4.3] Gerar segmento de dados

[4.4] Gerar atribuições

[4.5] Gerar expressões

[4.6] Gerar escreva

[4.7] Gerar leia

[4.8] Gerar se/senao

[4.9] Gerar enquanto

[4.10] Testar MASM

  

[5.1] Criar README

[5.2] Padronizar mensagens

[5.3] Rodar bateria de testes

[5.4] Revisar código

[5.5] Gerar ZIP/RAR

[5.6] Simular apresentação

```

  

---

  

# Método de estudo e implementação

  

Para entendimento completo, usar sempre este ciclo:

  

```text

1. Ler a especificação da parte

2. Criar 3 exemplos válidos

3. Criar 3 exemplos inválidos

4. Implementar só o suficiente para passar esses testes

5. Explicar em voz alta o que foi feito

6. Só então avançar

```

  

Exemplo para o lexer:

  

```text

Antes de codar identificadores:

- criar exemplo com ID válido

- criar exemplo com palavra reservada

- criar exemplo com ID inválido

- implementar

- testar

- explicar

```

  

---

  

# Regra de ouro do projeto

  

Não pensar assim:

  

```text

"Preciso fazer um compilador."

```

  

Pensar assim:

  

```text

"Preciso transformar texto em tokens."

"Depois tokens em AST."

"Depois AST em programa validado."

"Depois programa validado em ASM."

```

  

Seguindo essa separação, o trabalho deixa de ser um problema gigante e vira uma sequência de problemas pequenos, testáveis e controláveis.

  

---

  

# Checklist final antes da entrega

  

```text

[ ] Projeto executa por linha de comando

[ ] Recebe arquivo .LC

[ ] Gera arquivo .ASM

[ ] Lexer funciona

[ ] Parser funciona

[ ] AST ou estrutura intermediária funciona

[ ] Tabela de símbolos funciona

[ ] Semântico funciona

[ ] Geração de código funciona

[ ] MASM monta o arquivo gerado

[ ] Programas válidos executam

[ ] Programas inválidos geram erro correto

[ ] README pronto

[ ] Testes incluídos

[ ] ZIP/RAR no padrão pedido

[ ] Todos os integrantes sabem explicar o projeto

```