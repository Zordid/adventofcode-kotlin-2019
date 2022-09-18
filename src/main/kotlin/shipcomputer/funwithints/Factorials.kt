package shipcomputer.funwithints
//
//import shipcomputer.compile
//import shipcomputer.debugGraphical
//
//val factorial = """
//
//// main
//    IN [INPUT]
//
//    // prepare call
//    ADD FACT 0 [CALL_DEST]
//    ADD [INPUT] 0 [CALL_PARAM]
//    ADD BACK 0 [CALL_RET]
//    JZ 0 CALL
//BACK:
//    OUT [CALL_PARAM]
//    HLT
//
//INPUT: (_)
//
//// end of main
//
//// sub routine FACT - one in/out parameter on stack
//FACT:
//    ADD [STACK_PTR] 1 [@NEXT+1]
//    ADD [?] 0 [N]
//    //OUT [N]
//    LT [N] 2 [@NEXT+1]
//    JNZ ? RETURN
//
//    ADD FACT 0 [CALL_DEST]
//    ADD [N] -1 [CALL_PARAM]
//    ADD @NEXT+3 0 [CALL_RET]
//    JNZ _ CALL
//
//    ADD [STACK_PTR] 1 [@NEXT+1]
//    ADD [?] 0 [N]
//    MUL [CALL_PARAM] [N] [N]
//
//    ADD [STACK_PTR] 1 [@NEXT+3]
//    ADD [N] 0 [?]
//    JNZ _ RETURN
//
//N: (_)
//
//CALL:
//    // make room for two stack ints
//    ADD [STACK_PTR] -2 [STACK_PTR]
//    // check stack size and quit if exceeded!
//    LT [STACK_PTR] STACK_LOW [@NEXT+1]
//    JZ ? STACK_OK
//    OUT 999999
//    HLT
//STACK_OK:
//    // push param & return address on stack
//    ADD [STACK_PTR] 1 [@NEXT+3]
//    ADD [CALL_PARAM] 0 [?]
//    ADD [STACK_PTR] 0 [@NEXT+3]
//    ADD [CALL_RET] 0 [?]
//    JNZ _ [CALL_DEST]
//
//RETURN:
//    // pop return address & param from stack
//    ADD [STACK_PTR] 0 [@NEXT+1]
//    ADD [?] 0 [CALL_RET]
//    ADD [STACK_PTR] 1 [@NEXT+1]
//    ADD [?] 0 [CALL_PARAM]
//    ADD [STACK_PTR] 2 [STACK_PTR]
//    JNZ _ [CALL_RET]
//
//CALL_DEST: (0)
//CALL_RET: (0)
//CALL_PARAM: (0)
//
//STACK_PTR:
//    (STACK_HIGH)
//STACK_LOW:
//    #NOISE_ALIGN_TO 161
//STACK_HIGH:
//
//""".trimIndent()
//
//fun main() {
//    factorial.compile().debugGraphical({12})
//}