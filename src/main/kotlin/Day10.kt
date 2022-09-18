import kotlin.math.atan2

class Day10(testData: List<String>? = null) : Day<List<Char>>(10, 2019, { s -> s.toList() }, testData) {

    val field = AsteroidField(input)

    class AsteroidField(definition: List<List<Char>>) {
        val dimX = definition[0].size
        val dimY = definition.size
        val asteroids = definition.matchingIndexes { it == '#' }

        private fun visibleFromByAngleUnsorted(a: Point) =
            asteroids.filterNot { it == a }.groupBy { other ->
                val delta = other - a
                val gcd = gcd(delta.x, delta.y)
                delta / gcd
            }

        fun countVisibleFrom(a: Point) =
            visibleFromByAngleUnsorted(a).size

        fun visibleFrom(a: Point) =
            visibleFromByAngleUnsorted(a).values.map { it.minBy { a manhattanDistanceTo it } }

        fun visibleFromByAngle(a: Point) =
            visibleFromByAngleUnsorted(a).mapValues { (_, v) -> v.sortedBy { a manhattanDistanceTo it } }

        fun hitSequenceFrom(origin: Point) = sequence {

            fun Point.toSortingAngle(): Double {
                if (this == 0 to -1) return -Math.PI
                val rotated = y to -x
                return atan2(rotated.y.toDouble(), rotated.x.toDouble())
            }

            val visibleByAngleSorted = visibleFromByAngle(origin)
                .entries.sortedBy { (k, _) -> k.toSortingAngle() }

            val rotations = visibleByAngleSorted.maxBy { (_, v) -> v.size }.value.size
            repeat(rotations) { r ->
                visibleByAngleSorted.forEach { (_, v) ->
                    if (r in v.indices) yield(v[r])
                }
            }

        }

        infix fun Int.fdiv(other: Int): Double = this / other.toDouble()

    }

    fun bestStation() = with(field) { asteroids.maxBy { countVisibleFrom(it) } }

    override fun part1() = field.countVisibleFrom(bestStation())

    override fun part2() = field.hitSequenceFrom(bestStation()).drop(199).first().let { (x, y) -> x * 100 + y }
}

fun main() {
    Day10().run()
}