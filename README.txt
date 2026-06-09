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

   Opcao A — via JAR (nome BRL conforme enunciado):
     java -jar BRL.jar <arquivo_fonte.LC> <saida.ASM>

   Opcao B — via wrapper BRL.bat (Windows):
     BRL.bat <arquivo_fonte.LC> <saida.ASM>

   Opcao C — via classpath (desenvolvimento):
     java -cp bin main.Main <arquivo_fonte.LC> <saida.ASM>

   Para gerar o BRL.jar a partir do codigo-fonte:
     build_jar.bat

4. EXEMPLO DE USO

   java -jar BRL.jar programa.LC saida.ASM
   BRL.bat programa.LC saida.ASM

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

   inteiro   -> DD ?             (4 bytes, acesso via DWORD PTR / EAX)
   real      -> DD ?             (4 bytes, acesso via DWORD PTR / EAX)
   logico    -> DB ?             (1 byte, 0h=falso, FFh=verdadeiro, via BYTE PTR / AL)
   caractere -> DB 512 DUP(?)   (512 bytes, terminado por '$')

   O ASM gerado usa a diretiva .386 para operacoes de 32 bits (EAX, EBX,
   DWORD PTR). O montador MASM e o ambiente de execucao (DOSBox/DOS real)
   precisam suportar 80386+.

--------------------------------------------------------

8. LIMITACOES CONHECIDAS

   - Tipo real e representado internamente em ponto fixo com 4 casas
     decimais dentro de DD. Literais, atribuicoes, comparacoes e operacoes
     aritmeticas basicas preservam essa escala.
   - A leitura de valores reais pelo teclado aceita entrada inteira e
     converte para a escala interna.
   - Operacao de concatenacao de strings ('+' entre caractere) e comparacao
     de igualdade entre strings ('==') geram rotinas auxiliares no ASM.

========================================================
