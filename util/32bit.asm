.orig x3000

;; PROGRAM CODE

lea r0, num1
lea r1, num1
;jsr neg32
jsr print32

and r0, r0, 0
add r0, r0 10
out


halt

;; SUBROUTINES

; get a 32-bit value as input
; out address = r1
input32:
	st r7, stack1

	; zero out *r0
	and r2, r2, 0
	str r2, r1, 0
	str r2, r1, 1

	; r2 = 10
	add r2, r2, 10
getc_loop:
	getc

	; if c == '\n'
	add r3, r0, -10
	brz getc_loop_end

	ld r3, neg_zero
	add r3, r3, r0
	; invalid - too low
	brn getc_loop
	add r4, r3, -10
	; invalid - too high
	brzp getc_loop

	; echo number
	out

	jsr mul
	; r1 = r3
	add r1, r3, 0
	brnzp getc_loop

getc_loop_end:
	; print newline
	out

	ld r7, stack1

; print a 32-bit signed integer
; precondition: r1 = in address
; uses r0-r6
print32:
	st r7, stack2

	lea r0, alu0

	; copy into alu memory
	ldr r2, r1, 0
	str r2, r0, 0
	ldr r2, r1, 1
	str r2, r0, 1

	; negate if negative (if upper word negative)
	brzp print_negate_done
		jsr neg32
		; print negative sign
		ld r0, dash_ascii
		out
	print_negate_done:

	; positive word now stored in alu0
	
	; alu1 = alu0 + digit
	lea r0, alu1
	lea r1, alu0
	lea r2, digits
	
	; r6: digit number
	and r6, r6, 0
	add r6, r6, 2

	print_digit_loop:
		; printing loop prerequisites:
		; r2 points to the negative current expanded digit (with place value) we're trying to check
		; alu0 is the number. the top digits have already been removed
		; r6 is the non-expanded digit (0-9) that corresponds to r2

		; subtract the current digit
		; alu1 now stores the number with the digit subtracted
		jsr add32

		; load upper byte of the result into r3 (gives us sign info)
		ldr r3, r0, 1
		brzp print_check_digit_correct
			; if negative (this digit is too big)

			; digits += 2 (increment expanded digit pointer, by 2 because 32 bit)
			add r2, r2, 2
			; decrement digit number
			add r6, r6, -1
			brp print_check_digit_done
				; r6 = 0: we went through all digits for this place value

				; print a 0
				ld r0, zero_ascii
				out
				lea r0, alu1

				; start back at 9 (r2 already points to 9 for the next place value)
				add r6, r6, 9
		br print_check_digit_done
		print_check_digit_correct:
			; this was the first digit to make it positive

			; print the digit
			ld r0, zero_ascii 
			add r0, r0, r6         ; r0 = '0' + r6
			out
			lea r0, alu1

			; copy alu1 (has subtracted digit) to alu0 (minuend), to actually subtract the digit
			ldr r3, r0, 0
			str r3, r1, 0
			ldr r3, r0, 1
			str r3, r1, 1

			; skip the digit pointer to next digit (double because 32 bit)
			; if r6 is 3, we need to increment past 3xxx, 2xxx, and 1xxx to get to 9xx
			add r2, r2, r6
			add r2, r2, r6

			; start back at 9
			and r6, r6, 0
			add r6, r6, 9
		print_check_digit_done:
		ldr r3, r2, 0
	; r2 will point to the null terminator if we're at the end
	; loop if we haven't reached the end
	brnp print_digit_loop

	; reload stack and return
	ld r7, stack2
	ret

; 32-bit place negate
; precondition: r0 = out address
; precondition: r1 = in address
; uses r0-r2
neg32:
	; save return
	st r7, stack3

	; invert lower word
	ldr r2, r1, 0
	not r2, r2
	str r2, r0, 0

	; invert upper word
	ldr r2, r1, 1
	not r2, r2
	str r2, r0, 1

	; add 1 (r0 = r0 + 1)
	add r1, r0, 0
	lea r2, pos1
	jsr add32

	ld r7, stack3
	ret

