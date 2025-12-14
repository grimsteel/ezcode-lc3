## Memory Structure

```
STACK INITIALIZATION CODE
-> r6 = STACK[0]
PROGRAM CODE
-> Address of data block every 512 words
UTIL SUBROUTINES (~200 words)
-> mul
-> div
-> print
-> input
-> extra data (+ input string pointer)
DATA BLOCK (63 words)
-> variables (32)
-> constants (31)  | data block address points here
STACK (256 words)
-> stack pointer (r6) points to next free spot
STRING BLOCK (rest of memory)
-> string constants
-> new alloc'd strings from input()
```
