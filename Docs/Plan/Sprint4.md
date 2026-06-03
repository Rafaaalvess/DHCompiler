# Sprint 4 — Geração de Código Assembly x86

**Pré-requisito:** Sprint 3 completa — Análise semântica funcional, tabela de símbolos preenchida  
**Próxima sprint:** Sprint 5 só começa quando todos os critérios de aceite desta estiverem marcados

---

## Objetivo

Implementar o **gerador de código**: traduzir o programa BRL semanticamente validado em um arquivo `.ASM` para a arquitetura 80x86, montável pelo MASM. Ao final desta sprint, qualquer programa BRL válido deve produzir um `.ASM` que pode ser montado, linkado e executado, produzindo a saída esperada.

---

## Critério de Aceite

- [ ] Para cada programa BRL válido é gerado um arquivo `.ASM` no caminho especificado
- [ ] O arquivo `.ASM` gerado é montado pelo MASM sem erros
- [ ] O executável gerado roda e produz a saída correta
- [ ] Todas as variáveis declaradas aparecem no segmento `.data` com o tamanho correto
- [ ] Atribuições simples e com expressões funcionam
- [ ] `escreva` com string e com inteiro funcionam
- [ ] `leia` lê valor do teclado e armazena na variável
- [ ] `se` com e sem `senao` desvia o fluxo corretamente
- [ ] `enquanto` repete corretamente e para quando a condição é falsa
- [ ] Labels gerados são únicos — dois `se` aninhados não conflitam

---

## Tarefas

### 4.1 — Estrutura do Arquivo Assembly MASM

**O que fazer:** Entender e implementar o esqueleto obrigatório de um programa MASM 80x86.

**Template base que deve ser gerado:**
```asm
; Compilador BRL — gerado automaticamente
SSEG    SEGMENT STACK
        DB 256 DUP(?)
SSEG    ENDS

DSEG    SEGMENT DATA
        ; variáveis declaradas aqui
DSEG    ENDS

CSEG    SEGMENT CODE
        ASSUME CS:CSEG, DS:DSEG, SS:SSEG

strt:
        MOV AX, DSEG
        MOV DS, AX

        ; código do programa aqui

        MOV AH, 4Ch
        INT 21h

CSEG    ENDS
        END strt
```

**Criar `AsmWriter.java`:** classe auxiliar para escrever o arquivo com indentação correta.
```java
public class AsmWriter {
    private final PrintWriter writer;

    public void writeLine(String line)           // linha com indentação padrão (8 espaços)
    public void writeLabel(String label)         // label sem indentação, seguido de ':'
    public void writeComment(String comment)     // linha de comentário '; texto'
    public void writeBlankLine()
    public void close()
}
```

---

### 4.2 — Alocação de Variáveis no Segmento de Dados

**O que fazer:** Para cada símbolo na tabela de símbolos, gerar a diretiva MASM correspondente no `DSEG`.

**Mapeamento de tipos:**

| Tipo BRL | Diretiva MASM | Tamanho | Observação |
|---|---|---|---|
| `inteiro` | `DD ?` | 4 bytes | -2³¹ a 2³¹-1 |
| `real` | `DD ?` | 4 bytes | ponto flutuante 7 dígitos |
| `logico` | `DB ?` | 1 byte | 0h = falso, FFh = verdadeiro |
| `caractere` | `DB 512 DUP(?)` | 512 bytes | terminado por `$` para DOS int 21h |

**Exemplo de saída gerada para:**
```
x : inteiro;
nome : caractere;
ativo : logico;
pi : real;
```

```asm
DSEG    SEGMENT DATA
x       DD ?
nome    DB 512 DUP(?)
ativo   DB ?
pi      DD ?
DSEG    ENDS
```

**`CodeGenerator.java` — método `generateData()`:**
```java
void generateData() {
    writer.writeLine("DSEG    SEGMENT DATA");
    for (Symbol s : symbolTable.getAll()) {
        String directive = switch (s.type) {
            case "inteiro"   -> "DD ?";
            case "real"      -> "DD ?";
            case "logico"    -> "DB ?";
            case "caractere" -> "DB 512 DUP(?)";
        };
        writer.writeLine(s.name + "    " + directive);
    }
    writer.writeLine("DSEG    ENDS");
}
```

---

### 4.3 — Gerador de Labels Únicos

**O que fazer:** Criar um contador global de labels para garantir unicidade.

```java
private int labelCounter = 0;

private String newLabel() {
    return "L" + (labelCounter++);
}
```

Uso: `newLabel()` retorna `L0`, `L1`, `L2`, etc.  
Para `se`: usar 2 ou 3 labels. Para `enquanto`: usar 2 labels.

---

### 4.4 — Geração de Expressões Aritméticas

