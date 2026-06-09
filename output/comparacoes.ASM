; Compilador BRL -- gerado automaticamente

SSEG    SEGMENT STACK
        DB 256 DUP(?)
SSEG    ENDS

DSEG    SEGMENT DATA
        a               DD ?
        b               DB ?
        STR_VERDADEIRO  DB "verdadeiro$"
        STR_FALSO       DB "falso$"
DSEG    ENDS

CSEG    SEGMENT CODE
        ASSUME CS:CSEG, DS:DSEG, SS:SSEG

strt:
        MOV AX, DSEG
        MOV DS, AX

        MOV AX, WORD PTR [a]
        PUSH AX
        MOV AX, 10
        MOV BX, AX
        POP AX
        CMP AX, BX
        JE  L0
        MOV AX, 0
        JMP L1
L0:
        MOV AX, 0FFh
L1:
        MOV BYTE PTR [b], AL
        MOV AX, WORD PTR [a]
        PUSH AX
        MOV AX, 5
        MOV BX, AX
        POP AX
        CMP AX, BX
        JNE L2
        MOV AX, 0
        JMP L3
L2:
        MOV AX, 0FFh
L3:
        MOV BYTE PTR [b], AL
        MOV AX, WORD PTR [a]
        PUSH AX
        MOV AX, 3
        MOV BX, AX
        POP AX
        CMP AX, BX
        JL  L4
        MOV AX, 0
        JMP L5
L4:
        MOV AX, 0FFh
L5:
        MOV BYTE PTR [b], AL
        MOV AX, WORD PTR [a]
        PUSH AX
        MOV AX, 3
        MOV BX, AX
        POP AX
        CMP AX, BX
        JG  L6
        MOV AX, 0
        JMP L7
L6:
        MOV AX, 0FFh
L7:
        MOV BYTE PTR [b], AL
        MOV AX, WORD PTR [a]
        PUSH AX
        MOV AX, 3
        MOV BX, AX
        POP AX
        CMP AX, BX
        JLE L8
        MOV AX, 0
        JMP L9
L8:
        MOV AX, 0FFh
L9:
        MOV BYTE PTR [b], AL
        MOV AX, WORD PTR [a]
        PUSH AX
        MOV AX, 3
        MOV BX, AX
        POP AX
        CMP AX, BX
        JGE L10
        MOV AX, 0
        JMP L11
L10:
        MOV AX, 0FFh
L11:
        MOV BYTE PTR [b], AL

        MOV AH, 4Ch
        INT 21h

; PRINT_INT: imprime AX como decimal com sinal
PRINT_INT PROC
        PUSH BX
        PUSH CX
        PUSH DX
        CMP AX, 0
        JGE PRNT_POS
        PUSH AX
        MOV DL, '-'
        MOV AH, 02h
        INT 21h
        POP AX
        NEG AX
PRNT_POS:
        MOV CX, 0
        MOV BX, 10
PRNT_DIV:
        MOV DX, 0
        DIV BX
        PUSH DX
        INC CX
        CMP AX, 0
        JNE PRNT_DIV
PRNT_OUT:
        POP DX
        ADD DL, '0'
        MOV AH, 02h
        INT 21h
        LOOP PRNT_OUT
        POP DX
        POP CX
        POP BX
        RET
PRINT_INT ENDP

; READ_INT: le inteiro do teclado, retorna em AX
READ_INT PROC
        PUSH BX
        PUSH CX
        PUSH DX
        MOV BX, 0
READ_CHR:
        MOV AH, 01h
        INT 21h
        CMP AL, 0Dh
        JE  READ_END
        CMP AL, '0'
        JL  READ_END
        CMP AL, '9'
        JG  READ_END
        AND AX, 00FFh
        PUSH AX
        MOV AX, BX
        MOV CX, 10
        MUL CX
        POP CX
        ADD AX, CX
        MOV BX, AX
        JMP READ_CHR
READ_END:
        MOV AX, BX
        POP DX
        POP CX
        POP BX
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

CSEG    ENDS
        END strt
