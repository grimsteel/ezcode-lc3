; CALCULATOR.ASM
;==========================================================
        .orig   x3000
        
        
; MAIN SUBROUTINE
;---------------------------------------------------------- 
MAIN    jsr TEST        ; Run the series of test cases
        
        
; SAMPLE ARITHMETIC SUBROUTINES (IMPRACTICAL TO USE)
;==========================================================

; NEGATION...
;  Precondition: r1 = x
; Postcondition: r0 = -x
;                Registers r1 through r7 remain unchanged
NEG     not r0, r1      ; 
        add r0, r0, #1  ; 
        ret             ; 
        
        
; ADDITION...
;  Precondition: r1 = x
;                r2 = y
; Postcondition: r0 = x + y
;                Registers r1 through r7 remain unchanged
PLUS    add r0, r1, r2  ; 
        ret             ; 
        
        
; BASIC ARITHMETIC SUBROUTINES
;==========================================================
        
; SUBTRACTION...
;  Precondition: r1 = x
;                r2 = y
; Postcondition: r0 = x - y
;                Registers r1 through r7 remain unchanged
SUB                     ; 
        not r0, r2
        add r0, r0, 1   ; r0 = -r2
        add r0, r0, r1  ; r0 = r1 - r2
        ret             ; 
        
        
; MULTIPLICATION...
;  Precondition: r1 = x
;                r2 = y
; Postcondition: r0 = x * y
;                Registers r1 through r7 remain unchanged
MULT                    ; 
        st r1, MULTr1
        
        and r0, r0, 0
        add r1, r1, 0
        brz MULTR       ; if r1 == 0: ret
        brp MULTL       ; if r1 < 0: r1 = -r1; r2 = -r2
        ; negate r2
        not r2, r2      ; r2 = -r2
        add r2, r2, 1
        ; negate r1
        not r1, r1      ; r1 = -r1
        add r1, r1, 1
MULTL   brnz MULTD      ; while (r1 > 0) {
        add r0, r0, r2  ;   r0 += r2
        add r1, r1, #-1 ;   r1--
        br MULTL        ; }
MULTD   ld r1, MULTr1   ; restore r1
        brzp MULTR      ; if MULTr1 < 0; r2 = -r2
        ; negate r2 to restore
        not r2, r2
        add r2, r2, 1
MULTR   ret             ; 
        
; DATA FOR THE MULTIPLY SUBROUTINE... 
MULTr1  .fill #0        ; For saving r1 in MULT
        
        
; DIVISION...
;  Precondition: r1 = x
;                r2 = y
; Postcondition: If y != 0, then r0 = x / y
;                If y == 0, then prints an error message and sets r0 = 0
;                Registers r1 through r7 remain unchanged
DIV                     ; 
        and r0, r0, 0
        st r1, DIVr1
        st r2, DIVr2
        st r3, DIVr3
        
        and r3, r3, 0
        
        
        add r2, r2, 0
        brz DIVERR
        brp DIVN
        ; negate r1 and r2
        not r1, r1
        add r1, r1, 1
        not r2, r2
        add r2, r2, 1
        
DIVN    add r1, r1, 0
        brzp DIVLS
        
        not r1, r1
        add r1, r1, 1
        add r3, r3, #-1 ; r3 = -1
        
        ; negate r2
DIVLS   not r2, r2
        add r2, r2, 1
        
DIVL    st r1, MODr0
        add r0, r0, #1
        add r1, r1, r2  ; r1 += r2 (but r2 negative)
        brzp DIVL
        
        add r0, r0, #-1
        
        add r3, r3, 0
        brzp DIVD
        
        ; negate answer
        not r0, r0
        add r0, r0, 1
        
DIVD    ld r1, DIVr1
        brzp DIVNEG
        ; negate mod
        ld r2, MODr0
        not r2, r2
        add r2, r2, 1
        st r2, MODr0
        
DIVNEG  ld r2, DIVr2
        ld r3, DIVr3
        ret             ; 
