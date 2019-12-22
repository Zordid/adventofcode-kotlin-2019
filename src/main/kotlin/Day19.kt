import com.marcinmoskala.math.pow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Day19(testData: List<String>? = null) : Day<String>(19, 2019, ::asStrings, testData) {

    val program = input.asIntcode()

    override fun part1(): Int = runBlocking {
        val input = Channel<Long>(Channel.UNLIMITED)
        val output = Channel<Long>(Channel.UNLIMITED)
        val ic = IntcodeComputer(program, byInChannel(input), byOutChannel(output))

        launch { ic.runAsync() }

        suspend fun check(p: Point): Boolean {
            ic.reset()
            input.send(p.x.toLong())
            input.send(p.y.toLong())
            return output.receive() == 1L
        }

        val area = origin to (49 to 49)

//        area.allPoints().chunked(50).forEach {
//            print("%3d - ".format(it.first().y))
//            it.forEach { point ->
//                print(if (check(point)) '#' else '.')
//                if ((point.x + 1) % 5 == 0) print(' ')
//            }
//            println()
//        }

        area.allPoints().count { check(it) }
    }

    override fun part2(): Int {
        val size = 100

        var lower = origin
        var upper = origin

        fun check(p: Point): Boolean {
            require(p.x >= 0 && p.y >= 0)
            val result = mutableListOf<Long>()
            val ic = IntcodeComputer(program, byValues(p.x.toLong(), p.y.toLong()), byCapturing(result))
            ic.run()
            return result.first() == 1L
        }

        fun adjustByStart(startPoint: Point): Int {
            var (x, y) = startPoint
            if (!check(x to y)) {
                do {
                    x++
                } while (!check(x to y))
            } else {
                while (check(x - 1 to y)) {
                    x--
                }
            }
            return x
        }

        fun adjustByEnd(endPoint: Point): Int {
            var (x, y) = endPoint
            if (!check(x to y)) {
                do {
                    x--
                } while (!check(x to y))
            } else {
                while (check(x + 1 to y)) {
                    x++
                }
            }
            return x
        }

        fun checkByRow(y: Int): Pair<Int, Int> {
            require(y >= 0)
            //println("Checking row $y")
            val suspectedStart = (lower.x / lower.y.toDouble() * y).toInt() to y
            val realStart = adjustByStart(suspectedStart)
            if (realStart != suspectedStart.x) {
                //println("Guessed start at ${suspectedStart.x} but was $realStart - adjusting")
            }
            val suspectedEnd = (upper.x / upper.y.toDouble() * y).toInt().coerceAtLeast(realStart) to y
            val realEnd = adjustByEnd(suspectedEnd)
            if (realStart != suspectedStart.x) {
                //println("Guessed end at ${suspectedEnd.x} but was $realEnd - adjusting")
            }
            if (y > lower.y) {
                lower = realStart to y
                upper = realEnd to y
            }
            //println(" $realStart - $realEnd")
            return realStart to realEnd
        }

        fun checkFit(y: Int, size: Int = 10): Point? {
            //print("Check Fit for y=$y ")
            val lastY = y + size - 1
            val firstRow = checkByRow(y)
            if (firstRow.second - firstRow.first + 1 < size) {
                //println("X")
                return null
            }
            val secondRow = checkByRow(lastY)
            if (firstRow.second - secondRow.first + 1 >= size) {
                //println("Y")
                return secondRow.first to y
            }
            //println("X")
            return null
        }

        (1..4).forEach { checkByRow(10.pow(it)) }
        val firstValidRow = maximizeByBinarySearch(10, 9999) {
            checkByRow(it).let { it.second - it.first + 1 } < size
        } + 1

        //println("First valid with 100+ width: $firstValidRow")
        val rY = (firstValidRow..9999).first {
            checkFit(it, size) != null
        }
        val result = checkFit(rY, size)!!
        return result.x * 10000 + result.y
    }

    private fun maximizeByBinarySearch(
        min: Int,
        max: Int,
        betterSafeThanSorry: Boolean = true,
        predicate: (Int) -> Boolean
    ): Int {
        if (betterSafeThanSorry) {
            require(min < max)
            require(predicate(min)) { "At $min the predicate should be true" }
            require(!predicate(max)) { "At $max the predicate should be false" }
        }
        if (max - min == 1) return min
        val half = min + (max - min) / 2
        return if (predicate(half))
            maximizeByBinarySearch(half, max, false, predicate)
        else
            maximizeByBinarySearch(min, half, false, predicate)
    }
}

fun main() {
    Day19().run()
}