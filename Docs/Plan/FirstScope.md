# Compilador BRL — Índice do Planejamento

**Disciplina:** Compiladores — Ciência da Computação, Dom Helder  
**Professor:** Prof. Dr. Marcos W. Rodrigues  
**Entrega Final:** 08/06/2026  
**Implementação:** Java | Saída: Assembly x86 (MASM)

---

## Sprints

| Sprint | Arquivo | Fase | Entregável |
|--------|---------|------|------------|
| 1 | [Sprint1.md](Sprint1.md) | Infraestrutura + Análise Léxica | `Lexer.java` funcional + todos os tokens reconhecidos |
| 2 | [Sprint2.md](Sprint2.md) | Análise Sintática | `Parser.java` com gramática BRL completa |
| 3 | [Sprint3.md](Sprint3.md) | Análise Semântica | `SymbolTable` + verificação de tipos e declarações |
| 4 | [Sprint4.md](Sprint4.md) | Geração de Código Assembly | Arquivo `.ASM` válido, montável e executável no MASM |
| 5 | [Sprint5.md](Sprint5.md) | Integração, Testes e Entrega | Compilador BRL completo + ZIP no padrão |

**Regra:** Não iniciar a sprint seguinte sem que todos os critérios de aceite da sprint atual estejam marcados.

---

## Fluxo do Compilador

```
Arquivo .LC
     │
     ▼
  Lexer          → tokens (Sprint 1)
     │
     ▼
  Parser         → validação sintática (Sprint 2)
     │
     ▼
  SemanticAnalyzer → validação de tipos e declarações (Sprint 3)
     │
     ▼
  CodeGenerator  → arquivo .ASM (Sprint 4)
     │
     ▼
  MASM           → executável .EXE
```

---

## Referências do Enunciado

| Item | Especificação |
|------|---------------|
| Linguagem de implementação | Java |
| Nome do executável | `BRL` |
| Argumento 1 | Arquivo fonte com extensão `.LC` |
| Argumento 2 | Arquivo assembly a gerar com extensão `.ASM` |
| Montador | MASM (Microsoft Assembler) |
| Tipos de dados | `inteiro` (4B, -2³¹ a 2³¹-1), `caractere` (512B, `$` como terminador em memória, max 255 chars úteis), `logico` (1B, 0h/FFh), `real` (4B, 7 dígitos) |
| Operador de igualdade | `==` (dois sinais de igual) |
| Instrução leia | `leia ( id [, id] )` — aceita múltiplos identificadores |
| Negação lógica | `not` (palavra reservada, maior precedência após parênteses) |
| Precedência de operadores | parênteses > `not` > `* / && div mod` > `+ - ou` > `== <> < > <= >=` |
| Concatenação de strings | Operador `+` aplicado ao tipo `caractere` |
| Comentários | `/* ... */` (uma ou múltiplas linhas) |
| Case sensitive | Sim |
| Identificadores | Começam com letra ou `_`, máximo 512 caracteres |
| Entrega | ZIP/RAR com nomes dos integrantes separados por `_`, nomes no arquivo principal |
| Data de entrega | 08/06/2026 |

---

## Ambiguidades do Enunciado a Confirmar com o Professor

| # | Ambiguidade |
|---|-------------|
| 1 | Tabela de palavras reservadas lista `leitura` e `escrita`; formatos de instrução usam `leia` e `escreva` — qual é o correto? |
| 2 | Página 1 do PDF: identificador começa com "letra ou sublinhado"; Convenção Léxica (pág. 3): "começa com letra" — o `_` pode iniciar um identificador? |
