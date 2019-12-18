import util.*
import java.awt.Color
import java.util.*

class Day18(testData: List<String>? = null) : Day<String>(18, 2019, ::asStrings, testData) {

    val mapPart1 = input.map { it.toList() }
    val mapPart2 = mutateMapForPart2(mapPart1)

    val area = origin to (mapPart1[0].size - 1 to mapPart1.size - 1)
    val allKeys = area.allPoints().filter { mapPart1[it] in 'a'..'z' }.map { mapPart1[it] }.toSet()

    data class State(val pos: Point, val keys: Set<Char>)

    fun defineGraphForMap(map: List<List<Char>>) = object : Graph<State> {
        override fun neighborsOf(node: State) = node.pos.neighbors()
            .filter {
                val c = map[it.y][it.x]
                c == '.' || c == '@' || c in 'a'..'z' || (c in 'A'..'Z' && c.toLowerCase() in node.keys)
            }.map {
                val c = map[it.y][it.x]
                State(it, if (c in 'a'..'z') node.keys + c else node.keys)
            }
    }

    override fun part1(): Int {
        val graph = defineGraphForMap(mapPart1)
        val start = mapPart1.findFirst('@')!!
        val solution = graph.breadthFirstSearch<State>(State(start, emptySet())) { it.keys == allKeys }
        return solution.size - 1
    }

    data class MState(val keys: Set<Char>, val pos: List<Point>)

    fun defineGraphForRobotOrchestration(map: List<List<Char>>) = object : Graph<MState> {
        private val graph = defineGraphForMap(map)
        private val costCache = mutableMapOf<Pair<MState, MState>, Int>()
        private val keysWithDepthCache = Array(4) {
            mutableMapOf<State, List<Pair<Pair<Point, Char>, Int>>>()
        }
        private val relevantKeysFor = (0..3).map { seg ->
            doorsIn.filter { (door, idx) -> idx == seg }.map { it.key.toLowerCase() }.toSet()
        }

        override fun neighborsOf(node: MState): Collection<MState> {
            val r = node.pos.mapIndexed { idx, p ->
                val single = State(p, node.keys intersect relevantKeysFor[idx])
                val keysWithDepth = keysWithDepthCache[idx].getOrPut(single) {
                    val layers = graph.completeAcyclicTraverse(single)
                    val reachableKeys =
                        layers
                            .map { it.second }.flatten().map { it.pos to map[it.pos] }
                            .filter { it.second in 'a'..'z' && it.second !in node.keys }
                            .distinct().toList()

                    reachableKeys.map { k: Pair<Point, Char> ->
                        k to layers.first { l: Pair<Int, Set<State>> ->
                            k.second in l.second.flatMap { s -> s.keys }
                        }.let { it.first }
                    }
                }
                keysWithDepth.map {
                    val newState = MState(node.keys + it.first.second, node.pos.change(idx, it.first.first))
                    costCache.put(node to newState, it.second)
                    newState
                }
            }.flatten()

//        println("Looking at $node")
//        r.forEach {
//            println("${costCache[node to it]}-> $it")
//        }

            return r
        }

        override fun cost(node1: MState, node2: MState): Int {
            return costCache[node1 to node2]!!
        }
    }

    lateinit var stack: Stack<MState>

    override fun part2(): Int? {
        val map = mapPart2
        val starts = area.allPoints().filter { map[it] == '@' }
        val graph = defineGraphForRobotOrchestration(map)
        val start = MState(emptySet(), starts.toList())
        val result = graph.dijkstraSearch<MState>(start) { it.keys == allKeys }

        stack = buildStack(result.first, result.second)

        return result.second.first[result.first]
    }

    fun <T> List<T>.change(idx: Int, v: T): List<T> = toMutableList().apply { this[idx] = v }

    private val doorsIn = allKeys.map { it.toUpperCase() }.associateWith { key ->
        val p = area.allPoints().filter { mapPart2[it] == key }.single()
        when {
            p.x < 40 && p.y < 40 -> 0
            p.x > 40 && p.y < 40 -> 1
            p.x < 40 && p.y > 40 -> 2
            p.x > 40 && p.y > 40 -> 3
            else -> error("")
        }
    }

    private fun mutateMapForPart2(map: List<List<Char>>): List<List<Char>> {
        val at = map.findFirst('@')!!
        return map.mapIndexed { y, row ->
            if (y == at.y - 1 || y == at.y + 1) {
                row.toMutableList().apply {
                    this[at.x - 1] = '@'
                    this[at.x] = '#'
                    this[at.x + 1] = '@'
                }
            } else if (y == at.y) {
                row.toMutableList().apply {
                    this[at.x - 1] = '#'
                    this[at.x] = '#'
                    this[at.x + 1] = '#'
                }
            } else
                row
        }
    }
}

class Day18Graphical(val d: Day18) : PixelGameEngine() {

    val area = d.area
    val map = d.mapPart2
    val stack = d.stack
    val graph = d.defineGraphForMap(map)

    override fun onCreate() {
        area.allPoints().forEach { (x, y) ->
            when (map[x to y]) {
                '#' -> draw(x, y, Color.WHITE)
                '@' -> draw(x, y, Color.ORANGE)
                in 'a'..'z' -> draw(x, y, Color.GREEN)
                in 'A'..'Z' -> draw(x, y, Color.RED)
            }
        }
    }

    var pos = 0

    var posInPath = 0
    var currentPath: List<Point>? = null

    override fun onUpdate(elapsedTime: Long) {
        if (currentPath == null)
            sleep(2000)
        currentPath?.let { path ->
            posInPath++
            if (posInPath in 1..path.lastIndex) {
                draw(path[posInPath - 1].x, path[posInPath - 1].y, Color.BLACK)
                draw(path[posInPath].x, path[posInPath].y, Color.ORANGE)
            } else
                currentPath = null
        }

        if (currentPath == null) {
            pos++
            if (pos in 1..stack.lastIndex) {
                val prevState = stack[pos - 1]
                val state = stack[pos]

                val moved = (0..3).first { prevState.pos[it] != state.pos[it] }
                val from = prevState.pos[moved]
                val to = state.pos[moved]

                currentPath =
                    graph.breadthFirstSearch(
                        Day18.State(from, prevState.keys),
                        Day18.State(to, state.keys)
                    ).map { it.pos }
                posInPath = 0
            }
        }
        appName = "$pos/${stack.size} ($posInPath/${currentPath?.size ?: -1})"
        sleep(20)
    }

}

operator fun List<List<Char>>.get(p: Point) = this[p.y][p.x]

fun List<List<Char>>.findFirst(c: Char): Point? {
    for (y in indices) {
        for (x in this[y].indices)
            if (this[y][x] == c)
                return x to y
    }
    return null
}

fun main() {
    val day18 = Day18()
    day18.run()
//    with(Day18Graphical(day18)) {
//        construct(81, 81, 12, 12)
//        start()
//    }
}