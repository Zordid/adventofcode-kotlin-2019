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

fun <T> Iterable<T>.asEndlessSequence() = sequence { while (true) yieldAll(this@asEndlessSequence) }