DIVERR  
        lea r0, DIVZERO ; print error message
        puts
        and r0, r0, 0   ; r0 = 0 
        st r0, MODr0
        ld r3, DIVr3
        ret
        
; DATA FOR THE DIVIDE SUBROUTINE...
DIVr1   .fill #0
DIVr2   .fill #0
DIVr3   .fill #0
DIVZERO .stringz "ERROR: Division by 0"
                        ; TODO: Add additional labels if necessary
        
        
; MODULUS...
;  Precondition: r1 = x
;                r2 = y
; Postcondition: If y != 0, then r0 = x % y
;                If y == 0, then prints an error message and sets r0 = 0
;                Registers r1 through r7 remain unchanged
MOD                     ; 
        st r7, MODr7
        jsr DIV
        ld r0, MODr0
        ld r7, MODr7
        ret             ; 
        
; DATA FOR THE MODULUS SUBROUTINE... 

MODr0   .fill #0
MODr7   .fill #0
        
        
; SUBROUTINES FOR PRINTING INTEGERS
;==========================================================
        
; PRINT A NUMBER...
;  Precondition: r0 = a 16-bit integer value 
; Postcondition: r0 is printed to the console in decimal
;                Registers r0 through r7 remain unchanged
PRINT   st r0, PRINTr0  ; Save r0 to memory
        st r1, PRINTr1
        st r2, PRINTr2
        st r3, PRINTr3
        st r7, PRINTr7  ; Save r7 to memory
        
        lea r3, PRINTNL
        add r3, r3, #-1
        
        add r1, r0, 0   ; r1 = r0
        brzp PRINTST
        
        not r1, r1
        add r1, r1, #1
        
        ld r0, DASH
        out
        
PRINTST and r2, r2, 0   ; r2 = 10
        add r2, r2, #10
        
PRINTLP jsr DIV
        
        ; load the remainder
        ld r1, MODr0
        jsr TOASCII
        str r1, r3, #0
        add r3, r3, #-1
        
        add r1, r0, 0   ; r1 = r0
        brp PRINTLP
        
        add r0, r3, #1  ; r0 = r3+1
        puts
                       ; 
        ld r0, NEWLINE  ; 
        out             ; print('\n')
        
        ld r0, PRINTr0  ; Reload r0 from memory
        ld r1, PRINTr1
        ld r2, PRINTr2
        ld r3, PRINTr3
        ld r7, PRINTr7  ; Reload r7 from memory
                        ; 
        ret 	        ; PC = r7 (return addr.)
        
; DATA FOR THE PRINT SUBROUTINE... 
PRINTr0 .blkw #1        ; For saving r0 in PRINT
PRINTr1 .blkw #1
PRINTr2 .blkw #1
PRINTr3 .blkw #1
PRINTr7 .blkw #1        ; For saving r7 in PRINT
PRINTBF .blkw #5 
PRINTNL .fill #0
NEWLINE .fill #10       ; Newline character
DASH    .fill x2D
        
        
; CONVERT A NUMERICAL VALUE TO AN ASCII DIGIT CHARACTER...
;  Precondition: r1 = a positive, single-digit integer value
; Postcondition: r1 = the ASCII character for the digit originally in r1
;                All other registers remain unchanged
TOASCII st r2, DIGITr1  ; DIGITr1 = r1 (saves r1 into memory)
        ld r2, ASCII0   ; r1 = '0'
        add r1, r2, r1  ; r0 = r0 + '0'
        ld r2, DIGITr1  ; r1 = DIGITr1 (reloads r1 from memory)
        ret             ; 
        
        
; DATA FOR THE TOASCII SUBROUTINE...
DIGITr1 .fill #0        ; Allocates space for saving r1 in TOASCII
                        ; 
ASCII0  .fill #48       ; ASCII Character '0'
        
        
; ADVANCED ARITHMETIC SUBROUTINES
;==========================================================
        
