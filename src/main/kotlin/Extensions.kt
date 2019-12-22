import kotlin.math.absoluteValue
import kotlin.math.sign

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

    companion object {
        fun ofVector(v: Point): Direction? =
            with(v) {
                when (x.sign to y.sign) {
                    0 to -1 -> UP
                    1 to 0 -> RIGHT
                    0 to 1 -> DOWN
                    -1 to 0 -> LEFT
                    else -> null
                }
            }
    }
}

typealias Point = Pair<Int, Int>
typealias Area = Pair<Point, Point>

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

fun Point.neighbor(direction: Direction, steps: Int = 1) = this + (direction.vector * steps)
fun Point.neighbors() = Direction.values().map { neighbor(it) }

val origin = 0 to 0

infix operator fun Point.plus(other: Point) = x + other.x to y + other.y
infix operator fun Point.minus(other: Point) = x - other.x to y - other.y
infix operator fun Point.times(factor: Int) = when (factor) {
    0 -> origin
    1 -> this
    else -> x * factor to y * factor
}
infix operator fun Point.div(factor: Int) = when (factor) {
    1 -> this
    else -> x / factor to y / factor
}

operator fun Point.compareTo(other: Point): Int = if (y == other.y) x.compareTo(other.x) else y.compareTo(other.y)

fun allPointsInArea(from: Point, to: Point): Sequence<Point> {
    if (from > to) return allPointsInArea(to, from)
    return sequence {
        for (y in from.y..to.y) {
            for (x in from.x..to.x) {
                yield(x to y)
            }
        }
    }
}

fun Area.allPoints() = allPointsInArea(first, second)

operator fun Area.contains(p: Point) = p.x in first.x..second.x && p.y in first.y..second.y

val Area.width: Int
    get() = (second.x - first.x).absoluteValue + 1

val Area.height: Int
    get() = (second.y - first.y).absoluteValue + 1

fun Iterable<Point>.boundingBox(): Pair<Point, Point> {
    val minX = minBy { it.x }?.x!!
    val maxX = maxBy { it.x }?.x!!
    val minY = minBy { it.y }?.y!!
    val maxY = maxBy { it.y }?.y!!
    return (minX to minY) to (maxX to maxY)
}

operator fun <T> List<List<T>>.get(p: Point): T? =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] else null

fun <T> List<List<T>>.matchingIndexes(predicate: (T) -> Boolean): List<Point> =
    mapIndexed { y, l -> l.mapIndexedNotNull { x, item -> if (predicate(item)) x to y else null } }.flatten()

fun gcd(a: Int, b: Int): Int = if (b == 0) a.absoluteValue else gcd(b, a % b)
fun gcd(a: Long, b: Long): Long = if (b == 0L) a.absoluteValue else gcd(b, a % b)

fun lcm(a: Long, b: Long) = a * b / gcd(a, b)
fun lcm(a: Int, b: Int) = a * b / gcd(a, b)

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

fun <K, V> Map<K, V>.flip(): Map<V, K> = map { it.value to it.key }.toMap()