**Estratégia:** Usar a pilha da CPU (instruções `PUSH`/`POP`) para avaliar expressões compostas. Resultado final sempre fica em `AX`.

**Geração de constante inteira:**
```asm
MOV AX, 10
```

**Geração de variável:**
```asm
MOV AX, [x]
```

**Geração de operação binária (ex: `a + b`):**
```asm
; avaliar 'a' → AX
MOV AX, [a]
PUSH AX         ; salvar resultado de 'a' na pilha

; avaliar 'b' → AX
MOV AX, [b]
MOV BX, AX      ; BX = b

POP AX          ; AX = a (recuperar da pilha)
ADD AX, BX      ; AX = a + b
```

**Operações suportadas:**

| Operação BRL | Instruções MASM |
|---|---|
| `a + b` | `ADD AX, BX` |
| `a - b` | `SUB AX, BX` |
| `a * b` | `IMUL BX` (resultado em DX:AX) |
| `a / b` | `CDQ` + `IDIV BX` (quociente em AX) |
| `a div b` | igual a `/` para inteiros |
| `a mod b` | `CDQ` + `IDIV BX` (resto em DX, `MOV AX, DX`) |

**Unário negativo `-x`:**
```asm
MOV AX, [x]
NEG AX
```

---

### 4.5 — Geração de Expressões Lógicas e Relacionais

**Estratégia para lógicos:** `0000h` = falso, `FFFFh` = verdadeiro (trabalhar com 2 bytes em AX).

**Geração de comparação (ex: `a > b`):**
```asm
MOV AX, [a]
PUSH AX
MOV AX, [b]
MOV BX, AX
POP AX              ; AX = a, BX = b
CMP AX, BX
JG  label_true
MOV AX, 0           ; falso
JMP label_end
label_true:
MOV AX, 0FFFFh      ; verdadeiro
label_end:
```

**Tabela de saltos condicionais por operador:**

| Operador | Instrução de salto (se verdadeiro) |
|---|---|
| `==` | `JE` |
| `<>` | `JNE` |
| `<` | `JL` |
| `>` | `JG` |
| `<=` | `JLE` |
| `>=` | `JGE` |

**Geração de `not`:**
```asm
MOV AX, [a]
XOR AX, 0FFFFh  ; inverte todos os bits
AND AX, 0FFFFh  ; garante que só 0 ou FFFFh
```

**Geração de `&&` (AND lógico):**
```asm
; a em AX, b em BX
AND AX, BX
```

**Geração de `ou` (OR lógico):**
```asm
; a em AX, b em BX
OR AX, BX
```

---

### 4.6 — Geração de Atribuição

**Formato:** `x := EXP;`

```asm
; avaliar EXP → AX
; ...código da expressão...
MOV [x], AX    ; armazenar resultado em x
```

**Método `genAtribuicao(String varName, <nó da expressão>)`:**
```java
void genAtribuicao(String varName) {
    genExp(...);          // gera código da expressão, resultado em AX
    writer.writeLine("MOV [" + varName + "], AX");
}
```

---

### 4.7 — Geração de escreva

**Para strings (`caractere`):**
Usar interrupção DOS `int 21h` com `ah=09h`. A string deve terminar com `$`.

```asm
LEA DX, [nome]   ; DX aponta para o início da variável
MOV AH, 09h
INT 21h
```

**Para inteiros:** A interrupção DOS não imprime inteiros diretamente. Converter o inteiro em string de dígitos e então usar `09h`.

Alternativa mais simples (aceita para este trabalho):
```asm
; Para imprimir inteiro em AX:
; Usar rotina de conversão int→string e depois int 21h / ah=09h
```

> Implementar uma sub-rotina `PRINT_INT` no template base do arquivo ASM que converte AX em dígitos ASCII e imprime via DOS.

**Para `verdadeiro`/`falso`:** Tratar como string estática. Definir no `.data`:
```asm
STR_VERDADEIRO  DB "verdadeiro$"
STR_FALSO       DB "falso$"
```
E ao escrever um lógico:
```asm
; se AX == 0FFFFh
CMP AX, 0FFFFh
JE  imprimir_verdadeiro
LEA DX, STR_FALSO
JMP imprimir_string
imprimir_verdadeiro:
LEA DX, STR_VERDADEIRO
imprimir_string:
MOV AH, 09h
INT 21h
```

---

### 4.8 — Geração de leia

**Para inteiros:**
Usar interrupção DOS `int 21h` com `ah=01h` (lê 1 char) em loop, convertendo dígitos ASCII para valor binário em AX. Alternativa: ler linha com `ah=0Ah` e converter.

```asm
; Chamar sub-rotina READ_INT que lê dígitos e retorna valor em AX
CALL READ_INT
MOV [x], AX
```

