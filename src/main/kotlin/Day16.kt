import kotlin.math.absoluteValue

class Day16(testData: List<String>? = null) : Day<String>(16, 2019, ::asStrings, testData) {

    val basePattern = sequenceOf(0, 1, 0, -1)

    fun String.toSignal() = toCharArray().map { it.toString().toInt() }.toIntArray()
    fun IntArray.toString() = joinToString("")

    fun patternForDigit(n: Int) = basePattern.asRepeatingSequence(n).asInfiniteSequence().drop(1)

    fun <T> Sequence<T>.asRepeatingSequence(f: Int): Sequence<T> = sequence {
        for (element in this@asRepeatingSequence) {
            repeat(f) { yield(element) }
        }
    }

    fun <T> Sequence<T>.asInfiniteSequence(): Sequence<T> = sequence {
        while (true) yieldAll(this@asInfiniteSequence)
    }

    fun fft(input: IntArray): IntArray {
        val output = input.copyOf()
        for (idx in input.indices) {
            val pattern = patternForDigit(idx + 1)
            output[idx] = input.asSequence().zip(pattern).map { (i, p) -> i * p }.sum().absoluteValue % 10
        }
        return output
    }

    override fun part1(): String {
        var s = input[0].toSignal()
        //println("Input signal: ${s.joinToString("")}")
        repeat(100) {
            s = fft(s)
            //println("After ${it + 1} phase: ${s}")
        }
        return s.slice(0..7).joinToString("")
    }

    override fun part2(): String {
        val offset = input[0].slice(0..6).toInt()
        val s = input[0].repeat(10000).toSignal()
        repeat(100) {
            for (pos in s.lastIndex - 1 downTo offset) {
                s[pos] = (s[pos] + s[pos + 1]) % 10
            }
        }
        return s.slice(offset..offset + 7).joinToString("")
    }

}

fun main() {
    Day16().run()
}
