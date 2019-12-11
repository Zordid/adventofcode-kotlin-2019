import de.bmw.dojo.pixelgameengine.PixelGameEngine
import java.awt.Color
import kotlin.math.abs


class Day10Graphical(val day10: Day10) : PixelGameEngine() {

    val field = day10.field
    val asteroids = field.asteroids
    val best = day10.bestStation()
    val notVisibleByBest = asteroids - day10.field.visibleAsteroidsFrom(best) - best

    val fps = 30
    val sleep = 1000L / fps

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

    var cycle = -200

    val hitIterator = day10.field.hitSequenceFrom(best).iterator()

    var currentlyDestroying: Point? = null
    var destroyCount = 0
    var solutionPart2: Point? = null
    var visible = field.visibleAsteroidsFrom(best)

    fun drawAsteroid(p: Point, color: Color) {
        draw(p.x * 3 + 1, p.y * 3 + 1, color)
    }

    override fun onCreate() {
        asteroids.forEach { drawAsteroid(it, Color.WHITE) }
        drawAsteroid(best, Color.GREEN)
    }

    override fun onUpdate(elapsedTime: Long) {
        phase(1.seconds..3.seconds) { f, max ->
            notVisibleByBest.forEach {
                val color = colorTransition(Color.WHITE, Color(60, 60, 60), f.toFloat() / max)
                drawAsteroid(it, color)
            }
        }
        phase(3.seconds..Int.MAX_VALUE) { f, max ->
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
                field.asteroids.remove(this)
                val newVisible = field.visibleAsteroidsFrom(best)
                visible = newVisible
                newVisible.forEach { drawAsteroid(it, Color.WHITE) }
            }
            if (hitIterator.hasNext()) {
                destroyCount++
                currentlyDestroying = hitIterator.next()
                currentlyDestroying?.apply {
                    drawAsteroid(this, Color.RED)
                }
            } else {
                currentlyDestroying = null
            }
        }
        cycle++
        sleep(sleep)
    }


}

fun main() {
    val day10 = Day10()
    with(Day10Graphical(day10)) {
        construct(day10.field.dimX * 3, day10.field.dimY * 3, 6, 6)
        start()
    }
}