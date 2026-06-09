package codegen;

import semantic.Symbol;
import semantic.SymbolTable;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerador de codigo para arquitetura 80x86 (MASM), modo real DOS.
 * Requer processador 80386+ (diretiva .386 emitida no arquivo ASM).
 *
 * Tipos e tamanhos conforme especificacao:
 *   inteiro  -> DD ? (4 bytes, acesso via DWORD PTR / EAX)
 *   real     -> DD ? (4 bytes, acesso via DWORD PTR / EAX; sem FP)
 *   logico   -> DB ? (1 byte, 0h=falso, FFh=verdadeiro; via BYTE PTR / AL)
 *   caractere-> DB 512 DUP(?) (512 bytes, terminado por '$')
 *
 * Convencao de registradores:
 *   EAX -- acumulador de expressoes numericas e logicas
 *   EBX -- operando direito temporario
 *   DX  -- ponteiro de strings (tipo caractere, offset 16-bit)
 */
public class CodeGenerator {

    private final String outputPath;
    private final List<String> dataEntries = new ArrayList<>();
    private final List<String> codeEntries = new ArrayList<>();
    private static final int REAL_SCALE = 10000;
    private int labelCounter = 0;
    private int stringCounter = 0;

    public CodeGenerator(String outputPath) {
        this.outputPath = outputPath;
    }

    // -------------------------------------------------------------------------
    // Labels
    // -------------------------------------------------------------------------

    public String newLabel() {
        return "L" + (labelCounter++);
    }

    // -------------------------------------------------------------------------
    // Emissores internos
    // -------------------------------------------------------------------------

    private void emit(String instr) {
        codeEntries.add("        " + instr);
    }

    private void emitLbl(String label) {
        codeEntries.add(label + ":");
    }

    // -------------------------------------------------------------------------
    // Carga de valores — resultado em EAX (ou DX para caractere)
    // -------------------------------------------------------------------------

    public void emitLoadInt(String value) {
        emit("MOV EAX, " + value);
    }

    public void emitLoadReal(String value) {
        BigDecimal scaled = new BigDecimal(value).multiply(BigDecimal.valueOf(REAL_SCALE));
        emit("MOV EAX, " + scaled.setScale(0, RoundingMode.HALF_UP).intValue());
    }

    /**
     * Carrega variavel em EAX conforme o tipo:
     *   inteiro/real -> MOV EAX, DWORD PTR [name]
     *   logico       -> XOR EAX, EAX + MOV AL, BYTE PTR [name]  (EAX = 0 ou 000000FFh)
     */
    public void emitLoadVar(String name, String type) {
        if (type.equals(Symbol.LOGICO)) {
            emit("XOR EAX, EAX");
            emit("MOV AL, BYTE PTR [" + name + "]");
        } else {
            emit("MOV EAX, DWORD PTR [" + name + "]");
        }
    }

    /** Carrega o endereco de uma variavel caractere em DX. */
    public void emitLeaDX(String varName) {
        emit("LEA DX, [" + varName + "]");
    }

    /** Define literal string no segmento de dados e carrega endereco em DX. */
    public void emitLoadString(String content) {
        String label = "_STR" + (stringCounter++);
        dataEntries.add(String.format("%-16s DB \"%s\", '$'", label, content));
        emit("LEA DX, " + label);
    }

    /** Verdadeiro: EAX = 000000FFh (1 byte armazenado via AL). */
    public void emitLoadTrue() {
        emit("MOV EAX, 0FFh");
    }

    /** Falso: EAX = 0. */
    public void emitLoadFalse() {
        emit("MOV EAX, 0");
    }

    // -------------------------------------------------------------------------
    // Operacoes de pilha e unarias
    // -------------------------------------------------------------------------

    public void emitPushAX() {
        emit("PUSH EAX");
    }

    public void emitUnaryNeg() {
        emit("NEG EAX");
    }

    /** NOT logico: inverte entre 0 e 000000FFh preservando zeros nos bits 8-31. */
    public void emitUnaryNot() {
        emit("XOR AL, 0FFh");
        emit("AND EAX, 0FFh");
    }

    // -------------------------------------------------------------------------
    // Operacoes binarias aritmeticas e logicas
    // Pressupoe que o operando esquerdo foi salvo com PUSH EAX antes de
    // avaliar o operando direito (que agora esta em EAX).
    // -------------------------------------------------------------------------

