class Day06(testData: List<String>? = null) : Day<String>(6, 2019, ::asStrings, testData) {

    val relations = input.map { it.split(")").let { it[1] to it[0] } }.toMap()
    val objects = (relations.keys + relations.values).toSet()

    fun countOrbits(o: String): Int = relations[o]?.let { countOrbits(it) + 1 } ?: 0

    fun orbitPath(o: String): List<String> = relations[o]?.let { listOf(it) + orbitPath(it) } ?: emptyList()

    fun totalOrbits() = objects.sumBy { countOrbits(it) }

    fun minimumTransfers(from: String, to: String): Int {
        val fromPath = orbitPath(from)
        val toPath = orbitPath(to)
        val earliest = fromPath.first { toPath.contains(it) }
        return fromPath.indexOf(earliest) + toPath.indexOf(earliest)
    }

    override fun part1() = totalOrbits()

    override fun part2() = minimumTransfers("YOU", "SAN")
}

fun main() {
    Day06().run()
}