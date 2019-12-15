import kotlin.math.absoluteValue

enum class Direction {
    UP, RIGHT, DOWN, LEFT;

    val right: Direction
        get() = values()[(this.ordinal + 1) % values().size]
    val left: Direction
        get() = values()[(this.ordinal - 1 + values().size) % values().size]
    val opposite: Direction
        get() = values()[(this.ordinal + 2) % values().size]
    val vector: Point
        get() = when (this) {
            UP -> 0 to -1
            DOWN -> 0 to 1
            LEFT -> -1 to 0
            RIGHT -> 1 to 0
        }
}

typealias Point = Pair<Int, Int>

val Point.x: Int
    get() = first

val Point.y: Int
    get() = second

val Point.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue

infix fun Point.manhattanDistanceTo(other: Point) = (this - other).manhattanDistance

fun Point.right(steps: Int = 1) = x + steps to y
fun Point.left(steps: Int = 1) = x - steps to y
fun Point.up(steps: Int = 1) = x to y - steps
fun Point.down(steps: Int = 1) = x to y + steps

fun Point.neighbor(direction: Direction) = this + direction.vector
fun Point.neighbors() = Direction.values().map { neighbor(it) }

val origin = 0 to 0

infix operator fun Point.plus(other: Point) = x + other.x to y + other.y
infix operator fun Point.minus(other: Point) = x - other.x to y - other.y
infix operator fun Point.times(factor: Int) = x * factor to y * factor
infix operator fun Point.div(factor: Int) = x / factor to y / factor

fun allPointsInArea(from: Point, to: Point): Sequence<Point> = sequence {
    for (y in from.y..to.y) {
        for (x in from.x..to.x) {
            yield(x to y)
        }
    }
}

fun Pair<Point, Point>.allPoints() = allPointsInArea(first, second)

val Pair<Point, Point>.width: Int
    get() = second.x - first.x + 1

fun Iterable<Point>.boundingBox(): Pair<Point, Point> {
    val minX = minBy { it.x }?.x!!
    val maxX = maxBy { it.x }?.x!!
    val minY = minBy { it.y }?.y!!
    val maxY = maxBy { it.y }?.y!!
    return (minX to minY) to (maxX to maxY)
}

fun Iterable<Point>.widthOfBoundingBox(): Int {
    val minX = minBy { it.x }?.x!!
    val maxX = maxBy { it.x }?.x!!
    return maxX - minX
}

fun <T> List<List<T>>.matchingIndexes(predicate: (T) -> Boolean): List<Point> =
    mapIndexed { y, l -> l.mapIndexedNotNull { x, item -> if (predicate(item)) x to y else null } }.flatten()

fun gcd(a: Int, b: Int): Int = if (b == 0) a.absoluteValue else gcd(b, a % b)
fun gcd(a: Long, b: Long): Long = if (b == 0L) a.absoluteValue else gcd(b, a % b)

fun Iterable<Int>.gcd() = reduce { gcd, i -> gcd(i, gcd) }

fun <T> Iterable<T>.asEndlessSequence() = sequence { while (true) yieldAll(this@asEndlessSequence) }

fun <R, T> Sequence<T>.scan(seed: R, transform: (a: R, b: T) -> R): Sequence<R> = object : Sequence<R> {
    override fun iterator(): Iterator<R> = object : Iterator<R> {
        val it = this@scan.iterator()
        var last: R = seed
        var first = true

        override fun next(): R {
            if (first) first = false else last = transform(last, it.next())
            return last
        }

        override fun hasNext(): Boolean = it.hasNext()
    }
}

