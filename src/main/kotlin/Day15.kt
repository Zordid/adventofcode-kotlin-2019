import Direction.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import util.AStarSearch
import util.Graph
import util.completeAcyclicTraverse

enum class AreaType { WALL, FLOOR }

class RobotControl(program: IntcodeProgram) {

    private val inChannel = Channel<Long>(1)
    private val outChannel = Channel<Long>(1)
    private val soc = IntcodeComputer(program, byInChannel(inChannel), byOutChannel(outChannel))
    var debugChannel: Channel<Boolean>? = null

    var currentPosition = origin
        private set
    val knownMap = mutableMapOf(origin to AreaType.FLOOR)

    var targetPosition: Point? = null
    var walkedAgainstWall = 0
    var walkedSuccessfully = 0

    private val Direction.cmd: Long
        get() = when (this) {
            UP -> 1L
            DOWN -> 2L
            LEFT -> 3L
            RIGHT -> 4L
        }

    init {
        GlobalScope.launch { soc.runAsync() }
    }

    suspend fun walk(direction: Direction): Boolean {
        val nextPosition = currentPosition.neighbor(direction)
        if (knownMap[nextPosition] == AreaType.WALL)
            return false
        inChannel.send(direction.cmd)
        val result = outChannel.receive()
        debugChannel?.receive()
        val wall = result == 0L
        val foundTarget = result == 2L
        knownMap[nextPosition] = if (wall) {
            walkedAgainstWall++
            AreaType.WALL
        } else {
            currentPosition = nextPosition
            walkedSuccessfully++
            AreaType.FLOOR
        }
        if (foundTarget) {
            targetPosition = currentPosition
        }
        return !wall
    }

    suspend fun exploreNeighbors() {
        values().forEach { direction ->
            if (!knownMap.containsKey(currentPosition.neighbor(direction)))
                if (walk(direction)) walk(direction.opposite)
        }
    }

    fun printStatus() {
        println("Droid is at $currentPosition")
        println("Droid walk stats: ${walkedAgainstWall + walkedSuccessfully} total, $walkedAgainstWall against wall.")
        val boundingBox = knownMap.keys.boundingBox()
        boundingBox.allPoints().chunked(boundingBox.width).forEach { row ->
            println(row.joinToString("") { pos ->
                when {
                    pos == currentPosition -> "D"
                    pos == targetPosition -> "$"
                    pos == origin -> "O"
                    knownMap[pos] == AreaType.WALL -> "#"
                    knownMap[pos] == AreaType.FLOOR -> "."
                    else -> "?"
                }
            })
        }
        println(boundingBox)
    }

    val knownGraph = object : Graph<Point> {
        override fun neighborsOf(node: Point): Collection<Point> =
            node.neighbors().filter { knownMap[it] == AreaType.FLOOR }

        override fun cost(from: Point, to: Point) = 1
        override fun costEstimation(from: Point, to: Point) = from manhattanDistanceTo to
    }

    val unknownGraph = object : Graph<Point> {
        override fun neighborsOf(node: Point): Collection<Point> {
            if (node.neighbors().any { !knownMap.containsKey(it) })
                runBlocking {
                    safeDriveTo(node)
                    exploreNeighbors()
                }
            return node.neighbors().filter { knownMap[it] == AreaType.FLOOR }
        }

        override fun cost(from: Point, to: Point) = 1
        override fun costEstimation(from: Point, to: Point) = from manhattanDistanceTo to
    }

    private suspend fun safeDriveTo(p: Point) {
        if (p == currentPosition) return
        val path = knownGraph.AStarSearch(currentPosition, p)
        path.forEach { newPos ->
            require(
                when (newPos) {
                    currentPosition.up() -> walk(UP)
                    currentPosition.down() -> walk(DOWN)
                    currentPosition.left() -> walk(LEFT)
                    currentPosition.right() -> walk(RIGHT)
                    currentPosition -> true
                    else -> false
                }
            )
        }
        require(currentPosition == p) { "Did not succeed walking to $p, stuck at $currentPosition" }
    }

}

class Day15(testData: String? = null) : Day<String>(15, 2019, ::asStrings, testData?.split("\n")) {

    val robot = RobotControl(input.asIntcode())

    suspend fun part1Async(): Int {
        val explorer = GlobalScope.launch { explore() }
        explorer.join()
        robot.printStatus()
        val solution = robot.unknownGraph.AStarSearch(robot.targetPosition!!, origin)
        return solution.size - 1
    }

    override fun part1(): Int = runBlocking {
        part1Async()
    }

    private suspend fun explore() {
        var direction = UP
        while (robot.targetPosition == null) {
            //robot.unknownGraph.depthFirstSearch(robot.currentPosition, Int.MIN_VALUE to Int.MIN_VALUE)
            //robot.unknownGraph.depthFirstSearch<Point>(robot.currentPosition) { it == robot.targetPosition }
            //robot.walk(Direction.values()[Random.nextInt(4)])
            with(robot) {
                if (walk(direction.left)) direction = direction.left else {
                    if (!walk(direction)) {
                        if (walk(direction.right)) direction = direction.right else {
                            walk(direction.opposite)
                            direction = direction.opposite
                        }
                    }
                }
            }
        }
        println("Exploration done!")
    }

    override fun part2(): Int {
        if (robot.targetPosition == null)
            part1()
        val levels = robot.unknownGraph.completeAcyclicTraverse(robot.targetPosition!!, false).count()
        return levels - 1
    }

}

fun main() {
    Day15().run()
}
