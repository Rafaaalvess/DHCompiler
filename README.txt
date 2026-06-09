========================================================
  COMPILADOR BRL -- Trabalho Pratico de Compiladores
  Faculdade Dom Helder -- Ciencia da Computacao
  Professor: Prof. Dr. Marcos W. Rodrigues
========================================================

1. INTEGRANTES
   Rafael De Andrade Alves
   Vinicius Barros Marinho

--------------------------------------------------------

2. COMO COMPILAR

   javac -cp src -d bin src/error/*.java src/lexer/*.java src/semantic/*.java src/codegen/*.java src/parser/*.java src/main/*.java

   Ou, se o seu shell suportar glob recursivo:
   javac -cp src -d bin src/**/*.java

   Requisitos: Java 8 ou superior. Sem dependencias externas.

--------------------------------------------------------

3. COMO EXECUTAR

   java -cp bin main.Main <arquivo_fonte.LC> <saida.ASM>

4. EXEMPLO DE USO

   java -cp bin main.Main programa.LC saida.ASM

   Em caso de sucesso:
     Compilacao concluida: saida.ASM

   Em caso de erro:
     Erro lexico na linha X: <descricao>
     Erro sintatico na linha X: <descricao>
     Erro semantico na linha X: <descricao>

--------------------------------------------------------

5. ESTRUTURA DO PROJETO

   src/main/      - ponto de entrada (Main.java)
   src/lexer/     - analise lexica (Lexer, Token, TokenType)
   src/parser/    - analise sintatica (Parser)
   src/semantic/  - analise semantica (SemanticAnalyzer, SymbolTable, Symbol)
   src/codegen/   - geracao de codigo Assembly (CodeGenerator, AsmWriter)
   src/error/     - excecoes do compilador (CompilerException e subclasses)
   tests/validos/ - programas BRL validos para teste
   tests/invalidos/ - programas BRL invalidos para teste

--------------------------------------------------------

6. FASES DO COMPILADOR

   Lexica:    transforma o arquivo .LC em uma sequencia de tokens
   Sintatica: valida a estrutura gramatical do programa
   Semantica: verifica tipos e declaracoes de variaveis
   Codegen:   gera o arquivo .ASM para MASM 80x86 (DOS)

   O .ASM gerado usa interrupcoes DOS (INT 21h) e e montavel
   com MASM (ml.exe). Para executar o .EXE resultante e necessario
   um ambiente DOS ou emulador como DOSBox.

--------------------------------------------------------

7. MAPEAMENTO DE TIPOS BRL -> MASM

   inteiro   -> DD ?             (4 bytes, acesso via WORD PTR)
   real      -> DD ?             (4 bytes, acesso via WORD PTR)
   logico    -> DB ?             (1 byte, 0h=falso, FFh=verdadeiro)
   caractere -> DB 512 DUP(?)   (512 bytes, terminado por '$')

--------------------------------------------------------

8. LIMITACOES CONHECIDAS

   - Tipo real nao suporta aritmetica de ponto flutuante; valores
     reais sao tratados como inteiros (parte inteira apenas).
   - Operacao de concatenacao de strings ('+' entre caractere)
     e aceita semanticamente mas nao gera codigo de concatenacao;
     use atribuicao direta para strings.
   - Inteiros limitados ao intervalo 16 bits (-32768 a 32767)
     devido ao uso de registradores AX/BX de 16 bits do 8086.

========================================================
