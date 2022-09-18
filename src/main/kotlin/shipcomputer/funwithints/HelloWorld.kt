package shipcomputer.funwithints
//
//import execute
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.runBlocking
//import shipcomputer.compile
//import shipcomputer.debugGraphical
//
//val hello = """
//START:
//    EQ [DATA] 0 [@NEXT+1]
//    JNZ ? STOP
//    OUT [DATA]
//    ADD [@THIS-1] 1 [@THIS-1]
//    ADD [START+1] 1 [START+1]
//
//    JNZ _ START
//
//STOP:
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
//fun main() = runBlocking {
//    val output = Channel<Int>(Channel.UNLIMITED)
//    hello.compile().debugGraphical(
//        output = {
//            output.send(it)
//        })
//    output.printAsString()
//    println()
//}