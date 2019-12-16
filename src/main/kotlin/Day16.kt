import kotlin.math.absoluteValue

class Day16(testData: List<String>? = null) : Day<String>(16, 2019, ::asStrings, testData) {

    val signal = input[0].toCharArray().map { it.toString().toInt() }

    val basePattern = sequenceOf(0, 1, 0, -1)

    fun patternForDigit(n: Int) = basePattern.asRepeatingSequence(n).asInfiniteSequence().drop(1)

    fun fft(input: List<Int>): List<Int> {
        val output = mutableListOf<Int>()
        for (idx in input.indices) {
            val pattern = patternForDigit(idx + 1)
            //println("$idx: ${pattern.take(input.size).joinToString()}")
            output.add(input.asSequence().zip(pattern).map { (i, p) -> i * p }.sum().absoluteValue % 10)
        }
        return output
    }

    fun <T> Sequence<T>.asRepeatingSequence(f: Int): Sequence<T> = sequence {
        for (element in this@asRepeatingSequence) {
            repeat(f) { yield(element) }
        }
    }

    fun <T> Sequence<T>.asInfiniteSequence(): Sequence<T> = sequence {
        while (true) yieldAll(this@asInfiniteSequence)
    }

    override fun part1(): String {
        //var s = signal
        var s = input[0].repeat(signalRepeat).toCharArray().map { it.toString().toInt() }
        println("Input signal: ${s.joinToString("")}")
        repeat(phasesMAX) {
            s = fft(s)
            println("After ${it + 1} phase: ${s.joinToString("")}")
        }
        return s.slice(offset + 0..offset + 7).joinToString("")
    }

    val cache = mutableMapOf<Int, Int>()

    fun periodOfPhase(phase: Int): Int {
        if (phase == 0)
            return lcm(4, signal.size)
        return lcm(4, periodOfPhase(phase - 1))
    }

    fun digit(n: Int, phase: Int): Int {
        if (phase == 0) {
            return signal[(n - 1) % signal.size]
        }
//        val cacheKey = n * 1000 + phase
//        if (cache.containsKey(cacheKey))
//            return cache[cacheKey]!!

        //val patternForDigit = patternForDigit(n).iterator()

        var sum = 0
        var idx = n
        var f = 1
        var fCount = n

        // start at n repeat pattern block is n values
        val period = 4 * n

        val periodLength = lcm(period, signal.size)

        val fullPeriods = signal.size * signalRepeat / periodLength
        val leftOver = signal.size * signalRepeat % periodLength

        // 1 pattern
        if (fullPeriods > 0) {
            while (idx <= periodLength) {
                val d = digit(idx, phase - 1)
                sum += (f * d)
                idx++
                fCount--
                if (fCount == 0) {
                    idx += n
                    f = -f
                    fCount = n
                }

            }
            require(fCount == n)
            require(f == 1)
            sum *= fullPeriods
        }
        if (leftOver > 0) {
            while (idx <= signal.size * signalRepeat) {
                val d = digit(idx, phase - 1)
                sum += (f * d) % 10
                idx++
                fCount--
                if (fCount == 0) {
                    idx += n
                    f = -f
                    fCount = n
                }
            }
        }
        val result = sum.absoluteValue % 10
        //cache[cacheKey] = result
        return result
    }

    val factors = listOf(0, 1, 0, -1)

    fun factorFor(idx: Int, n: Int): Int {
        val bucket = idx / n
        return factors[bucket % 4]
    }

    val signalRepeat = 100
    val phasesMAX = 2
    var offset = 0// signal.slice(0..6).joinToString("").toInt()

    override fun part2(): Any? {
        for (i in (offset + 1..offset + 24)) {
            print(digit(i, 1))
        }
        println()
        for (i in (offset + 1..offset + 24)) {
            print(digit(i, phasesMAX))
        }
        println()

        return ""
    }


}

fun main() {
    val testData = listOf("12345678")
    Day16(testData).run()
}