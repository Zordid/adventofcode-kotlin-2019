import util.*
import java.awt.Color
import java.util.*

class Day18(testData: List<String>? = null) : Day<String>(18, 2019, ::asStrings, testData) {

    val mapPart1 = input.map { it.toList() }
    val mapPart2 = mutateMapForPart2(mapPart1)

    val area = origin to (mapPart1[0].size - 1 to mapPart1.size - 1)
    val allKeys = area.allPoints().filter { mapPart1[it] in 'a'..'z' }.map { mapPart1[it]!! }.toSet()

    data class State(val pos: Point, val keys: Set<Char>)

    fun defineGraphForMap(map: List<List<Char>>) = object : Graph<State> {
        override fun neighborsOf(node: State) = node.pos.neighbors()
            .filter {
                val c = map[it.y][it.x]
                c == '.' || c == '@' || c in 'a'..'z' || (c in 'A'..'Z' && c.lowercaseChar() in node.keys)
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
        private fun createLocalStaticSearchGraph(keys: Set<Char>) = object : Graph<Point> {
            override fun neighborsOf(node: Point): Collection<Point> {
                return node.neighbors().filter {
                    val c = map[it.y][it.x]
                    c == '.' || c == '@' || c in 'a'..'z' || (c in 'A'..'Z' && c.lowercaseChar() in keys)
                }
            }
        }

        private val costCache = mutableMapOf<Pair<MState, MState>, Int>()
        private val keysWithDepthCache = Array(4) {
            mutableMapOf<State, Map<Char, Int>>()
        }
        private val relevantKeysFor = (0..3).map { seg ->
            doorToQuadrant.filterValues { idx -> idx == seg }.map { it.key.lowercaseChar() }.toSet()
        }
        private val keysIn = (0..3).map { seg ->
            keyToQuadrant.filterValues { idx -> idx == seg }.map { it.key }.toSet()
        }
        private val keyPos = allKeys.associateWith { map.findFirst(it)!! }

        private fun calculateReachableKeysWithDepth(
            idx: Int,
            state: State,
            keys: Set<Char>
        ): Map<Char, Int> {
            val keysMissing = (keysIn[idx] - keys).toMutableSet()
            val graph = createLocalStaticSearchGraph(state.keys)
            val layers = graph.completeBreadthFirstTraverse(state.pos).withIndex()
                .map { (level, layer) ->
                    level to layer.mapNotNull { pos -> map[pos].let { if (it in keysMissing) it else null } }
                }
                .filter { it.second.isNotEmpty() }
                .takeWhile { keysMissing.isNotEmpty() }
                .onEach { keysMissing -= it.second.toSet() }
                .toList()
            val reachableKeys = layers.map { it.second }.flatten()

            return reachableKeys.associateWith { key -> layers.first { key in it.second }.first }
        }

        override fun neighborsOf(node: MState): Collection<MState> {
            val r = node.pos.mapIndexed { idx, p ->
                val singleRobot = State(p, node.keys intersect relevantKeysFor[idx])
                val keysWithDepth = keysWithDepthCache[idx].getOrPut(singleRobot) {
                    calculateReachableKeysWithDepth(idx, singleRobot, node.keys)
                }
                keysWithDepth.map {
                    val newState = MState(node.keys + it.key, node.pos.change(idx, keyPos.getValue(it.key)))
                    costCache[node to newState] = it.value
                    newState
                }
            }.flatten()

//        println("Looking at $node")
//        r.forEach {
//            println("${costCache[node to it]}-> $it")
//        }

            return r
        }

        override fun cost(from: MState, to: MState): Int {
            return costCache[from to to]!!
        }
    }

    lateinit var stack: Stack<MState>

    override fun part2(): Int? {
        val map = mapPart2
        val starts = area.allPoints().filter { map[it] == '@' }
        val graph = defineGraphForRobotOrchestration(map)
        val start = MState(emptySet(), starts.toList())
        val result = graph.dijkstraSearch<MState>(start) { it.keys == allKeys }

        stack = result.buildStack()

        return result.distances[result.found]
    }

    fun <T> List<T>.change(idx: Int, v: T): List<T> = toMutableList().apply { this[idx] = v }

    private val doorToQuadrant = allKeys.map { it.uppercaseChar() }.associateWithQuadrant()
    private val keyToQuadrant = allKeys.associateWithQuadrant()

    private fun Iterable<Char>.associateWithQuadrant() = associateWith { key ->
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
            when (y) {
                at.y - 1, at.y + 1 -> {
                    row.toMutableList().apply {
                        this[at.x - 1] = '@'
                        this[at.x] = '#'
                        this[at.x + 1] = '@'
                    }
                }
                at.y -> {
                    row.toMutableList().apply {
                        this[at.x - 1] = '#'
                        this[at.x] = '#'
                        this[at.x + 1] = '#'
                    }
                }
                else -> row
            }
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

    override fun onUpdate(elapsedTime: Long, frame: Long) {
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
        appInfo = "$pos/${stack.size} ($posInPath/${currentPath?.size ?: -1})"
        sleep(20)
    }

}

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