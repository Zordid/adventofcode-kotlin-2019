//import com.marcinmoskala.math.permutations
//import kotlinx.coroutines.*
//import kotlinx.coroutines.channels.Channel
//
//class Day07(testData: List<String>? = null) : Day<String>(7, 2019, ::asStrings, testData) {
//
//    private val program = input.justInts()
//
//    fun runConnectedAmps(phases: List<Int>): Int {
//        var out = 0
//        phases.forEach { phase ->
//            val input = sequenceOf(phase, out)
//            //print("---$index---\ninput is ${input.toList()}")
//            val iterator = input.iterator()
//            ShipComputer(program, input = { iterator.next() }, output = { v -> out = v }).run()
//            //println(" => $out")
//        }
//        return out
//    }
//
//    fun runInFeedbackLoop(phases: List<Int>): Int = runBlocking {
//        val channels = phases.map { Channel<Int>(2) }
//
//        val amps = phases.indices.map { id ->
//            ShipComputer(
//                program,
//                input = { channels[id].receive() },
//                output = { out -> channels[(id + 1) % channels.size].send(out) }
//            )
//        }
//        phases.forEachIndexed { channelId, phase -> channels[channelId].send(phase) }
//
//        channels[0].send(0)
//        amps.map { launch { it.runAsync() } }.joinAll()
//        channels[0].receive()
//    }
//
//    override fun part1(): Int {
//        val bestPhaseSetting = listOf(0, 1, 2, 3, 4).permutations()
//            .maxBy { combination ->
//                //println("Now trying $combination")
//                runConnectedAmps(combination)
//            }!!
//        return runConnectedAmps(bestPhaseSetting)
//    }
//
//    override fun part2(): Int {
//        val bestPhaseSetting = listOf(5, 6, 7, 8, 9).permutations()
//            .maxBy { combination ->
//                //println("Now trying $combination")
//                runInFeedbackLoop(combination)
//            }!!
//        return runInFeedbackLoop(bestPhaseSetting)
//    }
//}
//
//fun main() {
//    Day07().run()
//}