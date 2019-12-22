import java.math.BigInteger

class Day22(testData: List<String>? = null, val deckSize: Long = 10_007) :
    Day<String>(22, 2019, ::asStrings, testData) {

    sealed class CardDeck {

        abstract val size: Long
        abstract val factor: Long
        abstract val offset: Long
        open operator fun get(i: Long): Long {
            require(i in 0 until size) { "Cannot access card #$i" }
            return i
        }

        override fun toString() =
            (0 until size).joinToString(" ") { this[it].toString() }

        class FactoryDeck(override val size: Long) : CardDeck() {
            override val factor: Long = 1
            override val offset: Long = 0
        }

        class DealIntoNewStack(val predecessorDeck: CardDeck) : CardDeck() {
            override val size = predecessorDeck.size
            override val factor: Long = -1
            override val offset: Long = size - 1L

            override fun get(i: Long) = predecessorDeck[size - 1 - i]
        }

        class DealWithIncrement(val predecessorDeck: CardDeck, increment: Int) : CardDeck() {
            override val size = predecessorDeck.size
            private val baseForIdxOne = modInverse(increment.toLong(), size)
            override val factor: Long = baseForIdxOne
            override val offset: Long = 0

            private fun betterIdx(idx: Long): Long {
                return baseForIdxOne * idx % size
            }

            override fun get(i: Long) = predecessorDeck[betterIdx(i)]
        }

        class Cut(val predecessorDeck: CardDeck, val n: Int) : CardDeck() {
            override val size = predecessorDeck.size
            override val factor: Long = 1
            override val offset: Long = n + size

            override fun get(i: Long) =
                predecessorDeck[(i + n + size) % size]
        }

        companion object {
            fun parse(s: String, predecessorDeck: CardDeck): CardDeck {
                return when {
                    s.startsWith("deal into new stack") -> DealIntoNewStack(predecessorDeck)
                    s.startsWith("deal with increment") -> DealWithIncrement(
                        predecessorDeck,
                        s.split(" ").last().toInt()
                    )
                    s.startsWith("cut") -> Cut(predecessorDeck, s.split(" ").last().toInt())
                    else -> error(s)
                }
            }
        }
    }


    override fun part1(): Any? {
        val result = input.fold<String, CardDeck>(CardDeck.FactoryDeck(deckSize)) { acc, s -> CardDeck.parse(s, acc) }
        if (deckSize <= 10)
            return result
        return result.find(2019)
    }

    override fun part2(): Any? {

        val r = input.fold<String, Pair<BigInteger, BigInteger>>(BigInteger.ONE to BigInteger.ZERO) { acc, s ->
            CardDeck.parse(s, CardDeck.FactoryDeck(119315717514047))
                .let { (acc.first * it.factor.toBigInteger()) to acc.second + acc.first * it.offset.toBigInteger() }
        }

        println(r.first)
        println(r.second)


        val result =
            input.fold<String, CardDeck>(CardDeck.FactoryDeck(119315717514047)) { acc, s -> CardDeck.parse(s, acc) }


        var cardAt2020 = 2020L
        var shuffled = result[cardAt2020]
        println("Result: $shuffled")

        val calc = (r.first * 2020.toBigInteger() + r.second).mod(result.size.toBigInteger()).toLong()
        println("Calced: $calc")
        return null

        val cycles = mutableMapOf<Long, Long>()
        val repeater = 101741582076661L
        var t = 0L
        while (t < repeater) {
//            if (cardAt2020 in cycles) {
//                val previous = cycles[cardAt2020]!!
//                val remaining = repeater - t
//                val loopSize = t - previous
//                val fits = remaining / loopSize
//                println("shuffled: $t times, encountered card $cardAt2020 before at $previous")
//                println("loop size = $loopSize with $fits loops remaining, skipping!")
//                t += loopSize * fits
//                cardAt2020 = result[cardAt2020]
//                cycles.clear()
//            } else {
            //cycles[cardAt2020] = t
            cardAt2020 = result[cardAt2020]
            t++
//            }

        }
        return cardAt2020
    }
}

fun modInverse(a: Long, m: Long): Long {
    var a = a
    var m = m
    val m0 = m
    var y = 0L
    var x = 1L
    if (m == 1L) return 0
    while (a > 1) { // q is quotient
        val q = a / m
        var t = m
        // m is remainder now, process
        // same as Euclid's algo
        m = a % m
        a = t
        t = y
        // Update x and y
        y = x - q * y
        x = t
    }
    // Make x positive
    if (x < 0) x += m0
    return x
}


private fun Day22.CardDeck.find(card: Long) =
    (0 until size).first { this[it] == card }

fun main() {

//    println(modInverse(3, 11))
//    return
    val testData = """
deal into new stack
    """.trimIndent().split("\n")
    // Day22(testData = testData).run()
    Day22().run()
}