    public void emitBinaryOp(String op, String leftType, String rightType, String resultType) {
        emit("MOV EBX, EAX");
        emit("POP EAX");
        if (resultType.equals(Symbol.REAL)) {
            emitNumericPromotion(leftType, rightType);
        }
        switch (op) {
            case "+":   emit("ADD EAX, EBX");                                break;
            case "-":   emit("SUB EAX, EBX");                                break;
            case "*":
                emit("IMUL EBX");
                if (resultType.equals(Symbol.REAL)) {
                    emit("MOV EBX, " + REAL_SCALE);
                    emit("IDIV EBX");
                }
                break;
            case "/":
                if (resultType.equals(Symbol.REAL)) emit("IMUL EAX, " + REAL_SCALE);
                emit("CDQ");
                emit("IDIV EBX");
                break;
            case "div": emit("CDQ"); emit("IDIV EBX");                       break;
            case "mod": emit("CDQ"); emit("IDIV EBX"); emit("MOV EAX, EDX"); break;
            case "&&":  emit("AND EAX, EBX");                                break;
            case "ou":  emit("OR EAX, EBX");                                 break;
        }
    }

    private void emitNumericPromotion(String leftType, String rightType) {
        if (leftType.equals(Symbol.INTEIRO)) {
            emit("IMUL EAX, " + REAL_SCALE);
        }
        if (rightType.equals(Symbol.INTEIRO)) {
            emit("IMUL EBX, " + REAL_SCALE);
        }
    }

    // -------------------------------------------------------------------------
    // Operadores relacionais — resultado 0h (falso) ou FFh (verdadeiro)
    // -------------------------------------------------------------------------

    public void emitRelop(String op, String leftType, String rightType) {
        String labelTrue = newLabel();
        String labelEnd  = newLabel();
        emit("MOV EBX, EAX");
        emit("POP EAX");
        if (leftType.equals(Symbol.REAL) || rightType.equals(Symbol.REAL)) {
            emitNumericPromotion(leftType, rightType);
        }
        emit("CMP EAX, EBX");
        emit(jumpFor(op) + " " + labelTrue);
        emit("MOV EAX, 0");
        emit("JMP " + labelEnd);
        emitLbl(labelTrue);
        emit("MOV EAX, 0FFh");
        emitLbl(labelEnd);
    }

    public void emitPushDX() {
        emit("PUSH DX");
    }

    public void emitStringConcat() {
        String label = "_STR" + (stringCounter++);
        dataEntries.add(String.format("%-16s DB 512 DUP(?)", label));
        emit("MOV BX, DX");
        emit("POP SI");
        emit("LEA DI, [" + label + "]");
        emit("MOV DX, BX");
        emit("CALL STRCAT_PROC");
        emit("LEA DX, [" + label + "]");
    }

    public void emitStringEqual() {
        emit("MOV BX, DX");
        emit("POP SI");
        emit("MOV DI, BX");
        emit("CALL STRCMP_PROC");
        emit("XOR EAX, EAX");
        emit("MOV AL, BL");
    }

    private String jumpFor(String op) {
        switch (op) {
            case "==": return "JE ";
            case "<>": return "JNE";
            case "<":  return "JL ";
            case ">":  return "JG ";
            case "<=": return "JLE";
            case ">=": return "JGE";
            default:   return "JE ";
        }
    }

    // -------------------------------------------------------------------------
    // Atribuicao
    // -------------------------------------------------------------------------

    /**
     * Armazena EAX na variavel conforme o tipo:
     *   inteiro/real -> MOV DWORD PTR [name], EAX
     *   logico       -> MOV BYTE PTR [name], AL
     */
    public void genStore(String varName, String type) {
        if (type.equals(Symbol.LOGICO)) {
            emit("MOV BYTE PTR [" + varName + "], AL");
        } else {
            emit("MOV DWORD PTR [" + varName + "], EAX");
        }
    }

    public void emitIntToReal() {
        emit("IMUL EAX, " + REAL_SCALE);
    }

    /** Copia string de DX para o buffer da variavel caractere. */
    public void genStrCopy(String varName) {
        emit("MOV SI, DX");
        emit("LEA DI, [" + varName + "]");
        emit("CALL STRCPY_PROC");
    }

    // -------------------------------------------------------------------------
    // Fluxo de controle
    // -------------------------------------------------------------------------

    public void emitCmpAX0() {
        emit("CMP EAX, 0");
    }

    public void emitJE(String label) {
        emit("JE  " + label);
    }

    public void emitJMP(String label) {
        emit("JMP " + label);
    }

    public void emitLabel(String label) {
        emitLbl(label);
    }

    // -------------------------------------------------------------------------
    // Entrada / Saida
    // -------------------------------------------------------------------------

