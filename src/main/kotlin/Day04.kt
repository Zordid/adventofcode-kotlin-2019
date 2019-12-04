class Day04 : Day<String>(4, 2019, ::asStrings) {

    val range = input.first().split("-").map { it.toInt() }.let { it[0]..it[1] }

    override fun part1() = range.count { it.validPart1() }
    override fun part2() = range.count { it.validPart2() }

    fun Int.validPart1() = toString().let { it.neverDecrease() && it.twoAreTheSame() }
    fun Int.validPart2() = toString().let { it.neverDecrease() && it.twoAreTheSameButNotLarger() }

    fun String.neverDecrease() = inPairs().none { (a, b) -> a > b }
    fun String.twoAreTheSame() = inPairs().any { (a, b) -> a == b }
    fun String.twoAreTheSameButNotLarger() = inPairs().any { (a, b) -> a == b && !contains("$a$a$a") }

    fun String.inPairs() = asSequence().zipWithNext()

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Okay, let try this again - but in a fast, imperative way using nothing more than Ints!

    fun part1Optimized() = range.count { it.validPart1Optimized() }
    fun part2Optimized() = range.count { it.validPart2Optimized() }

    fun Int.validPart1Optimized(): Boolean {
        var rest = this
        var previous = 10
        var double = false
        while (rest > 0) {
            val d = rest % 10
            if (d > previous) return false

            if (d == previous) double = true

            previous = d
            rest /= 10
        }
        return double
    }

    fun Int.validPart2Optimized(): Boolean {
        var rest = this
        var previous = 10
        var double = false
        var count = 0
        while (rest > 0) {
            val d = rest % 10
            if (d > previous) return false

            if (d == previous) {
                count++
            } else {
                if (count == 2) double = true
                count = 1
            }

            previous = d
            rest /= 10
        }
        return double || count == 2
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // And now, let's go absolutely crazy with performance. Julius showed: without recurring
    // memory allocation, this beast gets really, really fast! Another x10 factor!

    inline fun checkCodeInRange(predicate: (CharArray) -> Boolean): Int {
        val current = range.first.toString().toCharArray()
        val limit = range.last.toString().toCharArray()

        // increase the start code to the first with non-decreasing digits
        for (i in 1..5) {
            if (current[i] < current[i - 1]) {
                for (j in i..5)
                    current[j] = current[i - 1]
                break
            }
        }

        var count = 0
        while (true) {
            if (predicate(current))
                count++

            // now increase the current code
            if (current[5] != '9')
                current[5]++
            else {
                for (i in 4 downTo 0) {
                    if (current[i] != '9') {
                        current[i]++
                        for (j in i + 1..5) {
                            current[j] = current[i]
                        }
                        break
                    }
                }
            }

            // stop if bigger than high limit
            for (i in 0..5) {
                if (current[i] > limit[i])
                    return count
                else if (current[i] < limit[i])
                    break
            }
        }
    }

    fun part1VeryOptimized() =
        checkCodeInRange {
            for (i in 1..5) {
                if (it[i - 1] == it[i])
                    return@checkCodeInRange true
            }
            false
        }

    fun part2VeryOptimized() =
        checkCodeInRange {
            var digitCount = 1
            for (i in 1..5) {
                if (it[i] == it[i - 1])
                    digitCount++
                else {
                    if (digitCount == 2)
                        break
                    digitCount = 1
                }
            }
            digitCount == 2
        }

}

fun main() {
    Day04().run()

    val day = Day04()
    require(day.part1Optimized() == day.part1())
    require(day.part1VeryOptimized() == day.part1Optimized())
    require(day.part2Optimized() == day.part2())
    require(day.part2VeryOptimized() == day.part2Optimized())

    println("Performance measurement part1:")
    println("took ${measureAverageMillis(100) { day.part1() }} ms per slow call")
    println("took ${measureAverageMillis(1000) { day.part1Optimized() }} ms per fast call")
    println("took ${measureAverageMillis(10000) { day.part1VeryOptimized() }} ms per VERY fast call")

    println("Performance measurement part2:")
    println("took ${measureAverageMillis(100) { day.part2() }} ms per slow call")
    println("took ${measureAverageMillis(1000) { day.part2Optimized() }} ms per fast call")
    println("took ${measureAverageMillis(10000) { day.part2VeryOptimized() }} ms per VERY fast call")

}