import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

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

fun measureAverageMillis(count: Int, block: (Int) -> Unit) =
    (measureTimeMillis { repeat(count, block) } - measureTimeMillis { repeat(count) {} }).toDouble() / count
