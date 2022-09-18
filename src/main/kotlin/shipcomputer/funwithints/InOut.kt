package shipcomputer.funwithints
//
//import execute
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.runBlocking
//import shipcomputer.compile
//import shipcomputer.debug
//
//val inout = """
//
//START:
//    ADD DATA 0 [@NEXT+1]
//NEXT:
//    ADD [?] 0 [C]
//    EQ [C] 0 [@NEXT+1]
//    JNZ _ STOP
//    OUT [C]
//    ADD [NEXT+1] 1 [NEXT+1]
//    JNZ _ NEXT
//
//STOP:
//    IN [@NEXT+1]
//    JNZ ? START
//    HLT
//
//C:  ""
//DATA:
//    "Hello, world!"
//
//
//""".trimIndent()
//
//
//@ExperimentalCoroutinesApi
//suspend fun Channel<Int>.printAsString() {
//    var s = ""
//    while (!isEmpty) {
//        s += receive().toChar()
//    }
//    if (s.isNotBlank()) println(s)
//}
//
//fun main() = runBlocking {
//    val input = Channel<Int>(Channel.UNLIMITED)
//    val output = Channel<Int>(Channel.UNLIMITED)
//    inout.compile().execute(
//        input = {
//            when {
//                !input.isEmpty -> input.receive()
//                else -> {
//                    output.printAsString()
//                    println("Your input:")
//                    val user = readLine()!!
//                    val value = user.toIntOrNull()
//                    value ?: user[0].toInt().also { user.asSequence().drop(1).forEach { input.send(it.toInt()) }}
//                }
//            }
//        },
//        output = {
//            output.send(it)
//        })
//    output.printAsString()
//    println()
//}