; 32-bit add
; r0, r1, r2 refer to memory addresses of two words (may overlap)
; precondition: r0 = out address
; precondition: r1 = in address 1
; precondition: r2 = in address 2
; uses r0-r5
add32:
	; initialize carry register
	and r5, r5, 0
	add r5, r5, 0b01

	; load lower bits
	ldr r3, r1, 0

	; CARRY FLAG ALGORITHM
	; the lc3 doesn't have a carry flag so we calc it using MSB
	; technically the lower words cannot be "negative", but we use the negative CC flag to determine the MSB
	; if both lower words are "negative" (MSB set), will always carry because MSB + MSB causes a carry to the next word
	; if only one has MSB set, it didn't carry if result still has MSB set.
	; it did carry if the result no longer has MSB set
	; r5 - carry register. LSB is AND of both MSBs
	;                      bit 1 is a sort of XOR (if we add 2 exactly once, it'll be 1)

	brn load_par1_neg		; if (r1[0] >= 0 (MSB not set)) {
		and r5, r5, 0b10 	;   set lower bit of r5 to 0
	br load_par1_done		; } else {
	load_par1_neg:
		add r5, r5, 0b10	;   r5 += 2
	load_par1_done:			; }

	ldr r4, r2, 0

	; same algorithm as above
	brn load_par2_neg
		and r5, r5, 0b10	; clear lower bit
	br load_par2_done
	load_par2_neg:
		add r5, r5, 0b10	; add 2
	load_par2_done:

	; add lower bits
	add r3, r3, r4

	; if the result was still negative it didn't carry
	brzp check_result_neg_done
		and r5, r5, 0b01		; clear bit 1
	check_result_neg_done:

	; clear other bits
	and r5, r5, 0b11

	str r3, r0, 0

	; load upper bits
	ldr r3, r1, 1
	ldr r4, r2, 1

	; add upper bits
	add r3, r3, r4

	; add carry if r5 > 0
	add r5, r5, 0
	brz add_carry_done
		add r3, r3, 1
	add_carry_done:

	; store upper bits
	str r3, r0, 1

	ret

; 16x32 bit multiply
; precondition: r0 = out address
; precondition: r1 = 32 bit in address
; precondition: r2 = 16-bit operand
halfmul32:
	st r7, stack2

	; alu0 = r1
	ldr r3, r1, 0
	ldr r4, r1, 1
	lea r1, alu0
	str r3, r1, 0
	str r4, r1, 1

	; r6 = r2
	add r6, r2, 0
	brzp halfmul_negate_done ; if r2 < 0, negate r1
		add r3, r0, 0 ; save r0
		lea r0, alu0
		jsr neg32
		add r0, r3, 0 ; restore
	halfmul_negate_done:

	; zero out *r0
	and r3, r3, 0
	str r3, r0, 0
	str r3, r0, 1

	; r2 = r0 (r0 = r1 + r2 = r0 += r1)
	add r2, r0, 0

	; initial neg/zero check
	add r6, r6, 0
	brnz halfmul_loop_end
	halfmul_loop:
		jsr add32
		add r6, r6, -1
		brp halfmul_loop
	halfmul_loop_end:

	; restore r2
	add r2, r6, 0

	ld r7, stack2
	ret

;; STORAGE

; i32 variable storage
num0: .blkw 2
num1: .blkw 2
num2: .blkw 2
num3: .blkw 2

; alu temp storage
alu0: .blkw 2
alu1: .blkw 2
alu2: .blkw 2

; call stack
stack0: .blkw 1
stack1: .blkw 1
stack2: .blkw 1
stack3: .blkw 1

;; CONSTANTS

; i32 literals
pos1:
	.fill 0x0001
    .fill 0x0000
zero:
	.fill 0x0000
	.fill 0x0000
neg1:
	.fill 0xFFFF
	.fill 0xFFFF

; char literals
zero_ascii: .fill 0x30
dash_ascii: .fill 0x2D

; digit literals for printing (script generated)
; lower word first
digits:
  .fill 0x6C00 ; -2 EE 9
  .fill 0x88CA
  .fill 0x3600 ; -1 EE 9
  .fill 0xC465
  .fill 0x1700 ; -9 EE 8
  .fill 0xCA5B
  .fill 0xF800 ; -8 EE 8
  .fill 0xD050
  .fill 0xD900 ; -7 EE 8
  .fill 0xD646
  .fill 0xBA00 ; -6 EE 8
  .fill 0xDC3C
  .fill 0x9B00 ; -5 EE 8
  .fill 0xE232
  .fill 0x7C00 ; -4 EE 8
  .fill 0xE828
  .fill 0x5D00 ; -3 EE 8
  .fill 0xEE1E
  .fill 0x3E00 ; -2 EE 8
  .fill 0xF414
  .fill 0x1F00 ; -1 EE 8
  .fill 0xFA0A
  .fill 0xB580 ; -9 EE 7
  .fill 0xFAA2
  .fill 0x4C00 ; -8 EE 7
  .fill 0xFB3B
  .fill 0xE280 ; -7 EE 7
  .fill 0xFBD3
  .fill 0x7900 ; -6 EE 7
  .fill 0xFC6C
  .fill 0x0F80 ; -5 EE 7
  .fill 0xFD05
  .fill 0xA600 ; -4 EE 7
  .fill 0xFD9D
  .fill 0x3C80 ; -3 EE 7
  .fill 0xFE36
  .fill 0xD300 ; -2 EE 7
  .fill 0xFECE
  .fill 0x6980 ; -1 EE 7
  .fill 0xFF67
  .fill 0xABC0 ; -9 EE 6
  .fill 0xFF76
  .fill 0xEE00 ; -8 EE 6
  .fill 0xFF85
  .fill 0x3040 ; -7 EE 6
  .fill 0xFF95
  .fill 0x7280 ; -6 EE 6
  .fill 0xFFA4
  .fill 0xB4C0 ; -5 EE 6
  .fill 0xFFB3
  .fill 0xF700 ; -4 EE 6
  .fill 0xFFC2
  .fill 0x3940 ; -3 EE 6
  .fill 0xFFD2
  .fill 0x7B80 ; -2 EE 6
  .fill 0xFFE1
  .fill 0xBDC0 ; -1 EE 6
  .fill 0xFFF0
  .fill 0x4460 ; -9 EE 5
  .fill 0xFFF2
  .fill 0xCB00 ; -8 EE 5
  .fill 0xFFF3
  .fill 0x51A0 ; -7 EE 5
  .fill 0xFFF5
  .fill 0xD840 ; -6 EE 5
  .fill 0xFFF6
  .fill 0x5EE0 ; -5 EE 5
  .fill 0xFFF8
  .fill 0xE580 ; -4 EE 5
  .fill 0xFFF9
  .fill 0x6C20 ; -3 EE 5
  .fill 0xFFFB
  .fill 0xF2C0 ; -2 EE 5
  .fill 0xFFFC
  .fill 0x7960 ; -1 EE 5
  .fill 0xFFFE
  .fill 0xA070 ; -9 EE 4
  .fill 0xFFFE
  .fill 0xC780 ; -8 EE 4
  .fill 0xFFFE
  .fill 0xEE90 ; -7 EE 4
  .fill 0xFFFE
  .fill 0x15A0 ; -6 EE 4
  .fill 0xFFFF
  .fill 0x3CB0 ; -5 EE 4
  .fill 0xFFFF
  .fill 0x63C0 ; -4 EE 4
  .fill 0xFFFF
  .fill 0x8AD0 ; -3 EE 4
  .fill 0xFFFF
  .fill 0xB1E0 ; -2 EE 4
  .fill 0xFFFF
  .fill 0xD8F0 ; -1 EE 4
  .fill 0xFFFF
  .fill 0xDCD8 ; -9 EE 3
  .fill 0xFFFF
  .fill 0xE0C0 ; -8 EE 3
  .fill 0xFFFF
  .fill 0xE4A8 ; -7 EE 3
  .fill 0xFFFF
  .fill 0xE890 ; -6 EE 3
  .fill 0xFFFF
  .fill 0xEC78 ; -5 EE 3
  .fill 0xFFFF
  .fill 0xF060 ; -4 EE 3
  .fill 0xFFFF
  .fill 0xF448 ; -3 EE 3
  .fill 0xFFFF
  .fill 0xF830 ; -2 EE 3
  .fill 0xFFFF
  .fill 0xFC18 ; -1 EE 3
  .fill 0xFFFF
  .fill 0xFC7C ; -9 EE 2
  .fill 0xFFFF
  .fill 0xFCE0 ; -8 EE 2
  .fill 0xFFFF
  .fill 0xFD44 ; -7 EE 2
  .fill 0xFFFF
  .fill 0xFDA8 ; -6 EE 2
  .fill 0xFFFF
  .fill 0xFE0C ; -5 EE 2
  .fill 0xFFFF
  .fill 0xFE70 ; -4 EE 2
  .fill 0xFFFF
  .fill 0xFED4 ; -3 EE 2
  .fill 0xFFFF
  .fill 0xFF38 ; -2 EE 2
  .fill 0xFFFF
  .fill 0xFF9C ; -1 EE 2
  .fill 0xFFFF
  .fill 0xFFA6 ; -9 EE 1
  .fill 0xFFFF
  .fill 0xFFB0 ; -8 EE 1
  .fill 0xFFFF
  .fill 0xFFBA ; -7 EE 1
  .fill 0xFFFF
  .fill 0xFFC4 ; -6 EE 1
  .fill 0xFFFF
  .fill 0xFFCE ; -5 EE 1
  .fill 0xFFFF
  .fill 0xFFD8 ; -4 EE 1
  .fill 0xFFFF
  .fill 0xFFE2 ; -3 EE 1
  .fill 0xFFFF
  .fill 0xFFEC ; -2 EE 1
  .fill 0xFFFF
  .fill 0xFFF6 ; -1 EE 1
  .fill 0xFFFF
  .fill 0xFFF7 ; -9 EE 0
  .fill 0xFFFF
  .fill 0xFFF8 ; -8 EE 0
  .fill 0xFFFF
  .fill 0xFFF9 ; -7 EE 0
  .fill 0xFFFF
  .fill 0xFFFA ; -6 EE 0
  .fill 0xFFFF
  .fill 0xFFFB ; -5 EE 0
  .fill 0xFFFF
  .fill 0xFFFC ; -4 EE 0
  .fill 0xFFFF
  .fill 0xFFFD ; -3 EE 0
  .fill 0xFFFF
  .fill 0xFFFE ; -2 EE 0
  .fill 0xFFFF
  .fill 0xFFFF ; -1 EE 0
  .fill 0xFFFF
  .fill 0x0000 ; terminator
.end