    public void genLeia(String varName, String type) {
        if (type.equals(Symbol.CARACTERE)) {
            emit("LEA DI, [" + varName + "]");
            emit("CALL READ_STR");
        } else if (type.equals(Symbol.LOGICO)) {
            emit("CALL READ_INT");
            emit("MOV BYTE PTR [" + varName + "], AL");
        } else if (type.equals(Symbol.REAL)) {
            emit("CALL READ_INT");
            emit("IMUL EAX, " + REAL_SCALE);
            emit("MOV DWORD PTR [" + varName + "], EAX");
        } else {
            emit("CALL READ_INT");
            emit("MOV DWORD PTR [" + varName + "], EAX");
        }
    }

    public void genEscreva(String type) {
        if (type.equals(Symbol.CARACTERE)) {
            emit("MOV AH, 09h");
            emit("INT 21h");
        } else if (type.equals(Symbol.LOGICO)) {
            String labelTrue = newLabel();
            String labelEnd  = newLabel();
            emit("CMP EAX, 0FFh");
            emit("JE  " + labelTrue);
            emit("LEA DX, STR_FALSO");
            emit("JMP " + labelEnd);
            emitLbl(labelTrue);
            emit("LEA DX, STR_VERDADEIRO");
            emitLbl(labelEnd);
            emit("MOV AH, 09h");
            emit("INT 21h");
        } else {
            if (type.equals(Symbol.REAL)) emit("CALL PRINT_REAL");
            else emit("CALL PRINT_INT");
        }
    }

    // -------------------------------------------------------------------------
    // Escrita do arquivo ASM completo
    // -------------------------------------------------------------------------

    public void writeFile(SymbolTable symbolTable) throws IOException {
        AsmWriter w = new AsmWriter(outputPath);
        w.writeRaw(".386");
        w.writeComment("Compilador BRL -- gerado automaticamente");
        w.writeBlankLine();
        writeStackSegment(w);
        writeDataSegment(w, symbolTable);
        writeCodeSegment(w);
        w.close();
    }

    private void writeStackSegment(AsmWriter w) {
        w.writeRaw("SSEG    SEGMENT STACK");
        w.writeLine("DB 256 DUP(?)");
        w.writeRaw("SSEG    ENDS");
        w.writeBlankLine();
    }

    private void writeDataSegment(AsmWriter w, SymbolTable symbolTable) {
        w.writeRaw("DSEG    SEGMENT DATA");
        for (Symbol s : symbolTable.getAll()) {
            w.writeLine(String.format("%-16s%s", s.name, directiveFor(s.type)));
        }
        for (String entry : dataEntries) {
            w.writeLine(entry);
        }
        w.writeLine("STR_VERDADEIRO  DB \"verdadeiro$\"");
        w.writeLine("STR_FALSO       DB \"falso$\"");
        w.writeRaw("DSEG    ENDS");
        w.writeBlankLine();
    }

    private String directiveFor(String type) {
        switch (type) {
            case Symbol.INTEIRO:   return "DD ?";
            case Symbol.REAL:      return "DD ?";
            case Symbol.LOGICO:    return "DB ?";
            case Symbol.CARACTERE: return "DB 512 DUP(?)";
            default:               return "DD ?";
        }
    }

    private void writeCodeSegment(AsmWriter w) {
        w.writeRaw("CSEG    SEGMENT CODE");
        w.writeLine("ASSUME CS:CSEG, DS:DSEG, SS:SSEG");
        w.writeBlankLine();
        w.writeRaw("strt:");
        w.writeLine("MOV AX, DSEG");
        w.writeLine("MOV DS, AX");
        w.writeBlankLine();

        for (String line : codeEntries) {
            w.writeRaw(line);
        }

        w.writeBlankLine();
        w.writeLine("MOV AH, 4Ch");
        w.writeLine("INT 21h");
        w.writeBlankLine();

        writePrintInt(w);
        w.writeBlankLine();
        writePrintReal(w);
        w.writeBlankLine();
        writeReadInt(w);
        w.writeBlankLine();
        writeReadStr(w);
        w.writeBlankLine();
        writeStrCpy(w);
        w.writeBlankLine();
        writeStrCat(w);
        w.writeBlankLine();
        writeStrCmp(w);
        w.writeBlankLine();

        w.writeRaw("CSEG    ENDS");
        w.writeLine("END strt");
    }

