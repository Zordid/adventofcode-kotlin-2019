class Day04 : Day<String>(4, 2019, ::asStrings) {

    val range = input.first().split("-").map { it.toInt() }.let { it[0]..it[1] }

    fun String.inPairs() = asSequence().zipWithNext()

    fun String.neverDecrease() = inPairs().none { (a, b) -> a > b }
    fun String.twoAreTheSame() = inPairs().any { (a, b) -> a == b }
    fun String.twoAreTheSameButNotLarger() = inPairs().any { (a, b) -> a == b && !contains("$a$a$a") }

    fun Int.validPart1() = toString().let { it.neverDecrease() && it.twoAreTheSame() }
    fun Int.validPart2() = toString().let { it.neverDecrease() && it.twoAreTheSameButNotLarger() }

    override fun part1() = range.count { it.validPart1() }

    override fun part2() = range.count { it.validPart2() }

}

fun main() {
    Day04().run()
}