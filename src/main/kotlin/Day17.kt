import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

typealias Maze = List<List<Char>>

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
                    m.last().add(n.toChar())
                }
//                val v = m.joinToString("\n") { it.joinToString("") }
//                println(v)
            }
        }.join()
        socJob.cancelAndJoin()

        m
    }

    override fun part1() = retrieveMap().findIntersections().map { (x, y) -> x * y }.sum()


    override fun part2(): Any? = runBlocking {
        val m = retrieveMap()

        val (mainFunc, a, b, c) = solveMaze(m)

        readLine()

        var last = 0L

        suspend fun output(c: Long) {
            last = c
            print(c.toChar())
        }

        val inChannel = Channel<Long>(0)
        val soc = IntcodeComputer(program, output = ::output, input = byInChannel(inChannel))
        soc.memory[0] = 2

        suspend fun writeCommand(cmd: String) {
            require(cmd.length <= 20)
            for (c in cmd) {
                inChannel.send(c.toLong())
                print(c)
            }
            inChannel.send(10L)
            println()
        }

        val socJob = launch { soc.runAsync() }

        launch {
            writeCommand(mainFunc)
            writeCommand(a)
            writeCommand(b)
            writeCommand(c)
            writeCommand("n")
        }

        socJob.join()
        //soc.dump()

        last
    }

    private fun solveMaze(m: MutableList<MutableList<Char>>): List<String> {
        var mainFunc = "A,A,C"
        var funcA = "L,6,R,12,L,6,L,8,L,8"
        var funcB = "R,12,L,6,L,8,L,8"
        var funcC = "L,6,R,12"

        val intersections = m.findIntersections()
        val corners = m.findCorners()
        val start = m.find('^')!!
        val end = m.findEnd()

        m.print('S' to listOf(start), 'E' to end, 'O' to intersections, 'C' to corners)

        println("Intersections at: $intersections")
        println("Start at: $start")
        println("End at: $end")

        return listOf(mainFunc, funcA, funcB, funcC)
    }

    operator fun Maze.get(p: Point): Char =
        if (p.y in this.indices && p.x in this[p.y].indices) this[p.y][p.x] else '.'

    private val Maze.area: Pair<Point, Point>
        get() = origin to (first().lastIndex to lastIndex)

    private fun Maze.findIntersections() =
        area.allPoints().filter {
            this[it] == '#' && it.neighbors().all { this[it] == '#' }
        }.toList()

    private fun Maze.findCorners() =
        area.allPoints().filter {
            this[it] == '#' && it.neighbors().count { this[it] == '#' } == 2 && (this[it.left()] != this[it.right()])
        }.toList()

    private fun Maze.find(c: Char) = area.allPoints().find { this[it] == c }

    private fun Maze.findEnd() = area.allPoints().filter {
        this[it] == '#' && it.neighbors().count { this[it] == '.' } == 3
    }.toList()

    private fun Maze.print(vararg special: Pair<Char, List<Point>>) {
        val p = mapIndexed { y, line ->
            line.mapIndexed { x, c ->
                val s = special.filter { (x to y) in it.second }.map { it.first }
                s.firstOrNull() ?: c
            }.joinToString("")
        }.joinToString("\n")
        println(p)
    }

}

fun main() {
    Day17().run()
}