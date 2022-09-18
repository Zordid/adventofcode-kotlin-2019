import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

@ExperimentalCoroutinesApi
class Day13(testData: List<String>? = null) : Day<String>(13, 2019, ::asStrings, testData) {

    private val program = input.justLongs()

    val recorder = mutableListOf<String>()

    override fun part1() =
        mutableListOf<Long>().apply {
            program.execute { this.add(it) }
        }.chunked(3).count { it[2] == 2L }

    private suspend fun Channel<Long>.getNext(n: Int) = (1..n).map { receive().toInt() }

    override fun part2(): Int = runBlocking {
        var joystick = 0
        val outChannel = Channel<Long>(Channel.UNLIMITED)

        suspend fun input(): Long {
            recorder += "joystick $joystick"
            return joystick.toLong()
        }

        val sc = IntcodeComputer(program, output = { outChannel.send(it) }, input = ::input).apply {
            memory[0] = 2
        }

        val map = mutableMapOf<Point, Int>()

        var pedal = origin
        var ball = origin

        var score = 0

        val j = launch { sc.runAsync() }
        val o = launch {
            var redraw = false
            while (j.isActive) {
                while (outChannel.isEmpty && !sc.halt) {
                    yield()
                }
                if (sc.halt)
                    break
                val (x, y, t) = outChannel.getNext(3)
                if (x == -1 && y == 0) {
                    score = t
                    recorder += "score $score"
                } else {
                    recorder += "$x, $y, $t"
                    map[x to y] = t
                    when (t) {
                        4 -> {
                            ball = x to y
                            redraw = true
                        }

                        3 -> {
                            pedal = x to y
                            redraw = true
                        }
                    }
                }
                if (redraw) {
                    //drawScreen(score, map)
                    redraw = false
                }
                joystick = ball.x.compareTo(pedal.x)
            }


        }
        j.join()
        o.join()
        score
    }

    private fun drawScreen(score: Int, map: MutableMap<Pair<Int, Int>, Int>) {
        val (upperLeft, lowerRight) = map.keys.boundingBox()
        allPointsInArea(upperLeft, lowerRight).forEach { (x, y) ->
            val c = when (map[x to y]) {
                0 -> ' '
                1 -> '#'
                2 -> '$'
                3 -> '='
                4 -> '*'
                else -> '?'
            }
            print(c)
            if (x == lowerRight.x) println()
        }
        println("Your score: $score")
        println()
    }
}

@ExperimentalCoroutinesApi
fun main() {
    Day13().run()
}