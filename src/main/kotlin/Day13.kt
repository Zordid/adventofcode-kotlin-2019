import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

@ExperimentalCoroutinesApi
class Day13(testData: List<String>? = null) : Day<String>(13, 2019, ::asStrings, testData) {

    private val program = input.justLongs()

    private fun getMap() = runBlocking {
        val outChannel = Channel<Long>(Channel.UNLIMITED)
        val sc = ShipComputer(program, output = { outChannel.send(it) })

        launch { sc.runAsync() }.join()

        val map = mutableMapOf<Point, Int>()
        while (!outChannel.isEmpty) {
            val (x, y, tileType) = outChannel.getNext(3)
            map[x to y] = tileType
        }
        map
    }

    override fun part1(): Any? = getMap().count { it.value == 2 }

    private suspend fun Channel<Long>.getNext(n: Int) = (1..n).map { receive().toInt() }

    override fun part2(): Any? = runBlocking {
        var joystick = 0L
        val outChannel = Channel<Long>(Channel.UNLIMITED)
        val sc = ShipComputer(program, output = { outChannel.send(it) }, input = { joystick }).apply {
            writeMem(0, 2)
        }

        val map = mutableMapOf<Point, Int>()

        var pedal: Point = origin
        var ball: Point = origin

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
                } else {
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
                joystick = when {
                    ball.x < pedal.x -> -1L
                    ball.x == pedal.x -> 0
                    else -> +1L
                }
            }


        }
        j.join()
        o.join()
        score
    }

    private fun drawScreen(score: Int, map: MutableMap<Pair<Int, Int>, Int>) {
        val (upperLeft, lowerRight) = map.keys.areaCovered()
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

fun main() {
    Day13().run()
}