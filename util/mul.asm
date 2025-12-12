.ORIG	x3000

halt


;; multiply `alu0` by `alu1`
mult:
  st r1, aluret0            ; save r1, r2
  st r2, aluret1

  ld alu0, r1               ; load param 1
  st r0, alu0               ; save r0
  and r0, r0, 0             ; zero r0
  ld alu1, r2               ; load param 2
  brzp mult_neg_chk_done    ; if alu1 < 0 {
  not r1, r1
  add r1, r1, 1             ;   r1 = -r1
  not r2, r2
  add r2, r2, 1             ;   r2 = -r2
mult_neg_chk_done:          ; }
  st r3, alu1               ; save r3, r4
  st r4, alutmp0 

  and r3, r3, 0             ; r3 (shifter) = 1
  add r3, r3, 1
  
mult_loop:                  ; do {
  and r4, r3, r2            ;   r4 = r3 & r2
  brnp mult_add_done        ;   if ((r3 & r2) != 0) {
  add r0, r0, r1            ;     r0 += r1
mult_add_done:              ;   }
  add r1, r1, r1            ;   r1 << 1
  add r3, r3, r3            ;   r3 << 1 (shifter)
  brp mult_loop             ; } while (r3 > 0); (will loop until 1 in r3 hits final bit)

  ld r1, aluret0            ; restore r1, r2, r3, r4
  ld r2, aluret1
  ld r3, alu1
  ld r4, alutmp0
  st r0, aluret0            ; store result
  ld r0, alu0               ; restore r0
  ret

alu0: .blkw 1
alu1: .blkw 1
aluret0: .blkw 1
aluret1: .blkw 1
alutmp0: .blkw 1

; print number in `alu0`
printnum:
	ld r1, alu0
	; negate if negative (if upper word negative)
	brzp print_negate_done
		not r1, r1
    add r1, r1, 1
		; print negative sign
		ld r1, dash_ascii
		out
	print_negate_done:

	lea r2, digits
	
	; r3: digit number (3 because 32767)
	and r3, r3, 0
	add r3, r3, 3

	print_digit_loop:
		; printing loop prerequisites:
		; r2 points to the negative current expanded digit (with place value) we're trying to check
		; r0 is the number with the top digits removed
		; r3 is the non-expanded digit (0-9) that corresponds to r3
    ; r4 is *r2
    ldr r0, r2, 0
    add r0, r0, r1   ; attempt to subtract digit 
		brzp print_check_digit_correct
			; if negative (this digit is too big)

			; digit pointer ++
			add r2, r2, 1
			; decrement digit number
			add r3, r3, -1
			brp print_check_digit_done
				; r3 = 0: we went through all digits for this place value

				; print a 0
				ld r0, zero_ascii
				out
				lea r0, alu1

				; start back at 9 (r2 already points to 9 for the next place value)
				add r3, r3, 9
		br print_check_digit_done
		print_check_digit_correct:
			; this was the first digit to make it positive

      ; copy r0 ro r1
			add r1, r0, 0

			; print the digit
			ld r0, zero_ascii 
			add r0, r0, r6         ; r4 = '0' + r6
			out

			; skip the digit pointer to next digit (double because 32 bit)
			; if r6 is 3, we need to increment past 3xxx, 2xxx, and 1xxx to get to 9xx
			add r2, r2, r6
			add r2, r2, r6

			; start back at 9
			and r6, r6, 0
			add r6, r6, 9
		print_check_digit_done:
		ldr r0, r2, 0
	; r2 will point to the null terminator if we're at the end
	; loop if we haven't reached the end
	brnp print_digit_loop

	ret

digits:
.fill 0x8AD0 ; -3 EE 4
.fill 0xB1E0 ; -2 EE 4
.fill 0xD8F0 ; -1 EE 4
.fill 0xDCD8 ; -9 EE 3
.fill 0xE0C0 ; -8 EE 3
.fill 0xE4A8 ; -7 EE 3
.fill 0xE890 ; -6 EE 3
.fill 0xEC78 ; -5 EE 3
.fill 0xF060 ; -4 EE 3
.fill 0xF448 ; -3 EE 3
.fill 0xF830 ; -2 EE 3
.fill 0xFC18 ; -1 EE 3
.fill 0xFC7C ; -9 EE 2
.fill 0xFCE0 ; -8 EE 2
.fill 0xFD44 ; -7 EE 2
.fill 0xFDA8 ; -6 EE 2
.fill 0xFE0C ; -5 EE 2
.fill 0xFE70 ; -4 EE 2
.fill 0xFED4 ; -3 EE 2
.fill 0xFF38 ; -2 EE 2
.fill 0xFF9C ; -1 EE 2
.fill 0xFFA6 ; -9 EE 1
.fill 0xFFB0 ; -8 EE 1
.fill 0xFFBA ; -7 EE 1
.fill 0xFFC4 ; -6 EE 1
.fill 0xFFCE ; -5 EE 1
.fill 0xFFD8 ; -4 EE 1
.fill 0xFFE2 ; -3 EE 1
.fill 0xFFEC ; -2 EE 1
.fill 0xFFF6 ; -1 EE 1
.fill 0xFFF7 ; -9 EE 0
.fill 0xFFF8 ; -8 EE 0
.fill 0xFFF9 ; -7 EE 0
.fill 0xFFFA ; -6 EE 0
.fill 0xFFFB ; -5 EE 0
.fill 0xFFFC ; -4 EE 0
.fill 0xFFFD ; -3 EE 0
.fill 0xFFFE ; -2 EE 0
.fill 0xFFFF ; -1 EE 0
.fill 0x0000 ; terminator

.END
