import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import util.AStar

const val GO_UP = 1L
const val GO_DOWN = 2L
const val GO_LEFT = 3L
const val GO_RIGHT = 4L

class Day15(testData: String? = null) : Day<String>(15, 2019, ::asStrings, testData?.split("\n")) {

    val program = input.justLongs()

    override fun part1(): Int = runBlocking {

        val intcodeComputer =
            IntcodeComputer(program, input = { inChannel.receive() }, output = { outChannel.send(it) })
        val c = launch { intcodeComputer.runAsync() }

        var result = 0
        val explorer = launch { result = explore() }
        explorer.join()
        c.cancelAndJoin()
        result
    }

    val inChannel = Channel<Long>(1)
    val outChannel = Channel<Long>(1)

    enum class AreaType { WALL, FLOOR, TARGET }

    var position = origin
    var target = origin
    var knownTiles = mutableMapOf(origin to AreaType.WALL)

    private val engineKnown = AStar<Point>(
        neighborNodes = { pos ->
            Day11.Direction.values().mapNotNull {
                val p = when (it) {
                    Day11.Direction.UP -> pos.up()
                    Day11.Direction.DOWN -> pos.down()
                    Day11.Direction.LEFT -> pos.left()
                    Day11.Direction.RIGHT -> pos.right()
                }
                if (knownTiles[p] ?: AreaType.WALL != AreaType.WALL) p else null
            }
        },
        cost = { from, to -> (from - to).manhattanDistance },
        costEstimation = { from, to -> (from - to).manhattanDistance }
    )

    private val engine = AStar<Point>(
        neighborNodes = { p ->
            runBlocking {
                if (p.manhattanDistanceTo(position) > 1)
                    correctPosition(p)
                if (p != position) {
                    val cmd = when {
                        p == position.left() -> GO_LEFT
                        p == position.right() -> GO_RIGHT
                        p == position.up() -> GO_UP
                        p == position.down() -> GO_DOWN
                        else -> error("Way off! Droid at $position, needs to explore $p")
                    }
                    inChannel.send(cmd)
                    outChannel.receive()
                    position = p
                }
                testTile()
            }
            printMaze()
            Day11.Direction.values().mapNotNull {
                val p = when (it) {
                    Day11.Direction.UP -> position.up()
                    Day11.Direction.DOWN -> position.down()
                    Day11.Direction.LEFT -> position.left()
                    Day11.Direction.RIGHT -> position.right()
                }
                if (knownTiles[p] != AreaType.WALL) p else null
            }
        },
        cost = { from, to -> (from - to).manhattanDistance },
        costEstimation = { from, to -> (from - to).manhattanDistance })

    private suspend fun explore(): Int {
        testTile()
        printMaze()
        var n = 0
        while (!knownTiles.containsValue(AreaType.TARGET)) {
            val destNode = unknownPlaces().first()
            println("Exploration #${n++}. From $position to $destNode")
            val path = engine.search(position, destNode)
            if (path.second.isEmpty()) {
                println("Cannot reach $destNode!...")
            }
            println("Exploration done. New position is $position")
        }
        target = knownTiles.filterValues { it == AreaType.TARGET }.keys.first()

        val solution = engineKnown.search(origin, target)
        return solution.first[target]!!
    }

    private suspend fun correctPosition(p: Point): Long {
        println("Adjusting position to $p...")
        val path = engineKnown.search(position, p)

        val steps = mutableListOf(p)
        while (steps.last() != position) {
            steps.add(path.second[steps.last()]!!)
        }
        steps.reverse()
        steps.removeAt(0)
        steps.forEach { newPos ->
            val cmd = when (newPos) {
                position.up() -> GO_UP
                position.down() -> GO_DOWN
                position.left() -> GO_LEFT
                else -> GO_RIGHT
            }
            inChannel.send(cmd)
            outChannel.receive()
            position = newPos
        }

        println("Done")
        return 0L
    }

    fun unknownPlaces(): Sequence<Point> {
        val area = knownTiles.keys.areaCovered()
        return allPointsInArea(area.first, area.second).filter { knownTiles.containsKey(it) }
    }

    private fun printMaze() {
        println()
        println("D = $position")
        val area = knownTiles.keys.areaCovered()
        allPointsInArea(area.first, area.second).forEach { p ->
            if (position == p) print('D') else
                if (p == origin) print('o') else
                    print(
                        when (knownTiles[p]) {
                            AreaType.WALL -> '#'
                            AreaType.FLOOR -> '.'
                            AreaType.TARGET -> 'X'
                            else -> '?'
                        }
                    )
            if (p.x == area.second.x) println()
        }
    }

    suspend fun testTile() {
        (1..4).forEach {
            val probePosition = when (it) {
                1 -> position.up()
                2 -> position.down()
                3 -> position.left()
                else -> position.right()
            }
            if (!knownTiles.containsKey(probePosition)) {
                inChannel.send(it.toLong())
                val response = outChannel.receive()
                when (response) {
                    0L -> knownTiles[probePosition] = AreaType.WALL
                    1L -> knownTiles[probePosition] = AreaType.FLOOR
                    else -> knownTiles[probePosition] = AreaType.TARGET
                }
                if (response != 0L) {
                    val back = when (it) {
                        1 -> 2
                        2 -> 1
                        3 -> 4
                        else -> 3
                    }
                    inChannel.send(back.toLong())
                    outChannel.receive()
                }
            }
        }
    }

    override fun part2(): Any? {
        val area = knownTiles.keys.areaCovered()
        val max = allPointsInArea(area.first, area.second).filter {
            knownTiles[it] != AreaType.WALL
        }.map {
            val path = engineKnown.search(target, it)
            it to (path.first[it] ?: 0)
        }.maxBy { it.second }
        return max?.second
    }

}

fun main() {
    Day15().run()
}
