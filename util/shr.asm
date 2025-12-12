; comment

.orig x3000

	ld r1, test
	jsr shr
	halt

; shifts right r1 -> r0
shr:
	and r0, r0, 0
	; r2: input pos = 0b10
	; r3: output pos = 0b01
	and r2, r2, 0
	and r3, r3, 0
	add r3, r3, 1
	add r2, r2, 2

shr_loop:
  and r4, r2, r1        ; r4 = r2 & r1
	brz shr_chk_done      ; if (r2 & r1) > 0
	not r0, r0            ; r0 = r0 | r3
	not r4, r3
	and r0, r0, r4
	not r0, r0
shr_chk_done:
	add r2, r2, r2        ; r2 << 1
	add r3, r3, r3        ; r3 << 1
	brp shr_loop

	ret

	; a or b = not (not a and not b)

test: .fill 0xABCD
.end