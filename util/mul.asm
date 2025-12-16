.ORIG	x3000
;; initialize stack
ld r0, b
ld r1, a
lea r6, stack               ; push operands onto stack
str r0, r6, #0
str r1, r6, #1
add r6, r6, #2
jsr div
add r6, r6, #-1
jsr iprint
halt
a: .fill -4
b: .fill 5
stack: .blkw #16

;; flag instruction so our assembler knows the real code starts here
ezcode_lc3_start_assemble: halt

;; MULT: multiply stack[r6 - 1] by stack [r6 - 2]
;; Algorithm: bitwise place-value add/shift
mul:  st r0, alu_tmp_0          ; save registers
  st r1, alu_tmp_1
  st r2, alu_tmp_2
  st r3, alu_tmp_3
  st r4, alu_tmp_4
  
  ldr r1, r6, #-1           ; load param 1
  and r0, r0, 0             ; zero r0
  ldr r2, r6, #-2           ; load param 2
  brzp mult_neg_chk_done    ; if alu1 < 0 {
  not r1, r1
  add r1, r1, 1             ;   r1 = -r1
  not r2, r2
  add r2, r2, 1             ;   r2 = -r2
mult_neg_chk_done:          ; }
  and r3, r3, 0             ; r3 (shifter) = 1
  add r3, r3, 1
  
mult_loop:                  ; do {
  and r4, r3, r2            ;   r4 = r3 & r2
  brz mult_add_done         ;   if ((r3 & r2) != 0) {
  add r0, r0, r1            ;     r0 += r1
mult_add_done:              ;   }
  add r1, r1, r1            ;   r1 << 1
  add r3, r3, r3            ;   r3 << 1 (shifter)
  brp mult_loop             ; } while (r3 > 0); (will loop until 1 in r3 hits final bit)

  add r6, r6, #-1           ; push stack 
  str r0, r6, #-1
  ld r0, alu_tmp_0          ; restore registers
  ld r1, alu_tmp_1
  ld r2, alu_tmp_2
  ld r3, alu_tmp_3
  ld r4, alu_tmp_4
  ret
 
;; DIV: divide stack[r6 - 2] by stack[r6 - 1]
;; Algorithm: ts pmo
div:  st r0, alu_tmp_0          ; save registers
  st r1, alu_tmp_1
  st r2, alu_tmp_2
  st r3, alu_tmp_3
  st r4, alu_tmp_4
  
  and r0, r0, 0             ; r0 stores negate flag
  ;; Operand Negation
  ldr r1, r6, #-2           ; load divisor from stack
  brzp div_neg1_done
  not r1, r1                ; r1 = -r1
  add r1, r1, 1
  not r0, r0                ; toggle negate flag
div_neg1_done:
  ldr r2, r6, #-1           ; load dividend from stack
  brp div_neg2_done
  brz div_err
  not r2, r2                ; r2 = -r2
  add r2, r2, 1
  not r0, r0                ; toggle negate flag
div_neg2_done:
  ;; Divisor Shift Calculation
  lea r3, div_buf_end       ; after `div_buf`
  and r4, r4, 0
  add r4, r4 #15            ; while (r4 > 0)
div_shift_loop:
  add r3, r3, #-1           ; r3--
  str r2, r3, #0            ; mem[r3] = r2
  add r2, r2, r2            ; r2 << 1
  brnz div_shift_loop_done
  add r4, r4, #-1           ; r4--
  brp div_shift_loop
div_shift_loop_done:
  ;; Division Loop
  and r4, r4, 0             ; build result in r4
div_loop:
  ldr r2, r3, 0             ; r2 = *r3  | r2 = (r1 - *r3) (try subtractionn)
  brz div_loop_done         ; if we went past end, stop (null terminator)
  add r4, r4, r4            ; r0 << 1   |
  not r2, r2                ; r2 = -r2  |
  add r2, r2, 1             ;           |
  add r2, r2, r1            ; r2 += r1  |
  brn div_bit_done          ; if result positive...
  add r4, r4, 1             ; set bit 1
  add r1, r2, 0             ; apply subtraction
div_bit_done:
  add r3, r3, 1             ; r3++
  br div_loop
div_loop_done:
  ;; Quotient Negation
  add r0, r0, 0
  brzp div_neg_done:        ; if should negate
  not r4, r4                ; negate result
  add r4, r4, #1
div_neg_done:
  str r1, r6, #-1           ; push quotient and remainder to stack
  str r4, r6, #-2
  ld r0, alu_tmp_0          ; restore registers
  ld r1, alu_tmp_1
  ld r2, alu_tmp_2
  ld r3, alu_tmp_3
  ld r4, alu_tmp_4
  ret
  
div_err:
  lea r0, div_err_msg
  puts
  halt

