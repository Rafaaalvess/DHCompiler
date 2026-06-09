@echo off
echo Compilando fontes Java...
javac -cp src -d bin src/error/*.java src/lexer/*.java src/semantic/*.java src/codegen/*.java src/parser/*.java src/main/*.java
if errorlevel 1 (
    echo ERRO: falha na compilacao Java.
    exit /b 1
)

echo Empacotando BRL.jar...
jar cfm BRL.jar MANIFEST.MF -C bin .
if errorlevel 1 (
    echo ERRO: falha ao criar BRL.jar.
    exit /b 1
)

echo Pronto: BRL.jar gerado com sucesso.
echo Uso: java -jar BRL.jar ^<fonte.LC^> ^<saida.ASM^>
echo      BRL.bat ^<fonte.LC^> ^<saida.ASM^>
