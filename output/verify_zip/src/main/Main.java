/*
 * Compiladores -- Trabalho Pratico
 * Faculdade Dom Helder -- Ciencia da Computacao
 * Professor: Prof. Dr. Marcos W. Rodrigues
 *
 * Integrantes:
 *   Rafael De Andrade Alves
 *   Vinicius Barros Marinho
 */
package main;

import codegen.CodeGenerator;
import error.LexicalException;
import error.SemanticException;
import error.SyntaxException;
import lexer.Lexer;
import parser.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso: BRL <arquivo_fonte.LC> <saida.ASM>");
            System.exit(1);
        }

        String sourceFile = args[0];
        String outputFile = args[1];

        validateArguments(sourceFile, outputFile);

        String source = readSourceFile(sourceFile);

        compile(source, outputFile);
    }

    // -------------------------------------------------------------------------
    // Validação dos argumentos de linha de comando
    // -------------------------------------------------------------------------

    private static void validateArguments(String sourceFile, String outputFile) {
        if (!sourceFile.endsWith(".LC")) {
            System.err.println("Erro: arquivo fonte deve ter extensao .LC");
            System.exit(1);
        }

        if (!outputFile.endsWith(".ASM")) {
            System.err.println("Erro: arquivo de saida deve ter extensao .ASM");
            System.exit(1);
        }

        File source = new File(sourceFile);

        if (!source.exists()) {
            System.err.println("Erro: arquivo fonte nao encontrado: " + sourceFile);
            System.exit(1);
        }

        if (!source.canRead()) {
            System.err.println("Erro: sem permissao de leitura: " + sourceFile);
            System.exit(1);
        }

        File output = new File(outputFile);
        File outputDir = output.getParentFile();

        if (outputDir != null && !outputDir.exists()) {
            System.err.println("Erro: diretorio de saida nao existe: " + outputDir.getPath());
            System.exit(1);
        }
    }

    // -------------------------------------------------------------------------
    // Leitura do arquivo fonte
    // -------------------------------------------------------------------------

    private static String readSourceFile(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            return new String(bytes, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Pipeline do compilador
    // -------------------------------------------------------------------------

    private static void compile(String source, String outputFile) {
        try {
            Lexer lexer = new Lexer(source);
            Parser parser = new Parser(lexer);
            CodeGenerator codegen = new CodeGenerator(outputFile);
            parser.setCodeGenerator(codegen);
            parser.parseProgram();
            codegen.writeFile(parser.getSemanticAnalyzer().getSymbolTable());
            System.out.println("Compilacao concluida: " + outputFile);

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
    }
}