;; BPRINT: print boolean value
bprint: add r6, r6, #-1             ; pop stack
  lea r0, false
	ldr r1, r6, #0
	brz bprint_print
	lea r0, true
bprint_print:
  puts
  ret
	

;; IPRINT: print signed base-10 number
;; Algorithm:
;; Starting from the highest place value, subtracts
;; digits until the result is non negative.
;; Then, prints this digit and moves on to the next
;; place value.
iprint: add r6, r6, #-1             ; pop stack
	ldr r1, r6, #0
	
	add r4, r1, #0              ; if r4 == 0, we should print 0s
	brz print_zero
	
	;; Operand Negation
	brp print_negate_done      ; if `alu0` < 0
	not r1, r1                  ;   alu0 = -alu0
    add r1, r1, 1
	ld r0, dash_ascii           ;   print negative sign
	out
	print_negate_done:
	
	lea r2, digits              ; r2 is pointer to place value'd digit
	ldr r0, r2, 0               ; r0 is *r2
	and r3, r3, 0               ; r3 stores current digit (3 bc 32767)
	add r3, r3, 3
    ;; Digit Loop
	print_digit_loop:
    add r0, r0, r1              ; subtract digit from r0
	brzp print_digit_correct    ; if negative (this digit is too big)
	add r2, r2, 1               ;   try next digit (smaller)
	add r3, r3, -1
	brp print_digit_done        ;   if r3 == 0 (we tried all digits from 9-1)
	add r4, r4, 0               ;   don't print 0 if r4 != 0
	brnp print_zero_done
	ld r0, zero_ascii           ;   print  0
	out
	print_zero_done:
	add r3, r3, 9               ;   start back at 9 (r2 already points to 9)
	br print_digit_done         ; else
	print_digit_correct:
	add r1, r0, 0               ; apply subtraction
	ld r0, zero_ascii           ; print digit (stored in r3)
	add r0, r0, r3         
	out
	and r4, r4, 0               ; r4 = 0: enable printing 0s
	add r2, r2, r3              ; skip digit ptr to next place value 9
	and r3, r3, 0               ; r3 = 0
	add r3, r3, 9
	print_digit_done:
	ldr r0, r2, 0               ; r0 = *r2
	brnp print_digit_loop       ; r0 == 0 if we reached end

	ret
;; printing just a 0 doesn't work well with the leading zero code
print_zero:
    ld r0, zero_ascii           ;   print  0
	out
	ret
; char literals
zero_ascii:
.fill x30
dash_ascii:
.fill x2D
; alu storage
alu_tmp_0
.fill #0
alu_tmp_1
.fill #0
alu_tmp_2
.fill #0
alu_tmp_3
.fill #0
alu_tmp_4
.fill #0
div_buf:
.blkw #16
div_buf_end:
.fill #0
div_err_msg:
.stringz "Division by 0\n"
true:
.stringz "true"
false:
.stringz "false"
digits:
.fill x8AD0 ; -3 EE 4
.fill xB1E0 ; -2 EE 4
.fill xD8F0 ; -1 EE 4
.fill xDCD8 ; -9 EE 3
.fill xE0C0 ; -8 EE 3
.fill xE4A8 ; -7 EE 3
.fill xE890 ; -6 EE 3
.fill xEC78 ; -5 EE 3
.fill xF060 ; -4 EE 3
.fill xF448 ; -3 EE 3
.fill xF830 ; -2 EE 3
.fill xFC18 ; -1 EE 3
.fill xFC7C ; -9 EE 2
.fill xFCE0 ; -8 EE 2
.fill xFD44 ; -7 EE 2
.fill xFDA8 ; -6 EE 2
.fill xFE0C ; -5 EE 2
.fill xFE70 ; -4 EE 2
.fill xFED4 ; -3 EE 2
.fill xFF38 ; -2 EE 2
.fill xFF9C ; -1 EE 2
.fill xFFA6 ; -9 EE 1
.fill xFFB0 ; -8 EE 1
.fill xFFBA ; -7 EE 1
.fill xFFC4 ; -6 EE 1
.fill xFFCE ; -5 EE 1
.fill xFFD8 ; -4 EE 1
.fill xFFE2 ; -3 EE 1
.fill xFFEC ; -2 EE 1
.fill xFFF6 ; -1 EE 1
.fill xFFF7 ; -9 EE 0
.fill xFFF8 ; -8 EE 0
.fill xFFF9 ; -7 EE 0
.fill xFFFA ; -6 EE 0
.fill xFFFB ; -5 EE 0
.fill xFFFC ; -4 EE 0
.fill xFFFD ; -3 EE 0
.fill xFFFE ; -2 EE 0
.fill xFFFF ; -1 EE 0
.fill x0000 ; terminator

.END
