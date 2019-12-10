import kotlin.math.absoluteValue
import kotlin.math.atan

class Day10(testData: List<String>? = null) : Day<List<Char>>(10, 2019, { s -> s.toCharArray().toList() }, testData) {

    val asteroidField = input
    val dimX = asteroidField[0].size
    val dimY = asteroidField.size

    val asteroids =
        (0 until dimY).map { y -> (0 until dimX).map { x -> x to y }.filter { asteroidField[it.y][it.x] == '#' } }
            .flatten()

    fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    fun Int.listX(n: Int) = (0 until n).map { this }

    fun Point.visibleBy(other: Point, allAsteroids: List<Point>): Boolean {
        if (this == other) return false

        // is this point visible by the asteroid?
        //println("is $this visible by $asteroid?")
        val deltaX = other.x - this.x
        val deltaY = other.y - this.y

        val gcd = gcd(deltaX, deltaY)
        val stepX = deltaX / gcd
        val stepY = deltaY / gcd

        val xProgression: List<Int> = when {
            deltaX == 0 -> this.x.listX(deltaY.absoluteValue + 1)
            deltaX > 0 -> (this.x..other.x step stepX.absoluteValue).toList()
            else -> (this.x downTo other.x step stepX.absoluteValue).toList()
        }

        val yProgression: List<Int> = when {
            deltaY == 0 -> this.y.listX(deltaX.absoluteValue + 1)
            deltaY > 0 -> (this.y..other.y step stepY.absoluteValue).toList()
            else -> (this.y downTo other.y step stepY.absoluteValue).toList()
        }

        return xProgression.zip(yProgression).count { it in allAsteroids } == 2
    }

    fun Point.sees() = asteroids.filter { it.visibleBy(this, asteroids) }

    fun bestStation() = asteroids.maxBy { it.sees().count() }!!

    override fun part1(): Any? {
//        for (y in 0 until dimY) {
//            for (x in 0 until dimX) {
//                if (asteroidField[y][x] == '#') {
//                    print((x to y).sees().count())
//                } else print(".")
//            }
//            println()
//        }
        return bestStation().sees().count()
    }

    override fun part2(): Any? {
        val best = bestStation()

        var hitList = mutableListOf<Point>()

        println(best)

        fun Point.hit() {
            hitList.add(this)
            println("${hitList.size}: $this")
        }

        val remaining = asteroids

        // straight up
        (best.y downTo 0).map { best.x to it }.firstOrNull { it.visibleBy(best, remaining) }?.hit()

        println("Q1")
        val q1 = (best.x + 1 until dimX).map { x -> (0 until best.y).map { y -> x to y } }.flatten()
            .filter { it in remaining }.map { it to atan((it.x - best.x).toDouble() / (best.y - it.y).toDouble()) }
        q1.sortedBy { it.second }.forEach { p ->
            //println("check: $p")
            if (p.first.visibleBy(best, remaining))
                p.first.hit()
        }

        // straight right
        (best.x until dimX).map { it to best.y }.firstOrNull { it.visibleBy(best, remaining) }?.hit()

        println("Q2")
        val q2 = (best.x + 1 until dimX).map { x -> (best.y + 1 until dimY).map { y -> x to y } }.flatten()
            .filter { it in remaining }.map { it to atan((it.y - best.y).toDouble() / (it.x - best.x).toDouble()) }
        q2.sortedBy { it.second }.forEach { p ->
            //println("check: $p")
            if (p.first.visibleBy(best, remaining))
                p.first.hit()
        }

        // straight down
        (best.y + 1 until dimY).map { best.x to it }.firstOrNull { it.visibleBy(best, remaining) }?.hit()

        println("Q3")
        val q3 = (best.x - 1 downTo 0).map { x -> (best.y + 1 until dimY).map { y -> x to y } }.flatten()
            .filter { it in remaining }.map { it to atan((best.x - it.x).toDouble() / (it.y - best.y).toDouble()) }
        q3.sortedBy { it.second }.forEach { p ->
            //println("check: $p")
            if (p.first.visibleBy(best, remaining))
                p.first.hit()
        }

        // straight left
        (best.x - 1 downTo 0).map { it to best.y }.firstOrNull { it.visibleBy(best, remaining) }?.hit()
        println("Q4")
        val q4 = (best.x - 1 downTo 0).map { x -> (best.y - 1 downTo 0).map { y -> x to y } }.flatten()
            .filter { it in remaining }.map { it to atan((best.y - it.y).toDouble() / (best.x - it.x).toDouble()) }
        q4.sortedBy { it.second }.forEach { p ->
            //println("check: $p")
            if (p.first.visibleBy(best, remaining))
                p.first.hit()
        }

        return hitList[199].let { (x, y) -> x * 100 + y }
    }
}

fun main() {
    val testInput = """
.#..##.###...#######
##.############..##.
.#.######.########.#
.###.#######.####.#.
#####.##.#.##.###.##
..#####..#.#########
####################
#.####....###.#.#.##
##.#################
#####.##.###..####..
..######..##.#######
####.##.####...##..#
.#####..#.######.###
##...#.##########...
#.##########.#######
.####.#.###.###.#.##
....##.##.###..#####
.#.#.###########.###
#.#.#.#####.####.###
###.##.####.##.#..##
    """.trimIndent().split("\n")

    Day10().run()
}