; EXPONENTIATION...
;  Precondition: r1 = x
;                r2 = y (where y >= 0)
; Postcondition: r0 = Math.pow(x,y)
;                Registers r1 through r7 remain unchanged
POW                     ; 
        st r7, POWr7
        st r3, POWr3
        st r2, POWr2
    
        add r3, r2, #0  ; r3 = r2
        
        and r0, r0, 0   ; r0 = 1
        add r0, r0, 1
        
POWLP   add r3, r3, #-1
        brn POWLPD
        add r2, r0, #0  ; r2 = r0
        jsr MULT         ; r0 = r2 * r1
        br POWLP
        
POWLPD  
        
        ld r2, POWr2
        ld r3, POWr3
        ld r7, POWr7
        ret             ;
        
; DATA FOR THE EXPONENTIATION SUBROUTINE...
POWr2 .fill #0
POWr7 .fill #0
POWr3 .fill #0
        
        
; FACTORIAL...
;  Precondition: r1 = x (where x >= 0)
; Postcondition: r0 = x!
;                Registers r1 through r7 remain unchanged
FACT                    ; 
        st r7, FACTr7
        st r1, FACTr1
        st r2, FACTr2
        
        and r0, r0, 0   ; r0 = 1
        add r0, r0, 1
        
        add r1, r1, #0  ; quit if r1 == 0 (0! = 1)
        brnz FACTLPD
        
FACTLP   add r2, r0, #0  ; r2 = r0
        jsr MULT         ; r0 = r2 * r1
        add r1, r1, #-1
        brp FACTLP
        
FACTLPD  
        
        ld r2, FACTr2
        ld r1, FACTr1
        ld r7, FACTr7
        ret             ;
        
; DATA FOR THE FACTORIAL SUBROUTINE...
FACTr2 .fill #0
FACTr1 .fill #0
FACTr7 .fill #0
        
        
; CUSTOM ROUTINE...Define an operation of your choice.
;  Precondition: ... insert your precondition here ...
; Postcondition: ... insert your postcondition here ...
                        ;
                        ; TODO: Design and implement this subroutine
                        ;
        
; DATA FOR YOUR CUSTOM SUBROUTINE...
                        ; TODO: Add additional labels if necessary
        
        
;==========================================================
; ******** DO NOT ALTER ANYTHING BELOW THIS POINT ********
;==========================================================
        
; SUBROUTINE FOR TESTING EACH ARITHMETIC OPERATION
;----------------------------------------------------------
TEST    lea r0, START   ; 
        puts            ; Output "START OF TESTS" message
        ;--------------------------------------------------
                        ; 
        lea r0, ADDTEST ; 
        jsr HEADER      ; Print PLUS header
                        ; 
        lea r0, CASEADD ; 
        st r0, CASEPTR  ; Initialize test case pointer
                        ; 
        ldr r1, r0, #0  ; 
        ldr r2, r0, #1  ; Load operands (r1 = x, r2 = y)
        jsr SAVEREG     ; --+
        jsr PLUS        ;   |
        jsr PRINT       ;   |+-- Run PLUS test
        jsr TESTREG     ; --+
                        ; 
        jsr PAUSE       ; Pause until a key is pressed
        ;--------------------------------------------------
                        ; 
        lea r0, SUBTEST ; 
        jsr HEADER      ; Print SUB header
                        ; 
        and r0, r0, #0  ; 
        add r0, r0, #4  ; 
        st r0, CASECT   ; Initialize loop counter for test cases
                        ; 
        lea r0, CASESUB ; 
        st r0, CASEPTR  ; Initialize test case pointer
                        ; 
