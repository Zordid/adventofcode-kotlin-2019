import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking

class Day21(testData: List<String>? = null) : Day<String>(21, 2019, ::asStrings, testData) {

    private val program = input.asIntcode()

    private fun springDroidExecute(script: String): Long = runBlocking {
        val inputChannel = Channel<Long>(Channel.UNLIMITED)
        val outputChannel = Channel<String>(Channel.UNLIMITED)
        val ic = IntcodeComputer(program, byInChannel(inputChannel), byAsciiOutChannel(outputChannel))

        inputChannel.write(script.split("\n"))
        ic.runAsync()
        outputChannel.close()
        val output = outputChannel.toList()

        val result = output.last().toLongOrNull()
        if (result == null)
            println(output.joinToString("\n"))

        result ?: -1
    }

    override fun part1(): Long {
        // (not(A) and D) or
        // (A and not B and not C and D) or
        // (A and not B and C and D) or
        // (A and B and not(C) and D)

        // => not(A and B and C) or D
        val springScript = """
            OR A T
            AND B T
            AND C T
            NOT T T
            
            OR D J
            
            AND T J
            WALK
        """.trimIndent()
        return springDroidExecute(springScript)
    }

    override fun part2(): Long {
        // (not(A) and D) or
        // (A and not B and not C and D) or
        // (A and not B and C and D) or
        // (A and B and not(C) and D and H)

        // => (not(A) and D) or (not(B) and D) or (not(C) and D and H)
        val springScript = """
            NOT A T
            AND D T
            
            OR T J
            
            NOT B T
            AND D T
            
            OR T J
            
            NOT C T
            AND D T
            AND H T
            
            OR T J
            RUN
        """.trimIndent()
        return springDroidExecute(springScript)
    }

    private suspend fun Channel<Long>.write(s: String) {
        s.forEach { send(it.code.toLong()) }
        send(10L)
    }

    private suspend fun Channel<Long>.write(s: List<String>) {
        s.forEach { if (it.isNotBlank()) write(it) }
    }
}

fun main() {
    Day21().run()
}