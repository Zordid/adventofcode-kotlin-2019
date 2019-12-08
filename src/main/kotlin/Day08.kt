typealias Layer = List<Char>

infix fun Layer.stack(other: Layer) = zip(other).map { (bottom, top) ->
    if (top == '2') bottom else top
}

class Day08(
    testData: List<String>? = null,
    private val width: Int = 25,
    height: Int = 6
) : Day<String>(8, 2019, ::asStrings, testData) {

    val layers = input[0].asSequence()
        .windowed(width * height, step = width * height).map { it.toList() }.toList()

    override fun part1(): Int {
        val layerWithFewestZeros = layers.minBy { it.count { it == '0' } }!!
        val onesInLayer = layerWithFewestZeros.count { it == '1' }
        val twosInLayer = layerWithFewestZeros.count { it == '2' }
        return onesInLayer * twosInLayer
    }

    override fun part2(): String {
        val result = layers.reversed().reduce { acc, layer -> acc stack layer }

        return result.windowed(width, step = width).joinToString("\n") { line ->
            line.joinToString("") {
                when (it) {
                    '0' -> " "
                    '1' -> "#"
                    else -> "?"
                }
            }.trimEnd()
        }
    }
}

fun main() {
    Day08().run()
}