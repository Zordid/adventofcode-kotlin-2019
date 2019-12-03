import kotlin.math.absoluteValue

class Day03 : Day<String>(3, 2019, ::asStrings) {

    val wire1 = input[0].split(",")
    val wire2 = input[1].split(",")

    fun followWire(wire: List<String>): Pair<Set<Pair<Int, Int>>, Map<Pair<Int, Int>, Int>> {
        val r = mutableSetOf<Pair<Int, Int>>()
        val steps = mutableMapOf<Pair<Int, Int>, Int>()
        var c = 0 to 0
        var stepCount = 0
        wire.forEach { step ->
            val d = step[0]
            val count = step.substring(1).toInt()
            repeat(count) {
                when (d) {
                    'R' -> c = c.first + 1 to c.second
                    'L' -> c = c.first - 1 to c.second
                    'U' -> c = c.first to c.second - 1
                    'D' -> c = c.first to c.second + 1
                }
                stepCount++
                steps[c]=stepCount
                r += c
            }
        }
        return r to steps
    }

    override fun part1(): Any? {
        val w1 = followWire(wire1).first
        val w2 = followWire(wire2).first
        val common = w1.intersect(w2).minBy { it.first.absoluteValue + it.second.absoluteValue }

        return "$common ${common!!.first.absoluteValue + common!!.second.absoluteValue}"
    }

    override fun part2(): Any? {
        val w1 = followWire(wire1)
        val w2 = followWire(wire2)
        val common = w1.first.intersect(w2.first).minBy { w1.second[it]!!+w2.second[it]!! }

        return "$common ${w1.second[common]!!+w2.second[common]!!}"
    }

}

fun main() {
    with(Day03()) {
        println(part1())
        println(part2())
    }
}