    private void writePrintReal(AsmWriter w) {
        w.writeComment("PRINT_REAL: imprime EAX como ponto fixo com 4 casas decimais");
        w.writeRaw("PRINT_REAL PROC");
        w.writeLine("PUSH EBX");
        w.writeLine("PUSH EDX");
        w.writeLine("CMP EAX, 0");
        w.writeLine("JGE PR_REAL_POS");
        w.writeLine("PUSH EAX");
        w.writeLine("MOV DL, '-'");
        w.writeLine("MOV AH, 02h");
        w.writeLine("INT 21h");
        w.writeLine("POP EAX");
        w.writeLine("NEG EAX");
        w.writeRaw("PR_REAL_POS:");
        w.writeLine("XOR EDX, EDX");
        w.writeLine("MOV EBX, " + REAL_SCALE);
        w.writeLine("DIV EBX");
        w.writeLine("PUSH EDX");
        w.writeLine("CALL PRINT_INT");
        w.writeLine("MOV DL, '.'");
        w.writeLine("MOV AH, 02h");
        w.writeLine("INT 21h");
        w.writeLine("POP EAX");
        writeRealDigit(w, 1000);
        writeRealDigit(w, 100);
        writeRealDigit(w, 10);
        writeRealDigit(w, 1);
        w.writeLine("POP EDX");
        w.writeLine("POP EBX");
        w.writeLine("RET");
        w.writeRaw("PRINT_REAL ENDP");
    }

    private void writeRealDigit(AsmWriter w, int divisor) {
        w.writeLine("XOR EDX, EDX");
        w.writeLine("MOV EBX, " + divisor);
        w.writeLine("DIV EBX");
        w.writeLine("ADD AL, '0'");
        w.writeLine("MOV DL, AL");
        w.writeLine("MOV AH, 02h");
        w.writeLine("INT 21h");
        w.writeLine("MOV EAX, EDX");
    }

    // -------------------------------------------------------------------------
    // Sub-rotinas embutidas no template
    // -------------------------------------------------------------------------

    private void writePrintInt(AsmWriter w) {
        w.writeComment("PRINT_INT: imprime EAX como decimal com sinal");
        w.writeRaw("PRINT_INT PROC");
        w.writeLine("PUSH EBX");
        w.writeLine("PUSH ECX");
        w.writeLine("PUSH EDX");
        w.writeLine("CMP EAX, 0");
        w.writeLine("JGE PRNT_POS");
        w.writeLine("PUSH EAX");
        w.writeLine("MOV DL, '-'");
        w.writeLine("MOV AH, 02h");
        w.writeLine("INT 21h");
        w.writeLine("POP EAX");
        w.writeLine("NEG EAX");
        w.writeRaw("PRNT_POS:");
        w.writeLine("MOV CX, 0");
        w.writeLine("MOV EBX, 10");
        w.writeRaw("PRNT_DIV:");
        w.writeLine("XOR EDX, EDX");
        w.writeLine("DIV EBX");
        w.writeLine("PUSH EDX");
        w.writeLine("INC CX");
        w.writeLine("CMP EAX, 0");
        w.writeLine("JNE PRNT_DIV");
        w.writeRaw("PRNT_OUT:");
        w.writeLine("POP EDX");
        w.writeLine("ADD DL, '0'");
        w.writeLine("MOV AH, 02h");
        w.writeLine("INT 21h");
        w.writeLine("LOOP PRNT_OUT");
        w.writeLine("POP EDX");
        w.writeLine("POP ECX");
        w.writeLine("POP EBX");
        w.writeLine("RET");
        w.writeRaw("PRINT_INT ENDP");
    }

    private void writeReadInt(AsmWriter w) {
        w.writeComment("READ_INT: le inteiro do teclado, retorna em EAX");
        w.writeRaw("READ_INT PROC");
        w.writeLine("PUSH EBX");
        w.writeLine("PUSH ECX");
        w.writeLine("PUSH EDX");
        w.writeLine("XOR EBX, EBX");
        w.writeRaw("READ_CHR:");
        w.writeLine("MOV AH, 01h");
        w.writeLine("INT 21h");
        w.writeLine("CMP AL, 0Dh");
        w.writeLine("JE  READ_END");
        w.writeLine("CMP AL, '0'");
        w.writeLine("JL  READ_END");
        w.writeLine("CMP AL, '9'");
        w.writeLine("JG  READ_END");
        w.writeLine("AND EAX, 0Fh");
        w.writeLine("IMUL EBX, EBX, 10");
        w.writeLine("ADD EBX, EAX");
        w.writeLine("JMP READ_CHR");
        w.writeRaw("READ_END:");
        w.writeLine("MOV EAX, EBX");
        w.writeLine("POP EDX");
        w.writeLine("POP ECX");
        w.writeLine("POP EBX");
        w.writeLine("RET");
        w.writeRaw("READ_INT ENDP");
    }