SCASES  ld r0, CASEPTR  ; 
        ldr r1, r0, #0  ; 
        ldr r2, r0, #1  ; Load operands (r1 = x, r2 = y)
        jsr SAVEREG     ; --+
        jsr SUB         ;   |
        jsr PRINT       ;   |--+ Run SUB test
        jsr TESTREG     ; --+
                        ; 
        ld r0, CASEPTR  ; 
        add r0, r0, #3  ; 
        st r0, CASEPTR  ; Increment test case pointer to answer
                        ; 
        ld r0, CASECT   ; 
        add r0, r0, #-1 ; 
        st r0, CASECT   ; Decrement loop counter
        brp SCASES      ; Repeat loop
                        ; 
        jsr PAUSE       ; Pause until a key is pressed
        ;--------------------------------------------------
                        ; 
        lea r0, MULTEST ; 
        jsr HEADER      ; Print MULT header
                        ; 
        and r0, r0, #0  ; 
        add r0, r0, #7  ; 
        st r0, CASECT   ; Initialize loop counter for test cases
                        ; 
        lea r0, CASEMLT ; 
        st r0, CASEPTR  ; Initialize test case pointer
                        ; 
MCASES  ld r0, CASEPTR  ; 
        ldr r1, r0, #0  ; 
        ldr r2, r0, #1  ; Load operands (r1 = x, r2 = y)
        jsr SAVEREG     ; --+
        jsr MULT        ;   |
        jsr PRINT       ;   |--+ Run MULT test
        jsr TESTREG     ; --+
                        ; 
        ld r0, CASEPTR  ; 
        add r0, r0, #3  ; 
        st r0, CASEPTR  ; Increment pointer to next test case
                        ; 
        ld r0, CASECT   ; 
        add r0, r0, #-1 ; 
        st r0, CASECT   ; Decrement loop counter
        brp MCASES      ; Repeat loop
                        ; 
        jsr PAUSE       ; Pause until a key is pressed
        ;--------------------------------------------------
                        ; 
        lea r0, DIVTEST ; 
        jsr HEADER      ; Print MOD/DIV header
                        ; 
        and r0, r0, #0  ; 
        add r0, r0, #7  ; 
        st r0, CASECT   ; Initialize loop counter for test cases
                        ; 
        lea r0, CASEDIV ; 
        st r0, CASEPTR  ; Initialize test case pointer
                        ; 
DCASES  ld r0, CASEPTR  ; 
        ldr r1, r0, #0  ; 
        ldr r2, r0, #1  ; Load operands (r1 = x, r2 = y)
        jsr SAVEREG     ; --+
        jsr MOD         ;   |
        jsr PRINT       ;   |--+ Run MOD test
        jsr TESTREG     ; --+
                        ; 
        ld r0, CASEPTR  ; 
        ldr r1, r0, #0  ; 
        ldr r2, r0, #1  ; Reload operands (r1 = x, r2 = y)
        jsr SAVEREG     ; --+
        jsr DIV         ;   |--+ Run DIV test
        jsr PRINT       ; --+
                        ; 
        st r0, POSTr0   ; --+
        ld r0, CASEPTR  ;   |
        add r0, r0, #1  ;   |+-- Bump pointer by 1 to read DIV answer
        st r0, CASEPTR  ;   |
        ld r0, POSTr0   ; --+
                        ; 
        jsr TESTREG     ; Complete DIV postcondition test
                        ; 
        lea r0, BLANK   ; 
        jsr HEADER      ; Print blank line between test cases
                        ; 
        ld r0, CASEPTR  ; 
        add r0, r0, #3  ; 
        st r0, CASEPTR  ; Increment test case pointer to answer
                        ; 
        ld r0, CASECT   ; 
        add r0, r0, #-1 ; 
        st r0, CASECT   ; Decrement loop counter
        brp DCASES      ; Repeat loop
                        ; 
        jsr PAUSE       ; Pause until a key is pressed
        ;--------------------------------------------------
                        ; 
        lea r0, POWTEST ; 
        jsr HEADER      ; Print POW header
                        ; 
        lea r0, CASEPOW ; 
        st r0, CASEPTR  ; Initialize test case pointer
                        ; 
        ldr r1, r0, #0  ; 
        ldr r2, r0, #1  ; Load operands (r1 = x, r2 = y)
        jsr SAVEREG     ; --+
        jsr POW         ;   |
        jsr PRINT       ;   |--+ Run POW test
        jsr TESTREG     ; --+
                        ; 
        jsr PAUSE       ; Pause until a key is pressed
        ;--------------------------------------------------
                        ; 
        lea r0, FACTEST ; 
        jsr HEADER      ; Print FACT header
                        ; 
        lea r0, CASEFCT ; 
        st r0, CASEPTR  ; Initialize test case pointer
                        ; 
        ldr r1, r0, #0  ; 
        ldr r2, r0, #1  ; Load operands (r1 = x, r2 = y) 
        jsr SAVEREG     ; --+
        jsr FACT        ;   |--+ Run FACT test
        jsr PRINT       ; --+
                        ; 
        st r0, POSTr0   ; --+
        ld r0, CASEPTR  ;   |
        add r0, r0, #-1 ;   |--+ Bump pointer by -1 to read FACT answer
        st r0, CASEPTR  ;   |
        ld r0, POSTr0   ; --+
                        ;
        jsr TESTREG     ; Complete FACT postcondition test
                        ; 
        jsr PAUSE       ; Pause until a key is pressed
        ;--------------------------------------------------
                        ; 
        lea r0, END     ; 
        puts            ; Output "END OF TESTS" message
                        ; 
        and r7, r7, #0  ; 
        add r7, r7, #1  ; 
        not r7, r7      ; Load address of system status register (xFFFE)
        and r6, r6, #0  ; 
        str r6, r7, #0  ; Halt the processor
                        ;  
