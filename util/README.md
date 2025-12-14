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
Division divides `stack[r6 - 2] / stack[r6 - 1]`, so the dividend is pushed first, followed by the divisor.