> Implementar `READ_INT` como sub-rotina no template base.

**Para strings (`caractere`):**
```asm
; Ler string para buffer 'nome' usando int 21h / ah=0Ah (leitura com tamanho)
; Adicionar '$' ao final após a leitura
```

---

### 4.9 — Geração de se / senao

**`se EXP entao inicio instrucoes fim`:**

```asm
; código da condição EXP → AX
CMP AX, 0           ; se EXP é falso (AX == 0)
JE  L1              ; pular o bloco then
; código das instrucoes do then
L1:
```

**`se EXP entao inicio instrucoes_then fim senao inicio instrucoes_else fim`:**

```asm
; código da condição EXP → AX
CMP AX, 0
JE  L1              ; se falso: ir para else
; código das instrucoes_then
JMP L2              ; pular o else
L1:
; código das instrucoes_else
L2:
```

**Método `genSe()`:**
```java
void genSe(boolean hasSenao) {
    String labelElse = newLabel();
    String labelEnd  = newLabel();

    genExp(...);                          // condição → AX
    writer.writeLine("CMP AX, 0");
    writer.writeLine("JE  " + labelElse);
    genInstrucoes(...);                   // bloco then
    if (hasSenao) {
        writer.writeLine("JMP " + labelEnd);
    }
    writer.writeLabel(labelElse);
    if (hasSenao) {
        genInstrucoes(...);               // bloco else
        writer.writeLabel(labelEnd);
    }
}
```

---

### 4.10 — Geração de enquanto

**`enquanto EXP faca inicio instrucoes fim`:**

```asm
L0:                             ; início do loop
; código da condição EXP → AX
CMP AX, 0
JE  L1                          ; se falso: sair do loop
; código das instrucoes
JMP L0                          ; voltar ao início
L1:                             ; saída do loop
```

**Método `genEnquanto()`:**
```java
void genEnquanto() {
    String labelStart = newLabel();
    String labelEnd   = newLabel();

    writer.writeLabel(labelStart);
    genExp(...);                          // condição → AX
    writer.writeLine("CMP AX, 0");
    writer.writeLine("JE  " + labelEnd);
    genInstrucoes(...);                   // corpo do loop
    writer.writeLine("JMP " + labelStart);
    writer.writeLabel(labelEnd);
}
```

---

## Testes

### Testes de geração de código

**`tests/validos/cod_atribuicao_simples.LC`**
```
inicio atr;
  x : inteiro;
  x := 42;
fim
```
Verificar no `.ASM`:
- `x DD ?` no DSEG
- `MOV AX, 42` + `MOV [x], AX` no CSEG

---

**`tests/validos/cod_expressao_soma.LC`**
```
inicio soma;
  a : inteiro;
  b : inteiro;
  c : inteiro;
  a := 10;
  b := 3;
  c := a + b;
fim
```
Verificar: três variáveis no DSEG, instruções ADD no CSEG, resultado em `c`

---

**`tests/validos/cod_escreva_string.LC`**
```
inicio ola;
  msg : caractere;
  msg := "Ola Mundo";
  escreva(msg);
fim
```
Verificar: `LEA DX, [msg]` + `MOV AH, 09h` + `INT 21h`  
Montar e executar: deve imprimir `Ola Mundo`

---

**`tests/validos/cod_se_simples.LC`**
```
inicio cond;
  x : inteiro;
  x := 5;
  se x > 3 entao inicio
    x := 100;
  fim
  escreva(x);
fim
```
Verificar: labels L0 e L1 gerados, `CMP` + `JE`, saída deve ser `100`

---

**`tests/validos/cod_se_senao.LC`**
```
inicio cond;
  x : inteiro;
  x := 2;
  se x > 3 entao inicio
    x := 100;
  fim
  senao inicio
    x := 0;
  fim
  escreva(x);
fim
```
Saída esperada ao executar: `0`

---

**`tests/validos/cod_enquanto_contador.LC`**
```
inicio contador;
  i : inteiro;
  i := 1;
  enquanto i <= 5 faca inicio
    escreva(i);
    i := i + 1;
  fim
fim
```
Saída esperada ao executar: `12345` (ou com separadores, dependendo do `escreva`)

---

**`tests/validos/cod_leia_e_escreva.LC`**
```
inicio leio;
  n : inteiro;
  leia(n);
  escreva(n);
fim
```
Executar manualmente: digitar `7`, esperar saída `7`

---

### Checklist de validação no MASM

Para cada `.ASM` gerado:
- [ ] `ml /Zi arquivo.ASM /link` (ou equivalente) sem erros
- [ ] Arquivo `.EXE` gerado
- [ ] Executar e verificar saída correta
- [ ] Nenhum crash ou comportamento inesperado
