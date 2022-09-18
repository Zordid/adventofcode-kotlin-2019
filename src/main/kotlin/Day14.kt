data class Reaction(val need: List<Pair<Int, String>>, val result: Pair<Int, String>)

const val FUEL = "FUEL"
const val ORE = "ORE"

class NanoFactory(rawReactions: List<Reaction>) {

    private val elements = extractElements(rawReactions)

    private fun extractElements(rawReactions: List<Reaction>): List<String> {
        val reactionDependencies = rawReactions.associate {
            it.result.second to it.need.map { it.second }
        }
        val allElements = reactionDependencies.keys + reactionDependencies.values.flatten()

        fun search(start: String, target: String): Boolean =
            (reactionDependencies[start]?.contains(target) == true) ||
                    (reactionDependencies[start]?.any { search(it, target) } == true)

        fun compare(e1: String, e2: String): Int {
            return when {
                e1 == e2 -> 0
                search(e1, e2) -> -1
                search(e2, e1) -> +1
                else -> e1.compareTo(e2)
            }
        }

        return allElements.sortedWith(Comparator(::compare))
    }

    val fuelIdx = 0
    val oreIdx = elements.lastIndex

    val reactionVectors = elements
        .map { element ->
            val r = rawReactions.filter { it.result.second == element }
            require(r.size <= 1)
            r.firstOrNull()?.let {
                val v = LongArray(elements.size)
                v[elements.indexOf(it.result.second)] = it.result.first.toLong()
                it.need.forEach { (x, n) ->
                    v[elements.indexOf(n)] = -x.toLong()
                }
                v
            }
        }

    init {
        require(reactionVectors.count { it == null } == 1)
    }

    fun createSituation(fuel: Long) = LongArray(elements.size).also {
        it[fuelIdx] = fuel
    }

    fun react(situation: LongArray): Boolean {
        var inNeed = situation.indexOfFirst { it < 0 }
        while (inNeed >= 0) {
            val reaction = reactionVectors[inNeed] ?: return false
            val needed = -situation[inNeed]
            val created = reaction[inNeed]
            val factor = needed / created + if (needed % created == 0L) 0 else 1
            situation.add(reaction, factor)
            inNeed = situation.indexOfFirst { it < 0 }
        }
        return true
    }

}

fun LongArray.add(other: LongArray, times: Long = 1L) {
    for (i in indices) {
        this[i] = this[i] + other[i] * times
    }
}

fun parseReaction(s: String): Reaction {
    val (need, result) = s.split("=>").map { it.trim() }
    val needs = need.split(Regex(",\\s+")).map {
        val (amount, material) = it.split(" ").map { it.trim() }
        amount.toInt() to material
    }
    val (amount, material) = result.split(" ").map { it.trim() }
    return Reaction(needs, amount.toInt() to material)
}

class Day14(testData: String? = null) : Day<Reaction>(14, 2019, ::parseReaction, testData?.split("\n")) {

    private val nf = NanoFactory(input)

    private fun calculateNeededOreFor(fuel: Long): Long {
        val situation = nf.createSituation(-fuel)
        nf.react(situation)
        return -situation.last()
    }

    fun maximizeByBinarySearch(min: Long, max: Long, linearFun: (Long) -> Boolean): Long {
//        require(min < max)
//        require(linearFun(min))
//        require(!linearFun(max))
        if (max - min == 1L) return min
        val half = min + (max - min) / 2L
        return if (linearFun(half))
            maximizeByBinarySearch(half, max, linearFun)
        else
            maximizeByBinarySearch(min, half, linearFun)
    }

    override fun part1() = calculateNeededOreFor(1)

    override fun part2(): Long {
        val availableOres = 1000000000000

        val perFuel = calculateNeededOreFor(1)
        val min = availableOres / perFuel

        return maximizeByBinarySearch(min, availableOres) {
            calculateNeededOreFor(it) <= availableOres
        }
    }

}

fun main() {
    Day14().run()
}
