.386
; Compilador BRL -- gerado automaticamente

SSEG    SEGMENT STACK
        DB 256 DUP(?)
SSEG    ENDS

DSEG    SEGMENT DATA
        nome            DB 512 DUP(?)
        _STR0            DB "Rafael", '$'
        STR_VERDADEIRO  DB "verdadeiro$"
        STR_FALSO       DB "falso$"
DSEG    ENDS

CSEG    SEGMENT CODE
        ASSUME CS:CSEG, DS:DSEG, SS:SSEG

strt:
        MOV AX, DSEG
        MOV DS, AX

        LEA DX, _STR0
        MOV SI, DX
        LEA DI, [nome]
        CALL STRCPY_PROC

        MOV AH, 4Ch
        INT 21h

; PRINT_INT: imprime EAX como decimal com sinal
PRINT_INT PROC
        PUSH EBX
        PUSH ECX
        PUSH EDX
        CMP EAX, 0
        JGE PRNT_POS
        PUSH EAX
        MOV DL, '-'
        MOV AH, 02h
        INT 21h
        POP EAX
        NEG EAX
PRNT_POS:
        MOV CX, 0
        MOV EBX, 10
PRNT_DIV:
        XOR EDX, EDX
        DIV EBX
        PUSH EDX
        INC CX
        CMP EAX, 0
        JNE PRNT_DIV
PRNT_OUT:
        POP EDX
        ADD DL, '0'
        MOV AH, 02h
        INT 21h
        LOOP PRNT_OUT
        POP EDX
        POP ECX
        POP EBX
        RET
PRINT_INT ENDP

; PRINT_REAL: imprime EAX como ponto fixo com 4 casas decimais
PRINT_REAL PROC
        PUSH EBX
        PUSH EDX
        CMP EAX, 0
        JGE PR_REAL_POS
        PUSH EAX
        MOV DL, '-'
        MOV AH, 02h
        INT 21h
        POP EAX
        NEG EAX
PR_REAL_POS:
        XOR EDX, EDX
        MOV EBX, 10000
        DIV EBX
        PUSH EDX
        CALL PRINT_INT
        MOV DL, '.'
        MOV AH, 02h
        INT 21h
        POP EAX
        XOR EDX, EDX
        MOV EBX, 1000
        DIV EBX
        ADD AL, '0'
        MOV DL, AL
        MOV AH, 02h
        INT 21h
        MOV EAX, EDX
        XOR EDX, EDX
        MOV EBX, 100
        DIV EBX
        ADD AL, '0'
        MOV DL, AL
        MOV AH, 02h
        INT 21h
        MOV EAX, EDX
        XOR EDX, EDX
        MOV EBX, 10
        DIV EBX
        ADD AL, '0'
        MOV DL, AL
        MOV AH, 02h
        INT 21h
        MOV EAX, EDX
        XOR EDX, EDX
        MOV EBX, 1
        DIV EBX
        ADD AL, '0'
        MOV DL, AL
        MOV AH, 02h
        INT 21h
        MOV EAX, EDX
        POP EDX
        POP EBX
        RET
PRINT_REAL ENDP

; READ_INT: le inteiro do teclado, retorna em EAX
READ_INT PROC
        PUSH EBX
        PUSH ECX
        PUSH EDX
        XOR EBX, EBX
READ_CHR:
        MOV AH, 01h
        INT 21h
        CMP AL, 0Dh
        JE  READ_END
        CMP AL, '0'
        JL  READ_END
        CMP AL, '9'
        JG  READ_END
        AND EAX, 0Fh
        IMUL EBX, EBX, 10
        ADD EBX, EAX
        JMP READ_CHR
READ_END:
        MOV EAX, EBX
        POP EDX
        POP ECX
        POP EBX
        RET
READ_INT ENDP

; READ_STR: le string do teclado para buffer em DI, termina com '$'
READ_STR PROC
        PUSH AX
        PUSH DI
READ_S_LOOP:
        MOV AH, 01h
        INT 21h
        CMP AL, 0Dh
        JE  READ_S_END
        MOV [DI], AL
        INC DI
        JMP READ_S_LOOP
READ_S_END:
        MOV BYTE PTR [DI], '$'
        POP DI
        POP AX
        RET
READ_STR ENDP

; STRCPY_PROC: copia string de SI para DI ate '$' (inclusive)
STRCPY_PROC PROC
        PUSH AX
        PUSH SI
        PUSH DI
STRCPY_LOOP:
        MOV AL, [SI]
        MOV [DI], AL
        INC SI
        INC DI
        CMP AL, '$'
        JNE STRCPY_LOOP
        POP DI
        POP SI
        POP AX
        RET
STRCPY_PROC ENDP

; STRCAT_PROC: concatena SI e DX em DI, terminando com '$'
STRCAT_PROC PROC
        PUSH AX
        PUSH SI
        PUSH DI
STRCAT_LEFT:
        MOV AL, [SI]
        CMP AL, '$'
        JE  STRCAT_RIGHT_PREP
        MOV [DI], AL
        INC SI
        INC DI
        JMP STRCAT_LEFT
STRCAT_RIGHT_PREP:
        MOV SI, DX
STRCAT_RIGHT:
        MOV AL, [SI]
        MOV [DI], AL
        INC SI
        INC DI
        CMP AL, '$'
        JNE STRCAT_RIGHT
        POP DI
        POP SI
        POP AX
        RET
STRCAT_PROC ENDP

; STRCMP_PROC: compara SI e DI; BL=FFh se iguais, BL=00h se diferentes
STRCMP_PROC PROC
        PUSH AX
        PUSH SI
        PUSH DI
STRCMP_LOOP:
        MOV AL, [SI]
        CMP AL, [DI]
        JNE STRCMP_FALSE
        CMP AL, '$'
        JE  STRCMP_TRUE
        INC SI
        INC DI
        JMP STRCMP_LOOP
STRCMP_TRUE:
        MOV BL, 0FFh
        JMP STRCMP_END
STRCMP_FALSE:
        MOV BL, 0
STRCMP_END:
        POP DI
        POP SI
        POP AX
        RET
STRCMP_PROC ENDP

CSEG    ENDS
        END strt
