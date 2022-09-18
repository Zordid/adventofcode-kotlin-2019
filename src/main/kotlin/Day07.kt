import com.marcinmoskala.math.permutations
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Day07(testData: List<String>? = null) : Day<String>(7, 2019, ::asStrings, testData) {

    private val program = input.justLongs()

    fun runConnectedAmps(phases: List<Long>): Long {
        var out = 0L
        phases.forEach { phase ->
            val input = sequenceOf(phase, out)
            //print("---$index---\ninput is ${input.toList()}")
            val iterator = input.iterator()
            IntcodeComputer(program, input = { iterator.next() }, output = { v -> out = v }).run()
            //println(" => $out")
        }
        return out
    }

    fun runInFeedbackLoop(phases: List<Long>): Long = runBlocking {
        val channels = phases.map { Channel<Long>(2) }

        val amps = phases.indices.map { id ->
            IntcodeComputer(
                program,
                input = { channels[id].receive() },
                output = { out -> channels[(id + 1) % channels.size].send(out) }
            )
        }
        phases.forEachIndexed { channelId, phase -> channels[channelId].send(phase) }

        channels[0].send(0)
        amps.map { launch { it.runAsync() } }.joinAll()
        channels[0].receive()
    }

    override fun part1(): Long {
        val bestPhaseSetting = listOf<Long>(0, 1, 2, 3, 4).permutations()
            .maxBy { combination ->
                //println("Now trying $combination")
                runConnectedAmps(combination)
            }
        return runConnectedAmps(bestPhaseSetting)
    }

    override fun part2(): Long {
        val bestPhaseSetting = listOf<Long>(5, 6, 7, 8, 9).permutations()
            .maxBy { combination ->
                //println("Now trying $combination")
                runInFeedbackLoop(combination)
            }
        return runInFeedbackLoop(bestPhaseSetting)
    }
}

fun main() {
    Day07().run()
}