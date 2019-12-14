import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class Day11(testData: List<String>? = null) : Day<String>(11, 2019, ::asStrings, testData) {

    private val program = input.justLongs()

    enum class Direction {
        UP, RIGHT, DOWN, LEFT;

        companion object {
            const val directions = 4
        }

        val right: Direction
            get() = values()[(this.ordinal + 1) % directions]
        val left: Direction
            get() = values()[(this.ordinal - 1 + directions) % directions]
    }

    fun scanSpace(initialSpace: List<Point> = emptyList()): Pair<Int, Set<Point>> = runBlocking {
        val inputChannel = Channel<Long>(1)
        val outputChannel = Channel<Long>(2)

        val robot = IntcodeComputer(program, input = { inputChannel.receive() }, output = { outputChannel.send(it) })

        var direction = Direction.UP
        var currentPosition = 0 to 0

        val whitePanels = initialSpace.toMutableSet()
        val paintedPanels = mutableSetOf<Point>()

        launch { robot.runAsync() }
        val c = launch {
            while (true) {
                val color = if (whitePanels.contains(currentPosition))
                    1L else 0L
                inputChannel.send(color)

                val paint = outputChannel.receive()
                val turn = outputChannel.receive()

                if (color != paint) {
                    paintedPanels.add(currentPosition)
                    if (paint == 0L)
                        whitePanels.remove(currentPosition)
                    else
                        whitePanels.add(currentPosition)
                }

                direction = when (turn) {
                    0L -> direction.left
                    else -> direction.right
                }

                currentPosition = when (direction) {
                    Direction.UP -> currentPosition.up()
                    Direction.RIGHT -> currentPosition.right()
                    Direction.DOWN -> currentPosition.down()
                    Direction.LEFT -> currentPosition.left()
                }
            }
        }
        while (!robot.halt) yield()
        c.cancelAndJoin()

        paintedPanels.size to whitePanels
    }

    override fun part1() = scanSpace().first

    override fun part2(): String {
        val space = scanSpace(listOf(origin)).second

        val (upperLeft, lowerRight) = space.areaCovered()

        return allPointsInArea(upperLeft, lowerRight)
            .chunked(lowerRight.x - upperLeft.x + 1)
            .map { it.joinToString("") { if (space.contains(it)) "#" else " " } }
            .joinToString("\n")
    }
}

fun main() {
    Day11().run()
}