CASECT  .fill #0        ; Countdown for number of test cases remaining
        
        
; DATA FOR THE TEST SUBROUTINE (DO NOT ALTER)
;----------------------------------------------------------
START   .stringz "=========== START OF TESTS ===========\n"
BLANK   .stringz "\n"   ; 
ADDTEST .stringz "PLUS:\n"
SUBTEST .stringz "SUB:\n"
MULTEST .stringz "MULT:\n"
DIVTEST .stringz "MOD & DIV:\n"
POWTEST .stringz "POW:\n"
FACTEST .stringz "FACT:\n"
END     .stringz "============ END OF TESTS ============\n"
        
        
; DATA FOR TEST CASES
;----------------------------------------------------------
CASEPTR .fill #0        ; Test case pointer
                        ; 
CASEADD .fill #4        ; 
        .fill #5        ; 
        .fill #9        ; 4 + 5 = 9
                        ; 
CASESUB .fill #12       ; 
        .fill #4        ; 
        .fill #8        ; 12 - 4 = 8
                        ; 
        .fill #-8       ; 
        .fill #5        ; 
        .fill #-13      ; (-8) - 5 = -13
                        ; 
        .fill #10       ; 
        .fill #-6       ; 
        .fill #16       ; 10 - (-6) = 16
                        ; 
        .fill #-9       ; 
        .fill #-3       ; 
        .fill #-6       ; (-9) - (-3) = -6
                        ; 
CASEMLT .fill #8        ; 
        .fill #3        ; 
        .fill #24       ; 8 * 3 = 24
                        ; 
        .fill #4        ; 
        .fill #-3       ; 
        .fill #-12      ; 4 * (-3) = -12
                        ; 
        .fill #-2       ; 
        .fill #5        ; 
        .fill #-10      ; (-2) * 5 = -10
                        ; 
        .fill #-5       ; 
        .fill #-6       ; 
        .fill #30       ; (-5) * (-6) = 30
                        ; 
        .fill #0        ; 
        .fill #3        ; 
        .fill #0        ; 0 * 3 = 0
                        ; 
        .fill #7        ; 
        .fill #0        ; 
        .fill #0        ; 7 * 0 = 0
                        ; 
        .fill #0        ; 
        .fill #0        ; 
        .fill #0        ; 0 * 0 = 0
                        ; 
