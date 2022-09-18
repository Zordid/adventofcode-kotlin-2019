class Day03 : Day<String>(3, 2019, ::asStrings) {

    val wireDefinitions = input.map { it.split(",") }
    val wirePoints = wireDefinitions.map(::tracePoints)
    val commonPoints = wirePoints[0].intersect(wirePoints[1].toSet())

    private fun tracePoints(wire: List<String>): List<Point> = wire.fold(mutableListOf()) { points, instruction ->
        val direction = instruction[0]
        val steps = instruction.substring(1).toInt()
        var c = points.lastOrNull() ?: (0 to 0)
        repeat(steps) {
            c = when (direction) {
                'R' -> c.right()
                'L' -> c.left()
                'U' -> c.up()
                'D' -> c.down()
                else -> error("unknown direction $direction")
            }
            points += c
        }
        points
    }

    override fun part1(): Int {
        val nearestToOrigin = commonPoints.minBy { it.manhattanDistance }

        println(nearestToOrigin)
        return nearestToOrigin.manhattanDistance
    }

    private fun List<Point>.signalDelayOf(p: Point) = indexOf(p) + 1
    private fun Point.signalDelay() = wirePoints[0].signalDelayOf(this) + wirePoints[1].signalDelayOf(this)

    override fun part2(): Int {
        val minimumSignalDelay = commonPoints.minBy { it.signalDelay() }

        println(minimumSignalDelay)
        return minimumSignalDelay.signalDelay()
    }

}

fun main() {
    Day03().run()
}