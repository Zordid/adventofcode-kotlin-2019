import kotlin.math.absoluteValue

typealias Point = Pair<Int, Int>

val Point.x: Int
    get() = first

val Point.y: Int
    get() = second

val Point.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue

fun Point.right(steps: Int = 1) = x + steps to y
fun Point.left(steps: Int = 1) = x - steps to y
fun Point.up(steps: Int = 1) = x to y - steps
fun Point.down(steps: Int = 1) = x to y + steps

val origin = 0 to 0

infix operator fun Point.plus(other: Point) = x + other.x to y + other.y

fun rectArea(from: Point, to: Point): Sequence<Point> = sequence {
    for (y in from.y .. to.y) {
        for (x in from.x .. to.x) {
            yield(x to y)
        }
    }
}

fun <T> List<List<T>>.matchingIndexes(predicate: (T)->Boolean): List<Point> =
    mapIndexed { y, l -> l.mapIndexedNotNull{ x, item -> if (predicate(item)) x to y else null } }.flatten()

fun gcd(a: Int, b: Int): Int = if (b == 0) a.absoluteValue else gcd(b, a % b)

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

