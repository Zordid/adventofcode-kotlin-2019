class Day06 : Day<String>(6, 2019, ::asStrings) {

    val test = """
        COM)B
        B)C
        C)D
        D)E
        E)F
        B)G
        G)H
        D)I
        E)J
        J)K
        K)L
        K)YOU
        I)SAN
    """.trimIndent().split("\n")

    val relations = input.map { it.split(")").let { it[1] to it[0] } }.toMap()

    val objects = (relations.keys + relations.values).toSet()

    fun countOrbits(o: String): Int = if (relations.containsKey(o)) {
        1 + countOrbits(relations[o]!!)
    } else
        0

    fun orbitPath(o: String): List<String> = if (relations.containsKey(o)) {
        listOf(relations[o]!!) + orbitPath(relations[o]!!)
    } else emptyList()

    override fun part1(): Any? {
        return objects.sumBy { countOrbits(it) }
    }

    override fun part2(): Any? {
        val you = orbitPath("YOU")
        val san = orbitPath("SAN")
        val earliest = you.first {
            san.contains(it)
        }
        println(earliest)
        println("$you\n$san")
        return you.indexOf(earliest) + san.indexOf(earliest)
    }
}

fun main() {
    Day06().run()
}