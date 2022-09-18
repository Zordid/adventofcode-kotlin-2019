import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import util.Graph
import util.breadthFirstSearch
import util.depthFirstSearch

typealias Maze = List<List<Char>>

private const val SCAFFOLD = '#'
private const val SPACE = '.'

class Day17(testData: List<String>? = null) : Day<String>(17, 2019, ::asStrings, testData) {

    val program = input.asIntcode()


    fun retrieveMap(): MutableList<MutableList<Char>> = runBlocking {
        val outChannel = Channel<Long>(1)
        val soc = IntcodeComputer(program, output = byOutChannel(outChannel))
        val socJob = launch { soc.runAsync() }
        val m = mutableListOf(mutableListOf<Char>())
        launch {
            while (true) {
                val n = outChannel.receive()
                if (n.toInt() == 10) {
                    if (m.last().isEmpty())
                        break
                    m.add(mutableListOf())
                } else {
                    m.last().add(n.toInt().toChar())
                }
//                val v = m.joinToString("\n") { it.joinToString("") }
//                println(v)
            }
        }.join()
        socJob.cancelAndJoin()

        m
    }

    private fun controlVacuumRobot(m: String, a: String, b: String, c: String): Long = runBlocking {
        var last = 0L
        val inChannel = Channel<Long>(0)

        suspend fun sendCommand(cmd: String) {
            require(cmd.length <= 20)
            for (c in cmd) {
                inChannel.send(c.code.toLong())
                print(c)
            }
            inChannel.send(10L)
            println()
        }

        suspend fun output(v: Long) {
            if (v < 126) print(v.toInt().toChar())
            last = v
        }

        val soc = IntcodeComputer(program, byInChannel(inChannel), ::output)
        soc.memory[0] = 2

        val socJob = launch { soc.runAsync() }

        launch {
            sendCommand(m)
            sendCommand(a)
            sendCommand(b)
            sendCommand(c)
            sendCommand("n")
        }


        socJob.join()
        //soc.dump()
        last
    }

    override fun part1() = retrieveMap().findIntersections().sumOf { (x, y) -> x * y }

    override fun part2(): Long {
        findSolution(retrieveMap())
        return 0L

        val (mainFunc, a, b, c) = hardCodedSolution()
        return controlVacuumRobot(mainFunc, a, b, c)
    }

    private fun findSolution(m: MutableList<MutableList<Char>>) = MazeSolver(m).solver()

    private fun hardCodedSolution() = listOf(
        "A,A,C,B,C,A,B,C,B,A",
        "L,6,R,12,L,6,L,8,L,8",
        "L,4,L,4,L,6",
        "L,6,R,12,R,8,L,8"
    )

    operator fun Maze.get(p: Point): Char =
        if (p.y in this.indices && p.x in this[p.y].indices) this[p.y][p.x] else SPACE

    private val Maze.area: Pair<Point, Point>
        get() = origin to (first().lastIndex to lastIndex)

    private fun Maze.findIntersections() =
        area.allPoints().filter {
            this[it] == SCAFFOLD && it.neighbors().all { this[it] == SCAFFOLD }
        }.toList()

    private fun Maze.findCorners() =
        area.allPoints().filter {
            this[it] == SCAFFOLD && it.neighbors()
                .count { this[it] == SCAFFOLD } == 2 && (this[it.left()] != this[it.right()])
        }.toList()

    private fun Maze.find(c: Char) = area.allPoints().filter { this[it] == c }

    private fun Maze.findStart() = find('^').single()

    private fun Maze.findEnd() = area.allPoints().filter {
        this[it] == SCAFFOLD && it.neighbors().count { this[it] == SPACE } == 3
    }.single()

    private fun Maze.print(vararg special: Pair<Char, List<Point>>) {
        val p = mapIndexed { y, line ->
            line.mapIndexed { x, c ->
                val s = special.filter { (x to y) in it.second }.map { it.first }
                s.firstOrNull() ?: c
            }.joinToString("")
        }.joinToString("\n")
        println(p)
    }

    inner class MazeSolver(val maze: Maze) {
        val corners = maze.findCorners()
        val start = maze.findStart()
        val end = maze.findEnd()
        val allNodes = corners + start + end

        val path = buildDirectPath()
        val commands = buildCommands()

        fun buildDirectPath(): List<Point> {
            val graph = object : Graph<Point> {
                override fun neighborsOf(node: Point) = allNodes.filter { isConnected(node, it) }
            }
            return graph.breadthFirstSearch(start, end)
        }

        fun isConnected(a: Point, b: Point) =
            direction(a, b) != null && (a to b).allPoints().all { maze[it] != SPACE }

        fun direction(a: Point, b: Point) = Direction.ofVector(b - a)

        fun solver(): List<String> {
            println(start)
            println(end)
            println(corners)
            maze.print('X' to allNodes)
            println("Solution path is: $path")

            val commands = buildCommands()
            println("Commands for path: ${commands.joinToString(",")}")

            return emptyList()
            shrinkToThree(commands)

            val mainFun = mutableListOf<String>()
            val aFun = mutableListOf<String>()
            val bFun = mutableListOf<String>()
            val cFun = mutableListOf<String>()

            return listOf(mainFun, aFun, bFun, cFun).map { it.asCommand() }
        }

        private fun buildCommands() =
            path.drop(1).fold(Triple(start, Direction.UP, mutableListOf<String>())) { (p, d, cmd), nextPoint ->
                val diff = nextPoint - p
                val nextDirection = Direction.ofVector(diff)!!
                if (nextDirection == d.left) cmd.add("L") else cmd.add("R")
                cmd.add((p manhattanDistanceTo nextPoint).toString())
                Triple(nextPoint, nextDirection, cmd)
            }.third.toList()

        private fun shrinkToThree(commands: List<String>) {
            val pre = emptyList<String>()
            val post = emptyList<String>()
            val rest = commands.toMutableList()

            var solution = solutionPossible(pre, post, rest)
            while (solution.isEmpty()) {


                solution = solutionPossible(pre, post, rest)
            }
        }

        private fun solutionPossible(vararg elements: List<String>): List<String> {
            if (elements.any { !it.fits() })
                return emptyList()
            val graph = object : Graph<Int> {
                override fun neighborsOf(node: Int): Collection<Int> {
                    return elements.filter { it == commands.subList(node, node + it.size) }.map { node + it.size }
                }
            }
            val solution = graph.depthFirstSearch(0, commands.size)
            if (solution.isNotEmpty()) {
                val mainCommands =
                    solution
                        .zipWithNext()
                        .map { (f, t) -> ('A' + elements.indexOf(commands.subList(f, t))).toString() }
                if (mainCommands.fits())
                    return mainCommands
            }
            return emptyList()
        }


        private fun List<String>.fits() = commandLength() <= 20

        private fun List<String>.commandLength() = sumOf { it.length } + size - 1

        private fun List<String>.asCommand() = joinToString(",")

    }

}


fun main() {
    Day17().run()
}