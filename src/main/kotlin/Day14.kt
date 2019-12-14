import kotlin.math.ceil

data class Reaction(val need: List<Pair<Int, String>>, val result: Pair<Int, String>)

const val FUEL = "FUEL"
const val ORE = "ORE"

class NanoFactory(rawReactions: List<Reaction>) {

    val elements = listOf(FUEL) + rawReactions.map { r ->
        r.need.map { it.second }
    }.flatten().distinct()

    val fuelIdx = elements.indexOf(FUEL)
    val oreIdx = elements.indexOf(ORE)

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

    fun createSituation(fuel: Long, ore: Long) = LongArray(elements.size).also {
        it[fuelIdx] = fuel
        it[oreIdx] = ore
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

fun asReactions(s: String): Reaction {
    val (need, result) = s.split("=>").map { it.trim() }
    val needs = need.split(Regex(",\\s+")).map {
        val (amount, material) = it.split(" ").map { it.trim() }
        amount.toInt() to material
    }
    val (amount, material) = result.split(" ").map { it.trim() }
    return Reaction(needs, amount.toInt() to material)
}

class Day14(testData: String? = null) : Day<Reaction>(14, 2019, ::asReactions, testData?.split("\n")) {

    val reactions = input.map { it.result to it.need }.toMap()

    override fun part1(): Long {
        if (true) {
            val nf = NanoFactory(input)

            val situation = nf.createSituation(-1, Long.MAX_VALUE)
            return if (nf.react(situation)) Long.MAX_VALUE - situation[nf.oreIdx] else -1
        }

        val needs = mutableMapOf("FUEL" to 1)
        val have = mutableMapOf("ORE" to Int.MAX_VALUE)

        while (needs.entries.sumBy { it.value } > 0) {
            val need = needs.entries.first { it.value > 0 }
            val reaction = reactions.entries.first { it.key.second == need.key }

            val factor = ceil(need.value.toDouble() / reaction.key.first).toInt()
            val leftOver = factor * reaction.key.first - need.value

            needs.remove(need.key)

            reaction.value.forEach { (amount, what) ->
                var stillNeeded = amount * factor
                if (have[what] ?: 0 > 0) {
                    if (have[what]!! >= stillNeeded) {
                        have[what] = have[what]!! - stillNeeded
                        stillNeeded = 0
                    } else {
                        stillNeeded -= have[what]!!
                        have[what] = 0
                    }
                }
                needs[what] = (needs[what] ?: 0) + stillNeeded
            }

            have[need.key] = (have[need.key] ?: 0) + leftOver
        }
        return Int.MAX_VALUE - (have["ORE"] ?: 0).toLong()
    }

    override fun part2(): Long {
        val initial = 1000000000000
        val have = mutableMapOf("ORE" to initial)

        val nf = NanoFactory(input)
        var situation = nf.createSituation(0, initial)

        var produced = 0L

        while (situation[nf.oreIdx] > 0) {
            situation[0] = -1

            if (nf.react(situation)) {
                produced++

                val oresLeft = situation[nf.oreIdx]
                if (situation.sum() == oresLeft) {
                    val oresNeeded = initial - oresLeft
                    println("Period detected after $produced FUELs and $oresNeeded OREs consumed!")
                    println("Shortcutting... :)")
                    val periods = (initial / oresNeeded)
                    produced *= periods
                    situation[nf.oreIdx] = initial % oresNeeded
                }

            }
        }
        return produced




        while (have["ORE"] ?: 0 > 0) {
            val needs = mutableMapOf("FUEL" to 1L)
            while (needs.entries.any { it.value > 0L } && needs["ORE"] ?: 0L == 0L) {
                val need = needs.entries.first { it.value > 0 }
                val reaction = reactions.entries.first { it.key.second == need.key }

                val factor = ceil(need.value.toDouble() / reaction.key.first).toInt()
                val leftOver = factor * reaction.key.first - need.value

                needs.remove(need.key)

                reaction.value.forEach { (amount, what) ->
                    var stillNeeded = (amount * factor).toLong()
                    if (have[what] ?: 0 > 0) {
                        if (have[what]!! >= stillNeeded) {
                            have[what] = have[what]!! - stillNeeded
                            stillNeeded = 0
                        } else {
                            stillNeeded -= have[what]!!
                            have[what] = 0
                        }
                    }
                    needs[what] = (needs[what] ?: 0) + stillNeeded
                }

                have[need.key] = (have[need.key] ?: 0) + leftOver
            }
            if (needs["ORE"] ?: 0L == 0L)
                produced++
        }
        return produced
    }

}

fun main() {
    Day14().run()
}
