package shipcomputer.funwithints

import shipcomputer.compile
import shipcomputer.debugGraphical

val division = """

// main
    IN [INPUT]
    IN [INPUT+1]
    ADD [INPUT] 0 [CALL_PARAM1]
    ADD [INPUT+1] 0 [CALL_PARAM2]
    ADD DIVIDE 0 [CALL_DEST]
    ADD @NEXT+3 0 [CALL_RET]
    JNZ _ CALL

    ADD [CALL_PARAM1] 0 [@NEXT+1]
    OUT ?

    HLT


    // prepare call
    ADD FACT 0 [CALL_DEST]
    ADD [INPUT] 0 [CALL_PARAM]
    ADD BACK 0 [CALL_RET]
    JZ 0 CALL
BACK:
    OUT [CALL_PARAM]
    HLT

INPUT: (_, _)

// end of main

// sub routine FACT - one in/out parameter on stack
FACT:
    ADD [STACK_PTR] 1 [@NEXT+1]
    ADD [?] 0 [N]
    //OUT [N]
    LT [N] 2 [@NEXT+1]
    JNZ ? RETURN

    ADD FACT 0 [CALL_DEST]
    ADD [N] -1 [CALL_PARAM]
    ADD @NEXT+3 0 [CALL_RET]
    JNZ _ CALL

    ADD [STACK_PTR] 1 [@NEXT+1]
    ADD [?] 0 [N]
    MUL [CALL_PARAM] [N] [N]

    ADD [STACK_PTR] 1 [@NEXT+3]
    ADD [N] 0 [?]
    JNZ _ RETURN

N: (_)

DIVIDE:
    ADD [STACK_PTR] 1 [@NEXT+1]
    ADD [?] 0 [A]
    ADD [STACK_PTR] 2 [@NEXT+1]
    ADD [?] 0 [B]

    MUL [B] -1 [B]
    ADD 0 0 [R]

MORE:
    ADD [A] [B] [A]
    LT [A] 0 [@NEXT+1]
    JNZ ? DONE
    ADD [R] 1 [R]
    JZ 0 MORE

DONE:
    ADD [STACK_PTR] 1 [@NEXT+3]
    ADD [R] 0 [?]
    JZ 0 RETURN

A:  (0)
B:  (0)
R:  (0)

CALL:
    // make room for three stack ints
    ADD [STACK_PTR] -3 [STACK_PTR]
    // check stack size and quit if exceeded!
    LT [STACK_PTR] STACK_LOW [@NEXT+1]
    JZ ? STACK_OK
    OUT 999999
    HLT
STACK_OK:
    // push param & return address on stack
    ADD [STACK_PTR] 2 [@NEXT+3]
    ADD [CALL_PARAM2] 0 [?]
    ADD [STACK_PTR] 1 [@NEXT+3]
    ADD [CALL_PARAM1] 0 [?]
    ADD [STACK_PTR] 0 [@NEXT+3]
    ADD [CALL_RET] 0 [?]
    JNZ _ [CALL_DEST]

RETURN:
    // pop return address & param from stack
    ADD [STACK_PTR] 0 [@NEXT+1]
    ADD [?] 0 [CALL_RET]
    ADD [STACK_PTR] 1 [@NEXT+1]
    ADD [?] 0 [CALL_PARAM1]
    ADD [STACK_PTR] 1 [@NEXT+1]
    ADD [?] 0 [CALL_PARAM2]
    ADD [STACK_PTR] 3 [STACK_PTR]
    JNZ _ [CALL_RET]

CALL_DEST: (0)
CALL_RET: (0)
CALL_PARAM:
CALL_PARAM1: (0)
CALL_PARAM2: (0)

STACK_PTR:
    (STACK_HIGH)
STACK_LOW:
    #NOISE_ALIGN_TO 250
STACK_HIGH:

""".trimIndent()

fun main() {
    division.compile().debugGraphical()
}