CASEDIV .fill #11       ; 
        .fill #4        ; 
        .fill #3        ; 11 % 4 = 3
        .fill #2        ; 11 / 4 = 2
                        ; 
        .fill #5        ; 
        .fill #8        ; 
        .fill #5        ; 5 % 8 = 5
        .fill #0        ; 5 / 8 = 0
                        ; 
        .fill #-12      ; 
        .fill #4        ; 
        .fill #0        ; (-12) % 4 = 0
        .fill #-3       ; (-12) / 4 = 3
                        ; 
        .fill #13       ; 
        .fill #-2       ; 
        .fill #1        ; 13 % (-2) = 1
        .fill #-6       ; 13 / (-2) = -6
                        ; 
        .fill #-9       ; 
        .fill #-4       ; 
        .fill #-1       ; (-9) % (-4) = -1
        .fill #2        ; (-9) / (-4) = 2
                        ; 
        .fill #11       ; 
        .fill #0        ; 
        .fill #0        ; 11 % 0 = ERROR
        .fill #0        ; 11 / 0 = ERROR
                        ; 
        .fill #0        ; 
        .fill #4        ; 
        .fill #0        ; 0 % 4 = 0
        .fill #0        ; 0 / 4 = 0
                        ; 
CASEPOW .fill #3        ; 
        .fill #5        ; 
        .fill #243      ; 3 ^ 5 = 243
                        ; 
CASEFCT .fill #7        ; 
        .fill #5040     ; 7! = 5040
        
        
; SUBROUTINE FOR SAVING REGISTER STATE (DO NOT ALTER)
;----------------------------------------------------------
SAVEREG st r1, POSTr1   ; --+
        st r2, POSTr2   ;   |
        st r3, POSTr3   ;   |+-- Save original states of r1-r6
        st r4, POSTr4   ;   |    (no point in saving/checking r7)
        st r5, POSTr5   ;   |
        st r6, POSTr6   ; --+
                        ;
        ld r0, QMARK    ; Preload r0 w/ '?'
                        ;
        ret             ; 
                        ;
POSTr0  .fill #0        ; --+
POSTr1  .fill #0        ;   |
POSTr2  .fill #0        ;   |
POSTr3  .fill #0        ;   |+-- For saving r0-r6 in TESTREG
POSTr4  .fill #0        ;   |
POSTr5  .fill #0        ;   |
POSTr6  .fill #0        ; --+

