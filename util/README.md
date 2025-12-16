# LC-3 Assembly Utility Routines

* 16-bit signed multiplication
* 16-bit signed division
* signed 5-digit print

All routines are located in `mul.asm`. The other files are for reference only.

## Implementation Information

Parameters and return values are pushed/popped off a stack addressed by `r6`. All register values are preserved by `mul` and `div`, but not `print` [^1].

All three subroutines are are O(1) time complexity in the values of the numbers. `mul` and `div` are technically O(N) in the bit width, and `print` is O(D) in the number of base-10 digits.

[^1]: Why does print not preserve register values? Print will only be called as the last operation in a print statement, so no registers are used afterwards.

## Stack Conventions

`r6` stores the memory address _after_ the top stack value.
Division divides `stack[r6 - 2] / stack[r6 - 1]`, so the dividend should be pushed first, followed by the divisor.
`div` pushes the quotient first, followed by the remainder

## `lc3tools` `.obj` file format

Subroutines here are assembled by lc3tools. The resulting obj files are parsed into hex arrays for embedding in our compiler.

This is a short reference of the `.obj` file format, based on [the converter source](https://github.com/chiragsakhuja/lc3tools/blob/74d5e971506716c0979e21c3a06d228fbb719a94/src/backend/converter.cpp#L67).

1. A 5 byte magic header: `1C 30 15 C0 01`
2. A 2 byte version: `01 01`
3. A sequence of instructions/memory values:
   1. The two-byte instruction, little endian
   2. A one-byte boolean. `01` if this is the `.orig` header, `00` if this is an instr/mem value
   3. A four-byte length, little endian, for the "comment"/text associated with this instruction
   4. The text associated with the instruction. `lc3tools` uses the assembly text here
