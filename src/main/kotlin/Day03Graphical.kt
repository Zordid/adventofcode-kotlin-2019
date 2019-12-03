import de.bmw.dojo.pixelgameengine.PixelGameEngine
import java.awt.Color

class Day03Game : PixelGameEngine() {

    val day03 = Day03()

    val wires = day03.wireDefinitions.map(::traceLines)
    val wire1 = day03.wirePoints[0]
    val wire2 = day03.wirePoints[1]
    val commonPoints = day03.commonPoints
    val nearestOverlap = commonPoints.minBy(Point::manhattanDistance)!!
    val earliestOverlap = commonPoints.minBy { wire1.indexOf(it) + wire2.indexOf(it) }!!

    fun traceLines(wire: List<String>): List<Point> = wire.fold(mutableListOf(0 to 0)) { points, instruction ->
        val direction = instruction[0]
        val steps = instruction.substring(1).toInt()
        var c = points.last()
        c = when (direction) {
            'R' -> c.right(steps)
            'L' -> c.left(steps)
            'U' -> c.up(steps)
            'D' -> c.down(steps)
            else -> error("unknown direction $direction")
        }
        points += c
        points
    }

    var midpoint = 0 to 0
    val scale = 15

    override fun onCreate() {
        day03.run()
        appName = "AoC 2019 Day 3: Crossed Wires"

        midpoint = screenWidth / 2 to screenHeight / 2

        drawLine(0, midpoint.second, screenWidth, midpoint.second, Color.BLUE)
        drawLine(midpoint.first, 0, midpoint.first, screenHeight, Color.BLUE)

        val colors = listOf(Color.LIGHT_GRAY, Color.DARK_GRAY).asEndlessSequence().iterator()
        wires.forEach { p ->
            val color = colors.next()
            p.zipWithNext { a, b -> drawWire(a, b, color) }
        }

        commonPoints.forEach { it.drawOverlap(Color.RED) }
        nearestOverlap.drawOverlap(Color.CYAN)
        earliestOverlap.drawOverlap(Color.ORANGE)
    }

    fun drawWire(a: Point, b: Point, color: Color) {
        drawLine(
            midpoint.x + a.x / scale, midpoint.y - a.y / scale,
            midpoint.x + b.x / scale, midpoint.y - b.y / scale,
            color,
            0xAAAAAAAA
        )
    }

    fun Point.drawOverlap(color: Color, radius: Int = 10) {
        draw(midpoint.x + x / scale, midpoint.y - y / scale, color)
        drawCircle(midpoint.x + x / scale, midpoint.y - y / scale, radius, color)
    }

}

fun main() {
    with(Day03Game()) {
        construct(1000, 1000, 1, 1)
        start()
    }
}