    private void writeReadStr(AsmWriter w) {
        w.writeComment("READ_STR: le string do teclado para buffer em DI, termina com '$'");
        w.writeRaw("READ_STR PROC");
        w.writeLine("PUSH AX");
        w.writeLine("PUSH DI");
        w.writeRaw("READ_S_LOOP:");
        w.writeLine("MOV AH, 01h");
        w.writeLine("INT 21h");
        w.writeLine("CMP AL, 0Dh");
        w.writeLine("JE  READ_S_END");
        w.writeLine("MOV [DI], AL");
        w.writeLine("INC DI");
        w.writeLine("JMP READ_S_LOOP");
        w.writeRaw("READ_S_END:");
        w.writeLine("MOV BYTE PTR [DI], '$'");
        w.writeLine("POP DI");
        w.writeLine("POP AX");
        w.writeLine("RET");
        w.writeRaw("READ_STR ENDP");
    }

    private void writeStrCpy(AsmWriter w) {
        w.writeComment("STRCPY_PROC: copia string de SI para DI ate '$' (inclusive)");
        w.writeRaw("STRCPY_PROC PROC");
        w.writeLine("PUSH AX");
        w.writeLine("PUSH SI");
        w.writeLine("PUSH DI");
        w.writeRaw("STRCPY_LOOP:");
        w.writeLine("MOV AL, [SI]");
        w.writeLine("MOV [DI], AL");
        w.writeLine("INC SI");
        w.writeLine("INC DI");
        w.writeLine("CMP AL, '$'");
        w.writeLine("JNE STRCPY_LOOP");
        w.writeLine("POP DI");
        w.writeLine("POP SI");
        w.writeLine("POP AX");
        w.writeLine("RET");
        w.writeRaw("STRCPY_PROC ENDP");
    }

    private void writeStrCat(AsmWriter w) {
        w.writeComment("STRCAT_PROC: concatena SI e DX em DI, terminando com '$'");
        w.writeRaw("STRCAT_PROC PROC");
        w.writeLine("PUSH AX");
        w.writeLine("PUSH SI");
        w.writeLine("PUSH DI");
        w.writeRaw("STRCAT_LEFT:");
        w.writeLine("MOV AL, [SI]");
        w.writeLine("CMP AL, '$'");
        w.writeLine("JE  STRCAT_RIGHT_PREP");
        w.writeLine("MOV [DI], AL");
        w.writeLine("INC SI");
        w.writeLine("INC DI");
        w.writeLine("JMP STRCAT_LEFT");
        w.writeRaw("STRCAT_RIGHT_PREP:");
        w.writeLine("MOV SI, DX");
        w.writeRaw("STRCAT_RIGHT:");
        w.writeLine("MOV AL, [SI]");
        w.writeLine("MOV [DI], AL");
        w.writeLine("INC SI");
        w.writeLine("INC DI");
        w.writeLine("CMP AL, '$'");
        w.writeLine("JNE STRCAT_RIGHT");
        w.writeLine("POP DI");
        w.writeLine("POP SI");
        w.writeLine("POP AX");
        w.writeLine("RET");
        w.writeRaw("STRCAT_PROC ENDP");
    }

    private void writeStrCmp(AsmWriter w) {
        w.writeComment("STRCMP_PROC: compara SI e DI; BL=FFh se iguais, BL=00h se diferentes");
        w.writeRaw("STRCMP_PROC PROC");
        w.writeLine("PUSH AX");
        w.writeLine("PUSH SI");
        w.writeLine("PUSH DI");
        w.writeRaw("STRCMP_LOOP:");
        w.writeLine("MOV AL, [SI]");
        w.writeLine("CMP AL, [DI]");
        w.writeLine("JNE STRCMP_FALSE");
        w.writeLine("CMP AL, '$'");
        w.writeLine("JE  STRCMP_TRUE");
        w.writeLine("INC SI");
        w.writeLine("INC DI");
        w.writeLine("JMP STRCMP_LOOP");
        w.writeRaw("STRCMP_TRUE:");
        w.writeLine("MOV BL, 0FFh");
        w.writeLine("JMP STRCMP_END");
        w.writeRaw("STRCMP_FALSE:");
        w.writeLine("MOV BL, 0");
        w.writeRaw("STRCMP_END:");
        w.writeLine("POP DI");
        w.writeLine("POP SI");
        w.writeLine("POP AX");
        w.writeLine("RET");
        w.writeRaw("STRCMP_PROC ENDP");
    }
}
