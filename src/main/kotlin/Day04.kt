class Day04 : Day<String>(4, 2019, ::asStrings) {

    val range = input.first().split("-").map { it.toInt() }

    val low = range[0]
    val high = range[1]

    fun String.twoAreTheSame() = asSequence().zipWithNext().any { (a, b) -> a == b }
    fun String.neverDecrease() = asSequence().zipWithNext().none { (a, b) -> a > b }

    fun String.twoAreTheSameButNotLarger() = asSequence().zipWithNext().any { (a,b) -> a==b && !contains("$a$a$a")}

    fun Int.valid() = toString().let { it.twoAreTheSame() && it.neverDecrease() }
    fun Int.valid2(): Boolean {
        return toString().let { it.twoAreTheSameButNotLarger() && it.neverDecrease() }
    }

    override fun part1(): Any? {
        return (low..high).count { it.valid() }
    }

    override fun part2(): Any? {
        return (low..high).count { it.valid2() }
    }

}

fun main() {
    Day04().run()
}