QMARK   .fill #15       ; '?' - '0' = 15
WARNREG  .fill #0       ; ASCII value of register that fails postcondition
        
        
; SUBROUTINE FOR TESTING REGISTER STATE (DO NOT ALTER)
;----------------------------------------------------------
TESTREG st r0, POSTr0   ; Save original value of r0 (cannot be handled in SAVEREG)
                        ; 
        ld r0, ASCII1   ; 
        st r0, WARNREG  ; Initialize "warning register" to '1'
                        ; 
        ld r0, POSTr1   ; --+
        not r0, r0      ;   |
        add r0, r0, #1  ;   |+-- if (r1 != POSTr1) { POSTERR }
        add r0, r0, r1  ;   |
        brnp POSTERR    ; --+
                        ; 
        ld r0, ASCII1   ; 
        add r0, r0, #1  ; Increment "warning register" to '2'
        st r0, WARNREG  ; --+
        ld r1, POSTr2   ;   |
        not r1, r1      ;   |
        add r1, r1, #1  ;   |+-- if (r2 != POSTr2) { POSTERR }
        add r1, r1, r2  ;   |
        brnp POSTERR    ; --+
                        ; 
        add r0, r0, #1  ; 
        st r0, WARNREG  ; Increment "warning register" to '3'
        ld r1, POSTr3   ; --+
        not r1, r1      ;   |
        add r1, r1, #1  ;   |+-- if (r3 != POSTr3) { POSTERR }
        add r1, r1, r3  ;   |
        brnp POSTERR    ; --+
                        ; 
        add r0, r0, #1  ; 
        st r0, WARNREG  ; Increment "warning register" to '4'
        ld r1, POSTr4   ; --+
        not r1, r1      ;   |
        add r1, r1, #1  ;   |+-- if (r4 != POSTr4) { POSTERR }
        add r1, r1, r4  ;   |
        brnp POSTERR    ; --+
                        ; 
        add r0, r0, #1  ; 
        st r0, WARNREG  ; Increment "warning register" to '5'
        ld r1, POSTr5   ; --+
        not r1, r1      ;   |
        add r1, r1, #1  ;   |+-- if (r5 != POSTr5) { POSTERR }
        add r1, r1, r5  ;   |
        brnp POSTERR    ; --+
                        ; 
        add r0, r0, #1  ; 
        st r0, WARNREG  ; Increment "warning register" to '6'
        ld r1, POSTr6   ; --+
        not r1, r1      ;   |
        add r1, r1, #1  ;   |+-- if (r6 != POSTr6) { POSTERR }
        add r1, r1, r6  ;   |
        brnp POSTERR    ; --+
                        ; 
        ld r2, POSTr0   ; Reload original value of r0
                        ; 
        ld r0, ASCII1   ; 
        add r0, r0, #-1 ; Set "warning register" to '0'
        st r0, WARNREG  ; --+
        ld r1, CASEPTR  ;   |
        ldr r1, r1, #2  ;   |
        not r1, r1      ;   |+-- if (r0 != POSTr0) { POSTERR }
        add r1, r1, #1  ;   |
        add r1, r2, r1  ;   |
        brnp POSTERR    ; --+
                        ; 
        ld r0, POSTr0   ; --+
        ld r1, POSTr1   ;   |+-- Reload r0-r2
        ld r2, POSTr2   ; --+
                        ;
TESTRET ret             ; 
        
        
; SUBROUTINE FOR PRINTING POSTCONDITION WARNING (DO NOT ALTER)
;----------------------------------------------------------
POSTERR lea r0, WARN1   ; 
        puts            ; Print first half of warning
        ld r0, WARNREG  ; 
        out             ; Print register that fails postcondition
        lea r0, WARN2   ; 
        puts            ; Print second half of warning
                        ; 
        br TESTRET      ;
                        ; 
ASCII1  .fill #49       ; '1' (for use in intializing warning register)
WARN1    .stringz " --WARNING: r"
WARN2    .stringz " doesn't meet postcondition\n"
        
        
; SUBROUTINE FOR PRINTING TEST HEADERS (DO NOT ALTER)
;----------------------------------------------------------
HEADER  st r7, HEADr7   ; 
        puts            ; Print a section header
        and r0, r0, #0  ; 
        add r0, r0, #15 ; Preload r0 with 15 (i.e., '?')
        ld r7, HEADr7   ; 
        ret             ; 
                        ; 
HEADr7  .fill #0        ; For saving r7 in HEADER
        
        
; SUBROUTINE FOR PAUSING (DO NOT ALTER)
;----------------------------------------------------------
PAUSE   st r0, PAUSEr0  ; PAUSEr0 = r0 (saves r0 into memory)
        st r7, PAUSEr7  ; PAUSEr7 = r7 (saves r7 into memory)
        lea r0, CONTMSG ; 
        puts            ; Output the prompt to continue
        getc            ; Wait for user response to prompt
        ld r7, PAUSEr7  ; r7 = PAUSEr7 (reloads r7 from memory)
        ld r0, PAUSEr0  ; r0 = PAUSEr0 (reloads r0 from memory)
        ret             ;  
                        ; 
PAUSEr0 .fill #0        ; Allocates space for saving r0 in PAUSE
PAUSEr7 .fill #0        ; Allocates space for saving r7 in PAUSE
CONTMSG .stringz "== PRESS [space] FOR THE NEXT TEST ==\n"
        
        .end