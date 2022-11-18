import util.PixelGameEngine
import java.awt.Color
import kotlin.math.abs

class Day10Graphical(val day10: Day10 = Day10()) : PixelGameEngine() {

    val field = day10.field
    val asteroids = field.asteroids

    val best = day10.bestStation()
    val visibleByBest = day10.field.visibleFrom(best)
    val notVisibleByBest = asteroids - visibleByBest.toSet() - best
    val visibleByAngle = day10.field.visibleFromByAngle(best)

    val fps = 30

    val Int.seconds: Int
        get() = fps * this

    fun colorTransition(from: Color, to: Color, ratio: Float): Color {
        val red = abs(ratio * to.red + (1 - ratio) * from.red).toInt()
        val green = abs(ratio * to.green + (1 - ratio) * from.green).toInt()
        val blue = abs(ratio * to.blue + (1 - ratio) * from.blue).toInt()
        return Color(red, green, blue)
    }

    inline fun phase(cycleRange: IntRange, phaseLogic: (Int, Int) -> Unit) {
        if (cycle in cycleRange)
            phaseLogic(cycle - cycleRange.first, cycleRange.last - cycleRange.first)
    }

    var cycle = 0

    val hitIterator = day10.field.hitSequenceFrom(best).iterator()

    var currentlyDestroying: Point? = null
    var destroyCount = 0
    var solutionPart2: Point? = null

    fun drawAsteroid(p: Point, color: Color) {
        draw(p.x * 3 + 1, p.y * 3 + 1, color)
    }

    private val middleGray = Color(128, 128, 128)

    override fun onCreate() {
        construct(day10.field.dimX * 3, day10.field.dimY * 3, 6, 6, "AoC 2019 Day 10")
        asteroids.forEach { drawAsteroid(it, middleGray) }
        //drawAsteroid(best, Color.GREEN)
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        phase(1.seconds..3.seconds) { f, max ->
            val colorFadingOut = colorTransition(middleGray, Color(60, 60, 60), f.toFloat() / max)
            val colorFadingIn = colorTransition(middleGray, Color.WHITE, f.toFloat() / max)
            val colorBest = colorTransition(middleGray, Color.GREEN, f.toFloat() / max)
            visibleByBest.forEach { drawAsteroid(it, colorFadingIn) }
            notVisibleByBest.forEach { drawAsteroid(it, colorFadingOut) }
            drawAsteroid(best, colorBest)
        }
        phase(4.seconds..Int.MAX_VALUE) { _, _ ->
            currentlyDestroying?.apply {
                if (destroyCount != 200)
                    drawAsteroid(this, Color.BLACK)
                else {
                    solutionPart2 = currentlyDestroying
                    with(this) {
                        drawLine(x * 3 + 1, y * 3, x * 3 + 1, y * 3 + 2, Color.ORANGE)
                        drawLine(x * 3, y * 3 + 1, x * 3 + 2, y * 3 + 1, Color.ORANGE)
                    }
                }
                visibleByAngle.forEach { (_, l) ->
                    val idx = l.indexOf(this)
                    if (idx >= 0 && idx + 1 in l.indices) {
                        drawAsteroid(l[idx + 1], Color.WHITE)
                    }
                }
            }
            if (hitIterator.hasNext()) {
                destroyCount++
                appInfo = "destroyed: $destroyCount"
                currentlyDestroying = hitIterator.next()
                currentlyDestroying?.apply {
                    drawAsteroid(this, Color.RED)
                }
            } else {
                currentlyDestroying = null
                stop()
            }
        }
        cycle++
    }

}

fun main() {
    Day10Graphical().start()
}