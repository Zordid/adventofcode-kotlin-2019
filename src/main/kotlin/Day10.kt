import kotlin.math.atan

class Day10(testData: List<String>? = null) : Day<List<Char>>(10, 2019, { s -> s.toList() }, testData) {

    val field = AsteroidField(input)

    class AsteroidField(definition: List<List<Char>>) {

        val dimX = definition[0].size
        val dimY = definition.size

        val asteroids = definition.matchingIndexes { it == '#' }.toMutableList()

        fun asteroidsOnLine(a: Point, b: Point): List<Point> {
            if (a == b) return if (a in asteroids) listOf(a) else emptyList()

            val deltaX = b.x - a.x
            val deltaY = b.y - a.y

            val gcd = gcd(deltaX, deltaY)
            val stepX = deltaX / gcd
            val stepY = deltaY / gcd

            val stepsToTake = if (stepX != 0) deltaX / stepX else deltaY / stepY
            return (0..stepsToTake).map { (a.x + it * stepX) to (a.y + it * stepY) }.filter { it in asteroids }
        }

        fun inSight(a: Point, b: Point) = asteroidsOnLine(a, b).size == 2

        fun visibleAsteroidsFrom(a: Point) = asteroids.filter { inSight(a, it) }

        fun hitSequenceFrom(origin: Point) = sequence {
            val endSituation = if (origin in asteroids) listOf(origin) else emptyList()
            while (asteroids != endSituation) {
                // Q1 & straight up
                val q1 = asteroids.filter { it.x >= origin.x && it.y < origin.y && it visibleBy origin }
                q1.sortedBy { atan((it.x - origin.x) fdiv (origin.y - it.y)) }.forEach {
                    asteroids.remove(it)
                    yield(it)
                }

                // Q2 & straight right
                val q2 = asteroids.filter { it.x > origin.x && it.y >= origin.y && it visibleBy origin }
                q2.sortedBy { atan((it.y - origin.y) fdiv (it.x - origin.x)) }.forEach {
                    asteroids.remove(it)
                    yield(it)
                }

                // Q3 & straight down
                val q3 = asteroids.filter { it.x <= origin.x && it.y > origin.y && it visibleBy origin }
                q3.sortedBy { atan((origin.x - it.x) fdiv (it.y - origin.y)) }.forEach {
                    asteroids.remove(it)
                    yield(it)
                }

                // Q4 & straight left
                val q4 = asteroids.filter { it.x < origin.x && it.y <= origin.y && it visibleBy origin }
                q4.sortedBy { atan((origin.y - it.y) fdiv (origin.x - it.x)) }.forEach {
                    asteroids.remove(it)
                    yield(it)
                }
            }
        }

        infix fun Point.visibleBy(other: Point) = asteroidsOnLine(this, other).size == 2

        infix fun Int.fdiv(other: Int): Double = this / other.toDouble()

    }

    fun bestStation() = with(field) { asteroids.maxBy { visibleAsteroidsFrom(it).count() }!! }

    override fun part1() = field.visibleAsteroidsFrom(bestStation()).count()

    override fun part2() = field.hitSequenceFrom(bestStation()).drop(199).first().let { (x, y) -> x * 100 + y }
}

fun main() {
    Day10().run()
}